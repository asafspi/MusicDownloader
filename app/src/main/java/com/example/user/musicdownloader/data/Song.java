package com.example.user.musicdownloader.data;

import android.net.Uri;

import java.io.Serializable;

public class Song implements Serializable {
    private String name, artist, album;
    private Uri uri;
    private Uri image;
    private Uri songUri;


    private boolean loadedToPlayer;

    public Song(Uri songUri, String name, String artist, String album,  Uri uri, Uri image ) {
        this.songUri = songUri;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.uri = uri;
        this.image = image;
    }

    public Song(String songLink, String songLabel, String songArtist, String songAlbum) {
        this.uri = Uri.parse(songLink);
        this.name = songLabel;
        this.artist = songArtist;
        this.album = songAlbum;
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

    public Uri getSongUri() {
        return songUri;
    }

    public boolean isLoadedToPlayer() {
        return loadedToPlayer;
    }

    public void setLoadedToPlayer(boolean loadedToPlayer) {
        this.loadedToPlayer = loadedToPlayer;
    }
}
