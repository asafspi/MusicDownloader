package com.example.user.musicdownloader.EventBus.messages;

public class MessageEvent {

    public final String message;
    public final String songName;
    public final String songArtist;
    public final String songThumb;
    public final int songDuration;
    public final int currentDuration;

    public MessageEvent(String message, int songDuration , int currentDuration, String songName, String songArtist, String songThumb) {
        this.message = message;
        this.songDuration = songDuration;
        this.currentDuration = currentDuration;
        this.songName = songName;
        this.songArtist = songArtist;
        this.songThumb = songThumb;
    }
}
