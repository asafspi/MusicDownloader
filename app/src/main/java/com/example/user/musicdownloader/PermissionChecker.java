package com.example.user.musicdownloader;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class PermissionChecker {

    public static boolean isPermissionsGranted(String... permissions) {
        for (String permission : permissions) {
            if (!isPermissionGranted(permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPermissionGranted(String permission) {
        Context context = Contextor.getInstance().getContext();
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
