package com.example.user.musicdownloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaButtonReceiver;
import android.util.Log;
import android.view.KeyEvent;

public class RemoteControlReceiver extends MediaButtonReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("zaq == ","From Blue tooth");
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            Log.d("zaq == ","From Blue tooth");
            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                // Handle key press.
            }
        }
    }
}