package com.example.user.musicdownloader.data;

/**
 * Created by B.E.L on 18/09/2016.
 */

public class SearchedSong {
    private String songLink;
    private String songLabel;
    private String songArtist;
    private String songAlbum;

    public SearchedSong(String songLink, String songLabel, String songArtist, String songAlbum) {
        this.songLink = songLink;
        this.songLabel = songLabel;
        this.songArtist = songArtist;
        this.songAlbum = songAlbum;
    }

    public String getSongLink() {
        return songLink;
    }

    public String getSongLabel() {
        return songLabel;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getSongAlbum() {
        return songAlbum;
    }
}
