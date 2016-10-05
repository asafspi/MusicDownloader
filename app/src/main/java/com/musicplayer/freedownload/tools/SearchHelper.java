package com.musicplayer.freedownload.tools;

import android.os.Handler;
import android.util.Log;

import com.musicplayer.freedownload.data.Song;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.musicplayer.freedownload.activities.MainActivity.query;

/**
 * Created by B.E.L on 21/09/2016.
 */

public class SearchHelper {

    private static final String URL_PREFIX = "http://mp3.sogou.com/music.so?st=1&query=";
    private static final String URL_SUFFIX = "&comp=1";
    private static long last;
    private static String currentQueried;


    public static ArrayList<Song> searchWeb(String query) {
        try {
            Element connectionElement = Jsoup.connect(URL_PREFIX + query + URL_SUFFIX).timeout(8000).ignoreHttpErrors(true).get().body();

            final ArrayList<Song> songs = new ArrayList<>();
            String songLink = null;
            String songLabel = null;
            String songArtist = null;
            String songAlbum = null;
            LOOP:
            for (Element element : connectionElement.getElementsByClass("play_btn")) {
                Pattern p = Pattern.compile("#(.*?)#");
                Matcher matcher = p.matcher(element.attr("onclick"));
                int i = 0;
                String s;
                MATCHER:
                while (matcher.find()) {
                    s = matcher.group(1);
                    switch (i) {
                        case 2:
                            if (s == null || s.length() < 10) { //link is broken
                                continue LOOP;
                            }
                            songLink = s;
                            break;
                        case 3:
                            songLabel = s;
                            break;
                        case 5:
                            songArtist = s;
                            break;
                        case 7:
                            songAlbum = s;
                            break MATCHER;

                    }
                    i++;
                }
                Song song = new Song(songLink, songLabel, songArtist, songAlbum);
                songs.add(song);
            }
            return songs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void searchWeb(final Handler handler, final WeakReference<OnSearchFinishListener> listener) {
        if (query == null || query.equals(currentQueried)) {
            return;
        }
        Log.d("TAG", "searchWeb: " + query);
        final ArrayList<Song> songs = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    currentQueried = query;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            Log.d("TAG", "listener.onStartSearch");
                            OnSearchFinishListener onSearchFinishListener = listener.get();
                            if (onSearchFinishListener != null) {
                                onSearchFinishListener.onStartSearch(currentQueried);
                            }
                        }
                    });
                    Element connectionElement = Jsoup.connect(URL_PREFIX + currentQueried + URL_SUFFIX).timeout(8000).ignoreHttpErrors(true).get().body();

                    String songLink = null;
                    String songLabel = null;
                    String songArtist = null;
                    String songAlbum = null;
                    LOOP:
                    for (Element element : connectionElement.getElementsByClass("play_btn")) {
                        Pattern p = Pattern.compile("#(.*?)#");
                        Matcher matcher = p.matcher(element.attr("onclick"));
                        int i = 0;
                        String s;
                        MATCHER:
                        while (matcher.find()) {
                            s = matcher.group(1);
                            switch (i) {
                                case 2:
                                    if (s == null || s.length() < 10) { //link is broken
                                        continue LOOP;
                                    }
                                    songLink = s;
                                    break;
                                case 3:
                                    songLabel = s;
                                    break;
                                case 5:
                                    songArtist = s;
                                    break;
                                case 7:
                                    songAlbum = s;
                                    break MATCHER;

                            }
                            i++;
                        }
                        if (songLink == null) {
                            continue;
                        }
                        Song song = new Song(songLink, songLabel, songArtist, songAlbum);
                        songs.add(song);
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            OnSearchFinishListener onSearchFinishListener = listener.get();
                            if (onSearchFinishListener != null) {
                                onSearchFinishListener.onSuccess(songs);

                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    OnSearchFinishListener onSearchFinishListener = listener.get();
                    if (onSearchFinishListener != null) {
                        onSearchFinishListener.onFailure();
                    }
                    Log.d("TAG", "No Results");
                } finally {
                    currentQueried = null;
                }
            }
        }).start();
    }

    private static class runQueryRunable implements Runnable {

        private Handler handler;
        private OnSearchFinishListener listener;

        runQueryRunable(Handler handler, OnSearchFinishListener listener) {
            this.handler = handler;
            this.listener = listener;
        }

        @Override
        public void run() {
            searchWeb(handler, new WeakReference<>(listener));
        }
    }

    public interface OnSearchFinishListener {
        void onStartSearch(String query);

        void onSuccess(ArrayList<Song> songs);

        void onFailure();
    }
}
