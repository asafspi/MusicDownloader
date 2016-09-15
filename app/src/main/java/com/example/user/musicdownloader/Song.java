package com.example.user.musicdownloader;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;

public class Song implements Serializable {
    private String name, artist, album, playlist;
    private Uri uri;
    private Uri image;

    public Song(String name, String artist, String album, String playlist, Uri uri, Uri image ) {
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.playlist = playlist;
        this.uri = uri;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPlaylist() {
        return playlist;
    }

    public void setPlaylist(String playlist) {
        this.playlist = playlist;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri folderName) {
        this.uri = uri;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }
}
