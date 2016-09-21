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

    public static void writePermission(){




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

    public static void playFromInternet(String name, String artist, String path){
        Context context = Contextor.getInstance().getContext();
        ShPref.put(R.string.song_path_for_service, path);
        ShPref.put(R.string.song_name_for_service, name);
        ShPref.put(R.string.song_artist_for_service, artist);
        context.stopService(new Intent(context, PlaySongService.class));
        context.startService(new Intent(context, PlaySongService.class));
    }
}
