package ru.arnis.pobedascanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.GregorianCalendar;
import java.util.Random;

import ru.arnis.pobedascanner.other.BounceListView;
import ru.arnis.pobedascanner.other.MyAdapter;
import ru.arnis.pobedascanner.other.Post;
import ru.arnis.pobedascanner.other.Utils;
import ru.arnis.pobedascanner.receviers.AlertReceiver;
import ru.arnis.pobedascanner.receviers.NetworkStateReceiver;
import ru.arnis.pobedascanner.services.Firebase;

public class MainActivity extends AppCompatActivity {

    public static String pobedaVkId = "-79459310";
    private int postCount;
    private BounceListView postsList;
    private ArrayList<Post> posts;
    private RelativeLayout panel;
    private boolean alarmUp;
    private PopupWindow dialog;
    private int scanning_interval = 1;
    private NetworkStateReceiver receiver;
    private boolean spinnerInit;

    public static final String PREFS = "prefs";
    public static final String POSTS = "posts";
    public static final String BOOL_SCANNING = "bool";
    public static final String INTERVAL = "interval";
    public static final int ALARM_FLAG = 1;
    private boolean isSpawningClouds;
    private DBhelper dbHelper;
    private ImageButton fab_base;
    private RelativeLayout mainLayout;
    public static int width;
    private View dimOverlay;
    private ImageView fab_icon;
    private Firebase analytics;
    private boolean debugModeOn;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = (RelativeLayout)findViewById(R.id.main_layout);
        dimOverlay = findViewById(R.id.dim_overlay);
        panel = (RelativeLayout)findViewById(R.id.button_panel);
        postsList = (BounceListView) findViewById(R.id.tabs);
        fab_base = (ImageButton) findViewById(R.id.fab_base);
        fab_icon = (ImageView) findViewById(R.id.fab_icon);

        analytics = new Firebase(this);
        posts = new ArrayList<>();

        getDisplayWidth();
        loadInterval();
        setUpSpinner();

        shadeCompat();

        fab_base.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    Utils.motionDown(view);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    Utils.motionUp(view);
                    if (!alarmUp){
                        if (!NetworkStateReceiver.connected)
                            Toast.makeText(getApplicationContext(),"Включите интернет для получения уведомлений",Toast.LENGTH_SHORT).show();
                        setAlarm(renderScanInterval());
                    }
                    else {
                        cancelAlarm();
                    }
//                    Log.d("happy", "ALARM " +alarmUp);
                }
                return true;
            }
        });
    }

    private void getDisplayWidth() {
        DisplayMetrics display = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(display);
        width = display.widthPixels;
    }

    private void setUpSpinner() {
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.intervals, R.layout.custom_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(scanning_interval-1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                scanning_interval = Integer.parseInt(adapterView.getItemAtPosition(i).toString().substring(0,1));
                if (checkAlarmUP()&&spinnerInit){
                    cancelAlarm();
                }
                if (!spinnerInit)
                    spinnerInit=true;
//                Log.d("happy", "scanning interval "+Integer.toString(scanning_interval));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadInterval() {
        SharedPreferences preferences = getSharedPreferences(PREFS,MODE_PRIVATE);
        scanning_interval = preferences.getInt(INTERVAL,1);
    }

    private NetworkInfo checkInternet() {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        analytics.createBundle().putValue(FirebaseAnalytics.Param.VALUE,scanning_interval).logEvent("scanning_interval");
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().putInt(INTERVAL,scanning_interval).apply();

    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new NetworkStateReceiver();
        dbHelper = new DBhelper(this,null,null,1);
        postCount = getPostCount();
        if (checkAlarmUP()){
            spawnClouds();
            Utils.fabIconAnim(fab_icon,true);
        }
        if (checkInternet()!=null){
            downloadPostCount();
            int count = postCount-getSharedPreferences(PREFS,MODE_PRIVATE).getInt(POSTS,0);
            if (count>0)
                downloadPosts(count);
            if (count<=0){
                dbHelper.clearTable();
                downloadPosts(postCount);
            }
            savePostCount();
        } else if (!dbHelper.checkIFempty()){
            loadPostsFromDB();
        } else {
            Toast.makeText(this,"Нет подключения к интернету",Toast.LENGTH_LONG).show();
        }
        posts = dbHelper.getPosts();
        initListView();
        registerReceiver(
                receiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        );
    }

    private int renderScanInterval() {
        if (debugModeOn)
            return 10000;
        switch (scanning_interval){
            case 1: return 1000*60*60*24;
            case 2: return 1000*60*60*12;
            case 3: return 1000*60*60*8;
            case 4: return 1000*60*60*6;
            default: return 1000*60*60*24;
        }
    }

    @Override
    protected void onDestroy() {
        ImageLoader.cachedPostImages.clear();
        if (receiver!=null)
            unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void showDialog() {
        final SharedPreferences preferences = getSharedPreferences(PREFS,MODE_PRIVATE);
        if (!preferences.getBoolean(BOOL_SCANNING,false)){
            analytics.createBundle().putString(FirebaseAnalytics.Param.ITEM_NAME,"show").logEvent("dialog");
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_window,null);
            dialog = new PopupWindow(view,width*2/3,width*2/3);
            TextView close = (TextView)view.findViewById(R.id.close_dialog);
            CheckBox checkBox = (CheckBox)view.findViewById(R.id.check_box);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    preferences.edit().putBoolean(BOOL_SCANNING,b).apply();
                    if (!b)
                        analytics.createBundle().putString(FirebaseAnalytics.Param.ITEM_NAME,"never_show").logEvent("dialog");
                }
            });
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Utils.undimAnim(dimOverlay);
                    fab_base.setClickable(true);
                }
            });
            Utils.dimAnim(dimOverlay);
            dialog.setAnimationStyle(R.style.Animation);
            dialog.showAtLocation(mainLayout, Gravity.CENTER,0,0);
            fab_base.setClickable(false);
        }

    }

    private boolean checkAlarmUP() {
        alarmUp = (PendingIntent.getBroadcast(this, ALARM_FLAG,
                new Intent(this,AlertReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
//        Log.d("happy", "ALARM " +alarmUp);
        return alarmUp;
    }

    private void shadeCompat() {
        if (Build.VERSION.SDK_INT<= Build.VERSION_CODES.LOLLIPOP){
            View shade1 = findViewById(R.id.pre_lolipop_shadow);
            shade1.setVisibility(View.VISIBLE);
        }
    }

    private int getPostCount() {
        SharedPreferences preferences = getSharedPreferences(PREFS,MODE_PRIVATE);
        return preferences.getInt(POSTS,0);
    }

    private void loadPostsFromDB() {
        posts = dbHelper.getPosts();
    }

//    public void vkAuth(View view) {
//        VKSdk.login(this, VKScope.WALL);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
//            @Override
//            public void onResult(VKAccessToken res) {
//                Toast.makeText(getApplicationContext(),"Auth successful",Toast.LENGTH_SHORT).show();
////                downloadPosts();
//
////                VKRequest request = new VKApiGroups().getById(VKParameters.from(VKApiConst.GROUP_ID,"pobeda.aero"));
////                request.executeWithListener(new VKRequest.VKRequestListener() {
////                    @Override
////                    public void onComplete(VKResponse response) {
////                        VKList vkList = (VKList) response.parsedModel;
////                        try {
////                            System.out.println(vkList.get(0).fields.getInt("id"));
////                        } catch (JSONException e) {
////                            e.printStackTrace();
////                        }
////                        super.onComplete(response);
////                    }
////                });
////                pobeda id = 79459310
//            }
//            @Override
//            public void onError(VKError error) {
//                Toast.makeText(getApplicationContext(),"Auth error",Toast.LENGTH_SHORT).show();
//// Произошла ошибка авторизации (например, пользователь запретил авторизацию)
//            }
//        })) {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }

    private void downloadPosts(int count) {
        VKRequest request = VKApi.wall().get(VKParameters.from(VKApiConst.OWNER_ID,pobedaVkId,VKApiConst.COUNT,count));
        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                System.out.println(response.responseString);
                try {
                    JSONObject resp = (JSONObject)response.json.get("response");
                    JSONArray jsonPosts = (JSONArray)resp.get("items");
                    for (int i = 0; i < jsonPosts.length(); i++) {
                        JSONObject jsonPost = (JSONObject) jsonPosts.get(i);
                        String text = jsonPost.getString("text");
                        if (checkPostForDeals(text)){
                            String url = null;
                            if (jsonPost.has("attachments")){
                                JSONArray jsonPhoto = (JSONArray) jsonPost.get("attachments");
                                JSONObject photos = (JSONObject)((JSONObject)(jsonPhoto.get(0))).get("photo");
                                url = photos.getString("photo_807");
                            }
                            Post post = new Post(text,url,Utils.getDateFromMilis(jsonPost.getString("date")));
                            posts.add(post);
                        }
                    }
                    dbHelper.addPosts(posts);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onComplete(response);
            }
        });
    }

    public static boolean checkPostForDeals(String text){
        return text.contains("от 999") || text.contains("от 1099") || text.contains("от 1199") || text.contains("от 1299") ||
                text.contains("от 1399") || text.contains("от 1499") || text.contains("от 1599") || text.contains("от 1699") ||
                text.contains("от 1799") || text.contains("от 1899") || text.contains("от 1999");
    }

    private void initListView(){
        MyAdapter myAdapter = new MyAdapter(posts,getApplicationContext());
        postsList.setAdapter(myAdapter);
        postsList.setVerticalScrollBarEnabled(false);
    }

    private void downloadPostCount(){
        final VKRequest request = VKApi.wall().get(VKParameters.from(VKApiConst.OWNER_ID,pobedaVkId));
        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
//                System.out.println(response.responseString);
                try {
                    postCount = ((JSONObject)response.json.get("response")).getInt("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onComplete(response);
            }
        });

    }

    private void savePostCount(){
        SharedPreferences.Editor editor = getSharedPreferences(PREFS,MODE_PRIVATE).edit();
        editor.putInt(POSTS,postCount).apply();
    }

    private void setAlarm(int interval) {
        analytics.createBundle().putString(FirebaseAnalytics.Param.ITEM_NAME,"start").logEvent("scanning");

        Utils.fabIconAnim(fab_icon,true);
        alarmUp=true;
        spawnClouds();
        showDialog();

        Long alertTime = new GregorianCalendar().getTimeInMillis();
        Intent intent = new Intent(this,AlertReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this,ALARM_FLAG,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,alertTime,interval,alarmIntent);
//        Log.d("happy", "ALARM SET FOR "+Integer.toString(scanning_interval));
    }

    private void cancelAlarm(){
        analytics.createBundle().putString(FirebaseAnalytics.Param.ITEM_NAME,"cancel").logEvent("scanning");

        Utils.fabIconAnim(fab_icon,false);
        alarmUp=false;
        isSpawningClouds =false;

        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, ALARM_FLAG, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.cancel(sender);
        sender.cancel();
    }

    private void spawnClouds(){
        if (!isSpawningClouds) {
            isSpawningClouds = true;
            final Random rnd = new Random();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (alarmUp) {
                        try {
                            Thread.sleep(400);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final ImageView cloud = new ImageView(getApplicationContext());
                                    cloud.setLayoutParams(new RelativeLayout.LayoutParams(170, 170));
                                    cloud.setX(panel.getWidth());
                                    cloud.setY(rnd.nextInt(panel.getHeight()));
                                    cloud.setImageResource(R.drawable.ic_cloud);
                                    panel.addView(cloud);
                                    cloud.animate().x(-200).setDuration(7000).withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    panel.removeView(cloud);
                                                }
                                            });
                                        }
                                    });
                                }
                            });

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }
    }

    public void debugModeOn(View view) {
        if (debugModeOn){
            debugModeOn=true;
            Toast.makeText(this,"Debug mode on",Toast.LENGTH_SHORT).show();
            pobedaVkId = "8672866";
        }
        else {
            debugModeOn=false;
            Toast.makeText(this,"Debug mode off",Toast.LENGTH_SHORT).show();
            pobedaVkId="-79459310";
        }
    }
}
