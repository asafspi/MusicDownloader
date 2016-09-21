package com.example.user.musicdownloader.tools;

import com.example.user.musicdownloader.data.SearchedSong;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by B.E.L on 21/09/2016.
 */

public class SearchHelper {

    private static final String URL_PREFIX = "http://mp3.sogou.com/music.so?st=1&query=";
    private static final String URL_SUFFIX = "&comp=1";

    public static ArrayList<SearchedSong> searchWeb(String query){
        try {
            Element connectionElement = Jsoup.connect(URL_PREFIX + query + URL_SUFFIX).timeout(8000).ignoreHttpErrors(true).get().body();

            final ArrayList<SearchedSong> songs = new ArrayList<>();
            String songLink = null;
            String songLabel = null;
            String songArtist = null;
            String songAlbum = null;
            LOOP: for (Element element : connectionElement.getElementsByClass("play_btn")){
                Pattern p = Pattern.compile("#(.*?)#");
                Matcher matcher = p.matcher(element.attr("onclick"));
                int i = 0;
                String s;
                MATCHER: while (matcher.find()) {
                    s = matcher.group(1);
                    switch (i){
                        case 2:
                            if (s == null || s.length() < 10){ //link is broken
                                continue LOOP;
                            }
                            songLink = s;
                            break;
                        case 3:
                            songLabel = s;
                            break;
                        case 5 :
                            songArtist = s;
                            break;
                        case 7:
                            songAlbum = s;
                            break MATCHER;

                    }
//                        System.out.println(s);
                    i++;
                }
                SearchedSong song = new SearchedSong(songLink, songLabel, songArtist, songAlbum);
                songs.add(song);
//                    System.out.println("_____________________________________________________________");
            }
            return songs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
