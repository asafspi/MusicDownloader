package com.example.user.musicdownloader;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.example.user.musicdownloader.activities.MainActivity;
import com.example.user.musicdownloader.tools.Contextor;

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
