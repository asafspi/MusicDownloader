package com.musicplayer.freedownload.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.musicplayer.freedownload.EventBus.EventToService;
import com.musicplayer.freedownload.EventBus.messages.EventForSearchRecyclerView;
import com.musicplayer.freedownload.EventBus.messages.MessageEvent;
import com.musicplayer.freedownload.R;
import com.musicplayer.freedownload.activities.MainActivity;
import com.musicplayer.freedownload.data.Song;
import com.musicplayer.freedownload.tools.ShPref;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static com.musicplayer.freedownload.EventBus.EventToService.KILL_NOTIFICATION;

public class PlaySongService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {
    public static final String EXTRA_ADDED_TO_QUEUE = "extraQueue";

    private static final int MAIN_REQUEST_ID = 999;
    private static final int NOTIFICATION_ID = 1213;
    public static boolean repeatSong;
    public static boolean shuffle;
    private MediaPlayer player;
    private NotificationCompat.Builder builder;
    private RemoteViews contentViewBig, contentViewSmall;
    private NotificationManager manager;
    private PhoneStateListener phoneStateListener;
    private Handler handler;

    public static ArrayList<Song> currentArraySong;
    public static CLIENT client;
    public static Song currentPlayedSong;
    public static int totalSongDuration, currentTimeValue;
    private static boolean addedToQueue;
    private boolean isPreparing;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        registerReceiver();
        handler = new Handler();
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnPreparedListener(this);
        player.setOnSeekCompleteListener(this);
        player.setLooping(false); // Set looping
        player.setVolume(100, 100);
        Log.d("TAG", "PlaySongService created");
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        addedToQueue = intent.getBooleanExtra(EXTRA_ADDED_TO_QUEUE, false);
        if (addedToQueue) {
            if (!player.isPlaying()) {
                songEnded();
            }
        } else {
            setPlayer();
        }

        return Service.START_NOT_STICKY;
    }

    private void setPlayer() {
        if (isPreparing){//we can't prepare while prepare different song
            return;
        }
        if (player.isPlaying()) {
            player.stop();
        }
        try {
            player.reset();
            player.setDataSource(this, currentPlayedSong.getUri());
            player.prepareAsync();
            isPreparing = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        isPreparing = false;
        player.start();
        totalSongDuration = player.getDuration();
        ShPref.put(getString(R.string.current_song_duratoin), player.getDuration());
        EventBus.getDefault().post(new MessageEvent(MessageEvent.EVENT.START_SONG, true));
        addNotification(NOTIFICATION_ID);
        handler.post(updateUi);
        if (client == CLIENT.WEB) {//this is song from search internet results
            currentPlayedSong.setLoadedToPlayer(false);
            EventBus.getDefault().post(new EventForSearchRecyclerView());

        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        handler.post(updateUi);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (client == CLIENT.WEB) {
            player.seekTo(0);
//            stopSelf();
        } else if (repeatSong) {
            player.seekTo(0);
            player.start();
        } else {
            songEnded();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("TAG", "PlaySongService destroyed");

        handler.removeCallbacks(updateUi);
        if (null != player) {
            player.stop();
            player.release();
            EventBus.getDefault().post(new MessageEvent(MessageEvent.EVENT.SONG_END, false));
        }
        EventBus.getDefault().unregister(this);

        manager.cancel(NOTIFICATION_ID);
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }


    Runnable updateUi = new Runnable() {
        @Override
        public void run() {
            if (player.isPlaying()) {
                currentTimeValue = player.getCurrentPosition();
                EventBus.getDefault().post(new MessageEvent(MessageEvent.EVENT.FROM_RUN, player.isPlaying()));
                handler.postDelayed(this, 100);
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventToService event) {
        handler.removeCallbacks(updateUi);
        int position;
        switch (event.action) {
            case 1:// PLAY
                EventBus.getDefault().post(new MessageEvent(MessageEvent.EVENT.PLAY_BTN_CLICKED, !player.isPlaying()));
                if (player.isPlaying()) {
                    player.pause();
                    addNotification(NOTIFICATION_ID);
                } else {
                    player.start();
                    handler.post(updateUi);
                    addNotification(NOTIFICATION_ID);
                }
                break;
            case 2: // NEXT_BUTTON
                songEnded();
                break;
            case 3: //PREVIOUS_BUTTON
                position = currentArraySong.indexOf(currentPlayedSong) - 1;
                playSongInPosition(position);
                break;
            case 4: // From seek bar
                player.seekTo(event.seekTo);
                break;
            case 5:
                manager.cancel(NOTIFICATION_ID);
                stopSelf();
                break;
        }
    }

    private void songEnded() {
        int position;
        if (addedToQueue) {
            position = 0;
            addedToQueue = false;
        } else if (shuffle) {
            position = new Random().nextInt(currentArraySong.size());
        } else {
            int index = currentArraySong.indexOf(currentPlayedSong);
            if (index != -1) {
                position = ++index;
            } else {
                position = index;
            }
        }
        playSongInPosition(position);
    }

    private void playSongInPosition(int position) {
        if (position >= 0 && position < currentArraySong.size()) {
            currentPlayedSong = currentArraySong.get(position);
            setPlayer();
        } else {
            EventBus.getDefault().post(new MessageEvent(MessageEvent.EVENT.SONG_END, false));
            player.seekTo(0);
//            stopSelf();
        }
    }


    private void addNotification(int id) {

        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.mp3_icon_notif).setOngoing(true) // Again,
                .setContentTitle("Title").setContentText("Text")
                .setPriority(NotificationCompat.PRIORITY_MAX);


        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), MAIN_REQUEST_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT); //123
        builder.setContentIntent(contentIntent);
        contentViewSmall = new RemoteViews(getPackageName(), R.layout.custom_notification_small);
        contentViewBig = new RemoteViews(getPackageName(), R.layout.custom_notification);
        builder.setCustomContentView(contentViewSmall);
        if (null != player && player.isPlaying()) {
            contentViewSmall.setImageViewResource(R.id.playNotificationImage, R.drawable.pause_notification);
        } else {
            contentViewSmall.setImageViewResource(R.id.playNotificationImage, R.drawable.play_notification);
        }
        contentViewSmall.setTextViewText(R.id.textViewNotification, currentPlayedSong.getName());
        builder.setCustomBigContentView(contentViewBig);
        contentViewBig.setTextViewText(R.id.textViewNotification, currentPlayedSong.getName());

        setNextNotificationButton();
        setPreviewsNotificationButton();
        setPlayNotificationButton();
        setPauseNotificationButton();
        setExitNotificationButton();

        // Add as notification
        manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id, builder.build());
    }

    private void setNextNotificationButton() {
        Intent nextIntent = new Intent(this, NextButtonListener.class);
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
        contentViewBig.setOnClickPendingIntent(R.id.nextNotification, pendingNextIntent);
        contentViewSmall.setOnClickPendingIntent(R.id.nextNotification, pendingNextIntent);
    }

    private void setPreviewsNotificationButton() {
        Intent previewsIntent = new Intent(this, PreviewsButtonListener.class);
        PendingIntent pendingPreviewsIntent = PendingIntent.getBroadcast(this, 0, previewsIntent, 0);
        contentViewBig.setOnClickPendingIntent(R.id.previous_notification, pendingPreviewsIntent);
        contentViewSmall.setOnClickPendingIntent(R.id.previous_notification, pendingPreviewsIntent);
    }

    private void setPlayNotificationButton() {
        Intent PlayPauseIntent = new Intent(this, PlayPauseButtonListener.class);
        PendingIntent pendingPlayPausesIntent = PendingIntent.getBroadcast(this, 0, PlayPauseIntent, 0);
        contentViewBig.setOnClickPendingIntent(R.id.playNotificationImage, pendingPlayPausesIntent);
        contentViewSmall.setOnClickPendingIntent(R.id.playNotificationImage, pendingPlayPausesIntent);
    }

    private void setPauseNotificationButton() {
        Intent PauseIntent = new Intent(this, PauseButtonListener.class);
        PendingIntent pendingPausesIntent = PendingIntent.getBroadcast(this, 0, PauseIntent, 0);
        contentViewBig.setOnClickPendingIntent(R.id.pauseNotificationButton, pendingPausesIntent);
    }

    private void setExitNotificationButton() {
        Intent ExitIntent = new Intent(this, ExitButtonListener.class);
        PendingIntent pendingExitIntent = PendingIntent.getBroadcast(this, 0, ExitIntent, 0);
        contentViewBig.setOnClickPendingIntent(R.id.x_notification, pendingExitIntent);
        contentViewSmall.setOnClickPendingIntent(R.id.x_notification, pendingExitIntent);
    }


    public static class NextButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.getDefault().post(new EventToService(EventToService.NEXT_BUTTON));
            Log.d("zaq", "from notification");

        }
    }

    public static class PreviewsButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.getDefault().post(new EventToService(EventToService.PREVIOUS_BUTTON));
            Log.d("zaq", "from notification");
        }
    }

    public static class PlayPauseButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.getDefault().post(new EventToService(EventToService.PLAY_BUTTON));
            Log.d("zaq", "from notification");
        }
    }

    public static class PauseButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.getDefault().post(new EventToService(EventToService.PLAY_BUTTON));
            Log.d("zaq", "from notification");
        }
    }

    public static class ExitButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.getDefault().post(new EventToService(KILL_NOTIFICATION));
            EventBus.getDefault().post(new MessageEvent(MessageEvent.EVENT.FINISH));
            Log.d("zaq", "from notification exit");
        }
    }

    private void registerReceiver() {

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    player.pause();
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                    //player.start();
                    //EventBus.getDefault().post(new MessageEvent("changePlayPauseButtonToPause", 0, 0, null, null, null));
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                    player.pause();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

    }

    public enum CLIENT {
        SONGS,
        ALBUMS,
        ARTIST,
        DOWNLOAD,
        WEB
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }
}