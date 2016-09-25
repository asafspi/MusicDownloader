package com.example.user.musicdownloader.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.example.user.musicdownloader.tools.Contextor;
import com.example.user.musicdownloader.data.GetMusicData;
import com.example.user.musicdownloader.tools.PermissionChecker;
import com.example.user.musicdownloader.R;

import java.util.ArrayList;

//https://blog.stylingandroid.com/permissions-part-3/

public class PermissionsActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 0;
    public static final int REQUEST_CODE_PERMISSION_LOCATION = 1;
    public static final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 2;
    public static final int REQUEST_CODE_PERMISSION_WRITE_SETTINGS = 3;
    public static final int REQUEST_CODE_PERMISSION_PHONE_LOGS = 4;
    public static final int REQUEST_CODE_PERMISSION_SMS = 5;
    private Dialog dialog;


    private static final String EXTRA_PERMISSIONS = Contextor.getInstance().getContext().getPackageName() + "EXTRA_PERMISSIONS";

    private boolean requiresCheck;
    //private String[] permissions;


    public static void startActivityForResult(Activity activity, int requestCode, String... permissions) {
        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        ActivityCompat.startActivityForResult(activity, intent, requestCode, null);
    }

    public static void startActivityFromService(Context context, String... permissions) {
        Intent intent = new Intent(context, PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.setString("Activity", "PermissionsActivity");
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("This Activity needs to be launched using the static startActivityForResult() method.");
        }
        setContentView(R.layout.activity_permissions);
        requiresCheck = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requiresCheck) {
            String[] permissions = getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
            if (PermissionChecker.isPermissionsGranted(permissions)) {
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }
        } else {
            requiresCheck = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            requiresCheck = true;
            setResult(Activity.RESULT_OK);
            GetMusicData.getAllSongs(getContentResolver(), getString(R.string.app_name));
            Log.d("zaq", "from permission");
            finish();
        } else {
            requiresCheck = false;
            showMissingPermissionDialog(permissions, grantResults);
        }
    }

    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void showMissingPermissionDialog(@NonNull String[] permissions, @NonNull int[] grantResults) {
        final  String[] deniedPermissions = removeApprovedPermission(permissions, grantResults);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PermissionsActivity.this);
        if (deniedPermissions.length == 1) {
            dialogBuilder.setTitle(getTitleResource(deniedPermissions[0]));
            dialogBuilder.setMessage(getMessageResource(deniedPermissions[0]));
        } else {
            dialogBuilder.setTitle(R.string.alert_default_title);
            dialogBuilder.setMessage(R.string.alert_default_permissions);
        }
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dial, int which) {
                ActivityCompat.requestPermissions(PermissionsActivity.this, deniedPermissions, PERMISSION_REQUEST_CODE);
                if (dialog != null && dialog.isShowing() &&!isFinishing()) {
                    try {
                        dialog.dismiss();
                    } catch (IllegalArgumentException e) {
                        //ExceptionHandler.handleException(e);
                    }
                }
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dial, int which) {
                setResult(Activity.RESULT_CANCELED);
                if (dialog != null && dialog.isShowing() &&!isFinishing()) {
                    try {
                        dialog.dismiss();
                    } catch (IllegalArgumentException e) {
                        //ExceptionHandler.handleException(e);
                    }
                }
                finish();
            }
        });
        dialog = dialogBuilder.show();
    }

    private String[] removeApprovedPermission(String[] permissions, int[] grantResults) {
        ArrayList<String> denied = new ArrayList<>();
        int count = 0;
        for (int result : grantResults){
            if (result == PackageManager.PERMISSION_DENIED){
                denied.add(permissions[count]);
            }
            count++;
        }
        return denied.toArray(new String[denied.size()]);
    }

    private int getTitleResource(@NonNull String deniedPermission) {
        switch (deniedPermission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return R.string.alert_location_title;
            case Manifest.permission.READ_CALL_LOG:
        }
        return R.string.alert_default_title;
    }

    private int getMessageResource(@NonNull String deniedPermission) {
        switch (deniedPermission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return R.string.alert_write_external_storage_permissions;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return R.string.alert_read_external_storage_permissions;
            case Manifest.permission.WRITE_SETTINGS:
                return R.string.alert_read_write_settings_permissions;
        }
        return R.string.alert_default_permissions;
    }

}
