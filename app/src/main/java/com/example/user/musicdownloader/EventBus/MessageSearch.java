package com.example.user.musicdownloader.EventBus;

import com.example.user.musicdownloader.Song;

import java.util.ArrayList;

/**
 * Created by B.E.L on 20/09/2016.
 */

public class MessageSearch {
    private final ArrayList<Song> querySongs;
    private final String query;

    public MessageSearch(ArrayList<Song> querySongs, String query) {
        this.querySongs = querySongs;
        this.query = query;
    }

    public ArrayList<Song> getQuerySongs() {
        return querySongs;
    }

    public String getQuery() {
        return query;
    }
}
