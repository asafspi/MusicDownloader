package com.example.user.musicdownloader.EventBus;

import android.content.Intent;

/**
 * Created by User on 9/11/2016.
 */



public class MessageFromBackPressed {

    public static Integer FROM_BACK_PRESSED = 1;
    public static Integer FROM_THREAD = 2;
    public final int action;

    public MessageFromBackPressed(int action) {
        this.action = action;
    }
}
