package com.musicplayer.freedownload.data;


import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.musicplayer.freedownload.EventBus.messages.MessageFromBackPressed;
import com.musicplayer.freedownload.adapters.RecyclerAdapterSearch;
import com.musicplayer.freedownload.tools.Contextor;
import com.musicplayer.freedownload.tools.PermissionChecker;
import com.startapp.android.publish.StartAppAd;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

public class GetMusicData {
    public static ArrayList<Song> songs = new ArrayList<>();
    public static ArrayList<Artist> artists = new ArrayList<>();
    public static ArrayList<Album> albums = new ArrayList<>();
    public static ArrayList<Song> downloads = new ArrayList<>();
    public static int counterForDownloads = 0;
    public static void getAllSongs(final ContentResolver cr, final String appName) {
        new Thread(new Runnable() {
            public void run() {
                if (songs.size() > 0) {
                    songs.clear();
                    artists.clear();
                    albums.clear();
                    downloads.clear();
                }

                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String selection = MediaStore.Audio.Media.ARTIST + "!= 0";
                String sortOrder = MediaStore.Audio.Media.ARTIST + " ASC";

                Cursor cur;
                cur = cr.query(uri, null, selection, null, sortOrder);
                int count = 0;
                File fileDownloads =  new File(Environment.DIRECTORY_MUSIC + File.separator + appName);
                if (cur != null) {
                    count = cur.getCount();
                    if (count > 0) {
                        while (cur.moveToNext()) {
                            String name = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                            String album = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                            long albumId = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                            Long id = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media._ID));
                            Uri songUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString());
                            String artistName = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                            Uri uriToImage = ContentUris.withAppendedId(sArtworkUri, albumId);
                            Uri uriOfSong = Uri.parse(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA)));
                            //String fileSize = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.SIZE));
                            File file = new File(String.valueOf(uriOfSong));
                            int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));

                            if (!name.toLowerCase().contains("notification") && !name.toLowerCase().contains("ringtone") && file_size > 0) {
                                Song song = new Song(songUri, name, artistName, album, uriOfSong, uriToImage);
                                songs.add(song);
                                addSongToArtisList(song);
                                addSongToAlbumList(song);
                                if (String.valueOf(uriOfSong).contains(fileDownloads.getPath()) && file_size > 0){
                                    downloads.add(song);
                                    counterForDownloads ++;
                                }
                            }

                        }
                        cur.close();
                    }
                }
                Log.d("zaq", "FromThread");
                EventBus.getDefault().post(new MessageFromBackPressed(MessageFromBackPressed.FROM_THREAD));
                String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                if (PermissionChecker.isPermissionsGranted(permissions)) {
                    StartAppAd.showAd(Contextor.getInstance().getContext());
                }
            }
        }).start();
    }

    private static void addSongToArtisList(Song song){
        boolean artFound = false;
        for (Artist art : artists){
            if (art.getArtistName().equals(song.getArtist())){
                art.getArtistSongs().add(song);
                artFound = true;
                break;
            }
        }
        if (!artFound){
            Artist artist = new Artist(song);
            artists.add(artist);
        }
    }

    private static void addSongToAlbumList(Song song){
        boolean albumFound = false;
        for (Album album : albums){
            if (album.getAlbumName().equals(song.getAlbum())){
                album.getAlbumSongs().add(song);
                albumFound = true;
                break;
            }
        }
        if (!albumFound){
            Album album = new Album(song);
            albums.add(album);
        }
    }


    public static int getSongPosition(String currentSong) {

        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getName().equals(currentSong)) {
                return i;
            }
        }
        return 1;
    }

    public static String getNumberOfSongsFromArtist(String artist) {
        int counter = 0;
        String counterToString;
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getArtist().equals(artist)) {
                counter++;
            }
        }
        counterToString = String.valueOf(counter);
        return counterToString;
    }

    public static int getNumberOfSongsForArtist(String artist) {
        int counter = 0;
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getArtist().equals(artist)) {
                counter++;
            }
        }
        return counter;
    }

    public static int getSongPosition(Song song) {
        return songs.indexOf(song);
    }


}
