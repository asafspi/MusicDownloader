package com.example.user.musicdownloader;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.example.user.musicdownloader.activities.MainActivity;
import com.example.user.musicdownloader.tools.Contextor;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends Application {
    private MainActivity main2Activity;

    @Override
    public void onCreate() {
        super.onCreate();
        Contextor.getInstance().init(this);
        Fabric.with(this, new Crashlytics());
        registerReceivers();
    }

    private void registerReceivers() {

    }

    public void setMainActivity(MainActivity mCurrentActivity) {
        this.main2Activity = mCurrentActivity;
    }
    public MainActivity getMainActivity() {
        return main2Activity;
    }

}
