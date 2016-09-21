package com.example.user.musicdownloader.EventBus.messages;

/**
 * Created by User on 9/11/2016.
 */



public class MessageFromBackPressed {

    public static final int FROM_BACK_PRESSED = 1;
    public static final int FROM_THREAD = 2;
    public final int action;

    public MessageFromBackPressed(int action) {
        this.action = action;
    }
}
