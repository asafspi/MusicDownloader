package com.example.user.musicdownloader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

public class ShPref {

    public static void put(int key, Object value) {
        Context ctx = Contextor.getInstance().getContext();
        put(ctx.getString(key), value);
    }

    public static void put(String key, Object value) {
        Context ctx = Contextor.getInstance().getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (value instanceof String) {
            prefs.edit().putString(key, (String) value).commit();
        } else if (value instanceof Integer) {
            prefs.edit().putInt(key, (int) value).commit();
        } else if (value instanceof Boolean) {
            prefs.edit().putBoolean(key, (boolean) value).commit();
        } else if (value instanceof Float) {
            prefs.edit().putFloat(key, (float) value).commit();
        } else if (value instanceof Long) {
            prefs.edit().putLong(key, (long) value).commit();
        } else {
            Log.e("zaq", "UNKNOWN TYPE OF VALUE");
        }
    }

    public static String getString(int key, String defaultValue) {
        Context ctx = Contextor.getInstance().getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(ctx.getString(key), defaultValue);
    }

    public static String getString(String key, String defaultValue) {
        Context ctx = Contextor.getInstance().getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(key, defaultValue);
    }

    public static int getInt(int key, int defaultValue) {
        Context ctx = Contextor.getInstance().getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt(ctx.getString(key), defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        Context ctx = Contextor.getInstance().getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt(key, defaultValue);
    }

    public static boolean getBoolean(int key, boolean defaultValue) {
        Context ctx = Contextor.getInstance().getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(ctx.getString(key), defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        Context ctx = Contextor.getInstance().getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(key, defaultValue);
    }

    public static float getFloat(int key, float defaultValue) {
        Context ctx = Contextor.getInstance().getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getFloat(ctx.getString(key), defaultValue);
    }

    public static long getLong(int key, long defaultValue) {
        Context ctx = Contextor.getInstance().getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        try {
            return prefs.getLong(ctx.getString(key), defaultValue);
        } catch (Resources.NotFoundException e) {
            return defaultValue;
        }
    }

    public static long getLong(String key, long defaultValue) {
        Context ctx = Contextor.getInstance().getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        try {
            return prefs.getLong(key, defaultValue);
        } catch (Resources.NotFoundException e) {
            return defaultValue;
        }
    }

    public static void remove(int key) {
        Context ctx = Contextor.getInstance().getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit().remove(ctx.getString(key)).commit();
    }

    public static void remove(String key) {
        Context ctx = Contextor.getInstance().getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit().remove(key).commit();
    }

}