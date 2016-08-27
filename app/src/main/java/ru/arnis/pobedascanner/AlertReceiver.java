package ru.arnis.pobedascanner;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by arnis on 19/08/16.
 */
public class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.d("happy", "Alarm triggered");
        new Firebase(context).createBundle().putString(FirebaseAnalytics.Param.ITEM_NAME,"trigger").logEvent("alarm");
        startService(context);
    }

    private void startService(Context context) {
        Intent intent = new Intent(context,PobedaScannerService.class);
        context.startService(intent);
    }


}
