package com.example.user.musicdownloader.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.activities.MainActivity;
import com.example.user.musicdownloader.data.GetMusicData;
import com.example.user.musicdownloader.data.Song;
import com.example.user.musicdownloader.services.PlaySongService;

import static com.example.user.musicdownloader.data.GetMusicData.songs;

/**
 * Created by User on 9/11/2016.
 */

public class Utils {


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };



    public static void changeSong(int i){
            Context context = Contextor.getInstance().getContext();
            int position = 2;
            switch (i) {
                case 1:
                    position = GetMusicData.getSongPosition(PlaySongService.currentPlayedSong) + 1;
                    break;
                case -1:
                    position = GetMusicData.getSongPosition(PlaySongService.currentPlayedSong) - 1;
                    break;
            }
            ShPref.put(R.string.song_path_for_service, songs.get(position).getUri().toString());
            ShPref.put(R.string.song_name_for_service, songs.get(position).getName());
            ShPref.put(R.string.song_artist_for_service, songs.get(position).getArtist());
            ShPref.put(R.string.song_thumb_for_service, songs.get(position).getImage().toString());
            context.stopService(new Intent(context, PlaySongService.class));
            context.startService(new Intent(context, PlaySongService.class));

    }

    public static void playFromInternet(String name, String artist, String path){
        Context context = Contextor.getInstance().getContext();
        ShPref.put(R.string.song_path_for_service, path);
        ShPref.put(R.string.song_name_for_service, name);
        ShPref.put(R.string.song_artist_for_service, artist);
        context.stopService(new Intent(context, PlaySongService.class));
        context.startService(new Intent(context, PlaySongService.class));
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public static void setSongAsRingtone(Context context, Song song){
        boolean permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(context);
        } else {
            permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        if (permission) {
            RingtoneManager.setActualDefaultRingtoneUri(
                    context,
                    RingtoneManager.TYPE_RINGTONE,
                    song.getSongUri()
            );
            Toast.makeText(context, "song set as ringtone", Toast.LENGTH_LONG).show();
        }  else {
            MainActivity.requestedBTSong = song;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                ((Activity)context).startActivityForResult(intent, MainActivity.CODE_WRITE_BT_SETTINGS_PERMISSION);
            } else {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_SETTINGS}, MainActivity.CODE_WRITE_BT_SETTINGS_PERMISSION);
            }
        }
    }
}
