package com.example.user.musicdownloader.EventBus;



public class EventToService {

    public int action;
    public int seekTo;

    public static int PLAY_BUTTON = 1;
    public static int NEXT_BUTTON = 2;
    public static int PREVIOUS_BUTTON = 3;
    public static int SEEK_TO = 4;
    public static int KILL_NOTIFICATION = 5;

    public EventToService(int action, int seekTo) {
        this.action = action;
        this.seekTo = seekTo;
    }
}
