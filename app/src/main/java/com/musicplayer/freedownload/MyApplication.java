package com.musicplayer.freedownload;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.musicplayer.freedownload.activities.MainActivity;
import com.musicplayer.freedownload.tools.Contextor;
import com.startapp.android.publish.StartAppSDK;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends Application {
    private MainActivity mMainActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        Contextor.getInstance().init(this);
        Fabric.with(this, new Crashlytics());

    }
}
