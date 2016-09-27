package com.example.user.musicdownloader.data;

import java.util.ArrayList;

/**
 * Created by B.E.L on 27/09/2016.
 */

public class Artist {

    private String artistName;
    private ArrayList<Song> artistSongs;

    public Artist(String artistName, ArrayList<Song> artistSongs) {
        this.artistName = artistName;
        this.artistSongs = artistSongs;
    }

    public String getArtistName() {
        return artistName;
    }

    public ArrayList<Song> getArtistSongs() {
        return artistSongs;
    }
}
