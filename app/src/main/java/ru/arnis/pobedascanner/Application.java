package ru.arnis.pobedascanner;

import com.vk.sdk.VKSdk;

/**
 * Created by arnis on 18/08/16.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
