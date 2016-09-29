package com.musicplayer.freedownload.data;

import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by B.E.L on 27/09/2016.
 */

public class Album {

    private String albumName, artistName;
    private ArrayList<Song> albumSongs;
    private Uri albumUri;

    public Album(Song song) {
        this.albumName = song.getAlbum();
        this.albumSongs = new ArrayList<>();
        this.albumSongs.add(song);
        this.albumUri = song.getImage();
        this.artistName = song.getArtist();
    }

    public String getAlbumName() {
        return albumName;
    }

    public ArrayList<Song> getAlbumSongs() {
        return albumSongs;
    }

    public String getNumberOfSongs() {
        return Integer.toString(albumSongs.size()) + " Songs";
    }

    public Uri getAlbumUri() {
        return albumUri;
    }

    public String getArtistName() {
        return artistName;
    }
}
