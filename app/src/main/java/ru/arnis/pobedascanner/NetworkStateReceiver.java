package ru.arnis.pobedascanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by arnis on 20/08/16.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    public static boolean connected;
    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.d("happy", "NETWORKSTATECHANGED");
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        connected = activeNetwork != null;
//        if (connected)
//            Log.d("happy", "NETWORKSTATECHANGED_CONNECTED");
        if (connected&&activeNetwork.isConnected()&&ImageLoader.cachedPostImages.size()==0){
//            Log.d("happy", "NETWORKSTATECHANGED_INIT_CACHING");
            DBhelper dbHelper = new DBhelper(context,null,null,1);
            ImageLoader.cacheImages(dbHelper.getPosts());
        }


    }
}
