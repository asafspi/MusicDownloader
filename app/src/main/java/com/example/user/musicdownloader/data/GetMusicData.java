package com.example.user.musicdownloader.data;


import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.user.musicdownloader.EventBus.messages.MessageFromBackPressed;
import com.example.user.musicdownloader.tools.Contextor;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GetMusicData {
    public static ArrayList<Song> songs = new ArrayList<>();
    public static ArrayList<String> artists = new ArrayList<>();
    public static ArrayList<String> albums = new ArrayList<>();
    public static ArrayList<Song> downloads = new ArrayList<>();

    public static void getAllSongs(final ContentResolver cr, final String appName) {
        new Thread(new Runnable() {
            public void run() {
                if (songs.size() > 0) {
                    songs.clear();
                    artists.clear();
                    albums.clear();
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
                            String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                            Uri uriToImage = ContentUris.withAppendedId(sArtworkUri, albumId);
                            Uri uriOfSong = Uri.parse(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA)));
                            //String fileSize = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.SIZE));
                            File file = new File(String.valueOf(uriOfSong));
                            int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
                            if (!artists.contains(artist) && file_size > 0) {
                                artists.add(artist);
                            }
                            if (!albums.contains(album) && file_size > 0) {
                                albums.add(album);
                            }
                            if (!name.toLowerCase().contains("notification") && !name.toLowerCase().contains("ringtone") && file_size > 0) {
                                Song song = new Song(songUri, name, artist, album, null, uriOfSong, uriToImage);
                                songs.add(song);
                                if (String.valueOf(uriOfSong).contains(fileDownloads.getPath())){
                                    downloads.add(song);
                                }
                            }

                        }
                        cur.close();
                    }
                }
                Log.d("zaq", "FromThread");
                EventBus.getDefault().post(new MessageFromBackPressed(MessageFromBackPressed.FROM_THREAD));
            }
        }).start();
    }


    public static void getDataFromJson(String shortVideoId) {

        //String stringUrl = "http://www.youtubeinmp3.com/fetch/?format=JSON&video=" + editText.getText();
        String stringUrl = "http://www.youtubeinmp3.com/fetch/?format=JSON&video=" + "https://www.youtube.com/watch?v=" + shortVideoId;
        Log.d("zaq Url for json ", stringUrl);
        StringBuilder url = new StringBuilder(stringUrl);
        HttpURLConnection connection = null;
        InputStream in = null;
        JSONObject jsonObject;
        try {
            connection = (HttpURLConnection) new URL(url.toString()).openConnection();
            in = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String read = br.readLine();
            jsonObject = new JSONObject(read);
            String link = jsonObject.getString("link");
            String title = jsonObject.getString("title");
            Log.d("ZAQ", link);
            //download(link);
            downloadFile(title, link);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException | SecurityException | OutOfMemoryError e) {
            //ExceptionHandler.handleException(e);
            // Asaf sometimes SecurityException occurs, check it on Fabric
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void downloadFile(final String title, final String url) {
        final Context context = Contextor.getInstance().getContext();
        Log.d("zaq URL to download ", url);
        final DownloadManager.Request request;
        request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Downloading...");
        request.setTitle(title);
        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir("/Music Download/", title + ".mp3");
        // get download service and enqueue file
        DownloadManager manager;
        manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show();
        File file = new File(Environment.getExternalStorageDirectory() + "/Music Download/", title + ".mp3");
        boolean b = file.exists();
        int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
        Log.d("ZAQ file size === ", b + " " + String.valueOf(file_size));
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

    public static int getSongPosition(Song song) {
        return songs.indexOf(song);
    }
}
