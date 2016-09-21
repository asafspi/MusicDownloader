package com.example.user.musicdownloader.tools;

import android.content.Context;
import android.util.Log;


public class Contextor {
    private static Contextor sInstance;
    private Context mContext;

    public static Contextor getInstance() {
        if (sInstance == null) {
            Log.d("ZAQ", "creating MyApllication context");
            sInstance = new Contextor();
        }
        return sInstance;
    }

    public void init(Context pContext) {
        mContext = pContext;
    }

    public Context getContext() {
        return mContext;
    }

}
