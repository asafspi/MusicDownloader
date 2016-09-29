package com.musicplayer.freedownload;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.musicplayer.freedownload.activities.MainActivity;
import com.musicplayer.freedownload.tools.Contextor;
import com.startapp.android.publish.StartAppSDK;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends Application {
    private MainActivity mMainActivity;
    private String FLURRY_API_KEY ="GN228V28XFBT2847ZW48";

    @Override
    public void onCreate() {
        super.onCreate();
        Contextor.getInstance().init(this);
        Fabric.with(this, new Crashlytics());
        new FlurryAgent.Builder()
                .withLogEnabled(false)
                .build(this, FLURRY_API_KEY);
    }
}
