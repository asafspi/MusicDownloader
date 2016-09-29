package com.musicplayer.freedownload.data;

import java.util.ArrayList;

/**
 * Created by B.E.L on 27/09/2016.
 */

public class Artist {

    private String artistName;
    private ArrayList<Song> artistSongs;

    public Artist(Song song) {
        this.artistName = song.getArtist();
        this.artistSongs = new ArrayList<>();
        this.artistSongs.add(song);
    }

    public String getArtistName() {
        return artistName;
    }

    public ArrayList<Song> getArtistSongs() {
        return artistSongs;
    }

    public String getNumberOfSongs() {
        return Integer.toString(artistSongs.size()) + " Songs";
    }
}
