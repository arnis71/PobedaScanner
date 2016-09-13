package ru.arnis.pobedascanner.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.arnis.pobedascanner.DBhelper;
import ru.arnis.pobedascanner.MainActivity;
import ru.arnis.pobedascanner.other.Post;
import ru.arnis.pobedascanner.R;
import ru.arnis.pobedascanner.other.Utils;
import ru.arnis.pobedascanner.receviers.NetworkStateReceiver;

/**
 * Created by arnis on 19/08/16.
 */
public class PobedaScannerService extends Service {

    int savedPostCount;
    int livePostCount;

    public static final String POST_COUNT = "post_count";
    public static final String POSTS = "posts";

    @Override
    public void onCreate() {
        super.onCreate();
        new Firebase(getApplicationContext()).createBundle().putString(FirebaseAnalytics.Param.ITEM_NAME,"start").logEvent("service");
//        Log.d("happy", "Service strated");
    }

    @Override
    public void onDestroy() {
        new Firebase(getApplicationContext()).createBundle().putString(FirebaseAnalytics.Param.ITEM_NAME,"stop").logEvent("service");
//        Log.d("happy", "Service stopped");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("happy", "onStartCommand: "+NetworkStateReceiver.connected);
        if (NetworkStateReceiver.connected){
        new Thread(new Runnable() {
            @Override
            public void run() {
                getSavedPostCount();
                getLivePostCount();

            }
        }).start();
        } else {
            new Firebase(getApplicationContext()).createBundle().putString(FirebaseAnalytics.Param.ITEM_NAME,"stop_no_internet").logEvent("service");
            stopSelf();
        }
        return Service.START_STICKY;
    }

    private void checkPost(int count) {
        final DBhelper dbHelper = new DBhelper(this,null,null,1);
        VKRequest request = VKApi.wall().get(VKParameters.from(VKApiConst.OWNER_ID, MainActivity.pobedaVkId,VKApiConst.COUNT,count+1));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
//                System.out.println(response.responseString);
                try {
                    ArrayList<Post> posts = new ArrayList<>();
                    JSONObject resp = (JSONObject)response.json.get("response");
                    JSONArray jsonPosts = (JSONArray)resp.get("items");
                    for (int i = 0; i < jsonPosts.length(); i++) {
                        JSONObject jsonPost = (JSONObject) jsonPosts.get(i);
                        String text = jsonPost.getString("text");
                        if (MainActivity.checkPostForDeals(text)){
                            String url;
                            if (jsonPost.has("attachments")){
                                JSONArray jsonPhoto = (JSONArray) jsonPost.get("attachments");
                                JSONObject photos = (JSONObject)((JSONObject)(jsonPhoto.get(0))).get("photo");
                                url = photos.getString("photo_807");
                            } else url = null;
                            Post post = new Post(text,url, Utils.getDateFromMilis(jsonPost.getString("date")));
                            posts.add(post);
                        }
                    }
                    dbHelper.addPosts(posts);
                    fireNotification(getApplicationContext(),posts.get(0).getText());
                    SharedPreferences.Editor editor = getSharedPreferences(MainActivity.PREFS,MODE_PRIVATE).edit();
                    editor.putInt(MainActivity.POSTS,livePostCount).apply();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onComplete(response);
            }
        });
    }

    private void getSavedPostCount() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(POST_COUNT,MODE_PRIVATE);
        savedPostCount = preferences.getInt(POSTS,10);
    }

    private void getLivePostCount(){
        final VKRequest request = VKApi.wall().get(VKParameters.from(VKApiConst.OWNER_ID,MainActivity.pobedaVkId));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
//                System.out.println(response.responseString);
                try {
                    livePostCount = ((JSONObject)response.json.get("response")).getInt("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                serviceWork();
//                Log.d("happy", "saved posts " + Integer.toString(savedPostCount));
//                Log.d("happy", "retreived posts " + Integer.toString(livePostCount));
                super.onComplete(response);
            }
        });
    }

    private void serviceWork(){
        if (livePostCount> savedPostCount)
            checkPost(livePostCount- savedPostCount);

        getSharedPreferences(MainActivity.PREFS,MODE_PRIVATE).edit().putInt(MainActivity.POSTS,livePostCount).apply();
        stopSelf();
    }

    private void fireNotification(Context context, String text) {
//        Log.d("happy", "fireNotification");
        new Firebase(context).createBundle().putString(FirebaseAnalytics.Param.ITEM_NAME,"notification").logEvent("service");
        PendingIntent notificationIntent = PendingIntent.getActivity(context,10, new Intent(getApplicationContext(),MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Успей купить!")
                .setContentText("В продаже появились билеты за "+getPriceFromText(text))
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setLights(Color.BLUE,1000,1000)
                .setAutoCancel(true)
                .setContentIntent(notificationIntent);

        manager.notify(1,builder.build());
    }

    private String getPriceFromText(String text){
        if (text.contains("от 999")) return "999";
        else if (text.contains("от 1099")) return "1099";
        else if (text.contains("от 1199")) return "1199";
        else if (text.contains("от 1299")) return "1299";
        else if (text.contains("от 1399")) return "1399";
        else if (text.contains("от 1499")) return "1499";
        else if (text.contains("от 1599")) return "1599";
        else if (text.contains("от 1699")) return "1699";
        else if (text.contains("от 1799")) return "1799";
        else if (text.contains("от 1899")) return "1899";
        else if (text.contains("от 1999")) return "1999";
        return "";
    }
}
