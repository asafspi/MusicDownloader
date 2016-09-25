package com.example.user.musicdownloader.recievers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.data.GetMusicData;
import com.example.user.musicdownloader.tools.Contextor;

import java.io.File;

import static com.example.user.musicdownloader.activities.MainActivity.downId;
import static com.example.user.musicdownloader.activities.MainActivity.pathId;

/**
 * Created by B.E.L on 25/09/2016.
 */

public class DownloadCompletedReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            Long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            if (downloadId == downId) {
                Log.d("TAG", "reciever got the doownload complete");
                File file = new File(Environment.DIRECTORY_MUSIC + File.separator + context.getString(R.string.app_name), pathId);
                MediaScannerConnection.scanFile(context.getApplicationContext(), new String[]{
                                file.getAbsolutePath()},
                        null, new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                if (uri != null) {
                                    Log.d("TAG", "onScanCompleted: " + uri.toString());
                                    GetMusicData.getAllSongs(Contextor.getInstance().getContext());
                                }
                            }

                        });
            }
        }
    }
}
