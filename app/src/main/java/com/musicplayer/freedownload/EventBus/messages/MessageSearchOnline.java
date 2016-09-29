package com.musicplayer.freedownload.EventBus.messages;

/**
 * Created by B.E.L on 20/09/2016.
 */

public class MessageSearchOnline {
    private final String query;

    public MessageSearchOnline(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
