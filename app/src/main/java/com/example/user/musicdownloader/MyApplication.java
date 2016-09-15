package com.example.user.musicdownloader;

import android.app.Application;
import android.app.usage.UsageEvents;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.EventBus;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends Application {
    private Main2Activity main2Activity;

    @Override
    public void onCreate() {
        super.onCreate();
        Contextor.getInstance().init(this);
        Fabric.with(this, new Crashlytics());
        registerReceivers();
    }

    private void registerReceivers() {

    }

    public void setMainActivity(Main2Activity mCurrentActivity) {
        this.main2Activity = mCurrentActivity;
    }
    public Main2Activity getMainActivity() {
        return main2Activity;
    }

}
