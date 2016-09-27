package com.example.user.musicdownloader.EventBus.messages;

public class MessageEvent {

    private EVENT event;

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
