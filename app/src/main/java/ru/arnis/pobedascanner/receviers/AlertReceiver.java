package ru.arnis.pobedascanner.receviers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.analytics.FirebaseAnalytics;

import ru.arnis.pobedascanner.services.Firebase;
import ru.arnis.pobedascanner.services.PobedaScannerService;

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
