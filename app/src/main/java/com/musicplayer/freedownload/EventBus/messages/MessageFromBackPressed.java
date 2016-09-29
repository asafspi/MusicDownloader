package com.musicplayer.freedownload.EventBus.messages;

/**
 * Created by User on 9/11/2016.
 */



public class MessageFromBackPressed {

    public static final int FROM_BACK_PRESSED = 1;
    public static final int FROM_THREAD = 2;
    private int action, position;

    public MessageFromBackPressed(int action) {
        this.action = action;
    }

    public MessageFromBackPressed(int action, int position) {
        this.action = action;
        this.position = position;
    }

    public int getAction() {
        return action;
    }

    public int getPosition() {
        return position;
    }
}
