package com.musicplayer.freedownload.recievers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.musicplayer.freedownload.R;
import com.musicplayer.freedownload.data.GetMusicData;

import java.io.File;

import static com.musicplayer.freedownload.activities.MainActivity.downId;
import static com.musicplayer.freedownload.activities.MainActivity.pathId;

/**
 * Created by B.E.L on 25/09/2016.
 */

public class DownloadCompletedReciever extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            Long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

            Log.d("TAG", "downloadId: " + downloadId);
            if (downloadId == downId) {
                File file = new File(Environment.DIRECTORY_MUSIC + File.separator + context.getString(R.string.app_name), pathId);
                Log.d("TAG", "reciever download complete, file;" + file.getAbsolutePath());

                MediaScannerConnection.scanFile(context.getApplicationContext(), new String[]{
                                file.getAbsolutePath()},
                        null, new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.d("TAG", "onScanCompleted, Uri: " + uri + " , path: " + path);
                                GetMusicData.getAllSongs(context.getContentResolver(), context.getString(R.string.app_name));
                            }

                        });
            }
        }
    }
}
