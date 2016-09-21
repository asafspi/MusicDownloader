package com.example.user.musicdownloader.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.data.GetMusicData;
import com.example.user.musicdownloader.services.PlaySongService;

import static com.example.user.musicdownloader.data.GetMusicData.songs;

/**
 * Created by User on 9/11/2016.
 */

public class Utils {

    public static boolean isStoragePermissionGranted(Context main2Activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (main2Activity.getApplicationContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("ZAQ", "Permission is granted");
                GetMusicData.getAllSongs(main2Activity);
                return true;
            } else {

                Log.v("ZAQ", "Permission is revoked");
                ActivityCompat.requestPermissions((Activity) main2Activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                ActivityCompat.requestPermissions((Activity) main2Activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("ZAQ", "Permission is granted");
            GetMusicData.getAllSongs(main2Activity);
            return true;
        }
    }
    public static void changeSong(int i){
            Context context = Contextor.getInstance().getContext();
            int position = 2;
            switch (i) {
                case 1:
                    position = GetMusicData.getSongPosition(PlaySongService.songName) + 1;
                    break;
                case -1:
                    position = GetMusicData.getSongPosition(PlaySongService.songName) - 1;
                    break;
            }
            ShPref.put(R.string.song_path_for_service, songs.get(position).getUri().toString());
            ShPref.put(R.string.song_name_for_service, songs.get(position).getName());
            ShPref.put(R.string.song_artist_for_service, songs.get(position).getArtist());
            ShPref.put(R.string.song_thumb_for_service, songs.get(position).getImage().toString());
            context.stopService(new Intent(context, PlaySongService.class));
            context.startService(new Intent(context, PlaySongService.class));

    }
}
