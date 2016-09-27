package com.example.user.musicdownloader.EventBus.messages;

public class MessageEvent {

//    public String message;
//    public String songName;
//    public String songArtist;
//    public String songThumb;
//    public int songDuration;
//    public int currentDuration;
    private EVENT event;

//    public MessageEvent(String message, int songDuration , int currentDuration, String songName, String songArtist, String songThumb) {
//        this.message = message;
//        this.songDuration = songDuration;
//        this.currentDuration = currentDuration;
//        this.songName = songName;
//        this.songArtist = songArtist;
//        this.songThumb = songThumb;
//    }

    public MessageEvent(EVENT event) {
        this.event = event;
    }

    public EVENT getEvent() {
        return event;
    }

    public enum EVENT{
        CHANGE_BTN_TO_PLAY,
        CHANGE_BTN_TO_PAUSE,
        START_SONG,
        FROM_RUN,
        SONG_END,
        NEXT_SONG,
        PREVIOUS_SONG,
        FINISH,


    }
}
