package com.example.user.musicdownloader.recievers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaButtonReceiver;
import android.util.Log;
import android.view.KeyEvent;

import com.example.user.musicdownloader.EventBus.EventToService;

import org.greenrobot.eventbus.EventBus;

public class RemoteControlReceiver extends MediaButtonReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event.getAction() != KeyEvent.ACTION_DOWN) return;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                Log.d("Bluetooth", "KEYCODE_MEDIA_PLAY");
                EventBus.getDefault().post(new EventToService(EventToService.PLAY_BUTTON, 0));
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                Log.d("Bluetooth", "KEYCODE_MEDIA_PAUSE");
                EventBus.getDefault().post(new EventToService(EventToService.PLAY_BUTTON, 0));
                break;
            case KeyEvent.KEYCODE_MEDIA_STOP:
                Log.d("Bluetooth", "KEYCODE_MEDIA_STOP");
                EventBus.getDefault().post(new EventToService(EventToService.PLAY_BUTTON, 0));
                break;
            case KeyEvent.KEYCODE_HEADSETHOOK:
                Log.d("Bluetooth", "KEYCODE_HEADSETHOOK");
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                Log.d("Bluetooth", "KEYCODE_MEDIA_PLAY_PAUSE");
                EventBus.getDefault().post(new EventToService(EventToService.PLAY_BUTTON, 0));
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                Log.d("Bluetooth", "KEYCODE_MEDIA_NEXT");
                EventBus.getDefault().post(new EventToService(EventToService.NEXT_BUTTON, 0));
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                Log.d("Bluetooth == ", "KEYCODE_MEDIA_PREVIOUS");
                EventBus.getDefault().post(new EventToService(EventToService.PREVIOUS_BUTTON, 0));
                break;
        }
    }
}