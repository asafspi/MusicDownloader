package com.musicplayer.freedownload.EventBus.messages;

public class MessageEvent {

    private EVENT event;
    private boolean isPlaying;

    public MessageEvent(EVENT event, boolean isPlaying) {
        this.event = event;
        this.isPlaying = isPlaying;
    }

    public MessageEvent(EVENT event) {
        this.event = event;
    }

    public EVENT getEvent() {
        return event;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public enum EVENT{
        START_SONG,
        FROM_RUN,
        SONG_END,
        FINISH, PLAY_BTN_CLICKED,
    }
}
