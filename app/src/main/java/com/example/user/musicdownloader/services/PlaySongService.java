package com.example.user.musicdownloader.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.user.musicdownloader.EventBus.EventToService;
import com.example.user.musicdownloader.EventBus.messages.EventForSearchRecyclerView;
import com.example.user.musicdownloader.EventBus.messages.MessageEvent;
import com.example.user.musicdownloader.data.GetMusicData;
import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.tools.ShPref;
import com.example.user.musicdownloader.activities.MainActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Random;

import static com.example.user.musicdownloader.data.GetMusicData.songs;

public class PlaySongService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {
    private static final String TAG = null;
    private static final int MAIN_REQUEST_ID = 999;
    private static final int NOTIFICATION_ID = 1213;
    public static boolean repeatSong;
    public static boolean shuffle;
    private MediaPlayer player;
    public static String songName;
    private NotificationCompat.Builder builder;
    private RemoteViews contentViewBig, contentViewSmall;
    private NotificationManager manager;
    private PhoneStateListener phoneStateListener;
    private Handler handler;
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        registerReceiver();
        songName = ShPref.getString(R.string.song_name_for_service, "");
        handler = new Handler();
        String songPath = ShPref.getString(R.string.song_path_for_service, "");
        Uri songUri = Uri.parse(songPath);
        EventBus.getDefault().post(new MessageEvent("changePlayPauseButtonToPlay", 0, 0, null, null, null));

//        player = MediaPlayer.create(this, songUri);
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnPreparedListener(this);
        player.setOnSeekCompleteListener(this);
        player.setLooping(false); // Set looping
        player.setVolume(100, 100);
        setPlayer(songUri);

    }

    private void setPlayer(Uri songUri){
        if (player.isPlaying()){
            player.stop();
        }
        try {
            player.setDataSource(this, songUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.prepareAsync();
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

    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(this);
        if (null != player) {
            player.stop();
            player.release();
            EventBus.getDefault().post(new MessageEvent("changePlayPauseButtonToPlay", 0, 0, null, null, null));
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
                EventBus.getDefault().post(new MessageEvent("from run", 0, player.getCurrentPosition(), "", "", ""));
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
                if (player.isPlaying()) {
                    player.pause();
                    addNotification(NOTIFICATION_ID);
                } else {
                    player.start();
                    handler.post(updateUi);
                    EventBus.getDefault().post(new MessageEvent("changePlayPauseButtonToPause", player.getCurrentPosition(), 0, null, null, null));
                    addNotification(NOTIFICATION_ID);
                }
                break;
            case 2: // NEXT_BUTTON
                if (null != player) {
                    player.stop();
                    EventBus.getDefault().post(new MessageEvent("changePlayPauseButtonToPlay", 0, 0, null, null, null));
                }
                if (!shuffle) {
                    position = GetMusicData.getSongPosition(ShPref.getString(R.string.song_name_for_service, "")) + 1;
                } else {
                    position = new Random().nextInt(GetMusicData.songs.size());
                }

                setPlayer(songs.get(position).getUri());
                EventBus.getDefault().post(new MessageEvent("start song", player.getDuration(), 0, GetMusicData.songs.get(position).getName(), GetMusicData.songs.get(position).getArtist(), GetMusicData.songs.get(position).getUri().toString()));
                ShPref.put(R.string.song_path_for_service, songs.get(position).getUri().toString());
                ShPref.put(R.string.song_name_for_service, songs.get(position).getName());
                ShPref.put(R.string.song_artist_for_service, songs.get(position).getArtist());
                ShPref.put(R.string.song_thumb_for_service, songs.get(position).getImage().toString());
                addNotification(NOTIFICATION_ID);
                break;
            case 3: //PREVIOUS_BUTTON
                if (null != player) {
                    player.stop();
                }
                position = GetMusicData.getSongPosition(ShPref.getString(R.string.song_name_for_service, "")) - 1;
                setPlayer(songs.get(position).getUri());
                EventBus.getDefault().post(new MessageEvent("start song", player.getDuration(), 0, GetMusicData.songs.get(position).getName(), GetMusicData.songs.get(position).getArtist(), GetMusicData.songs.get(position).getUri().toString()));
                ShPref.put(R.string.song_path_for_service, songs.get(position).getUri().toString());
                ShPref.put(R.string.song_name_for_service, songs.get(position).getName());
                ShPref.put(R.string.song_artist_for_service, songs.get(position).getArtist());
                ShPref.put(R.string.song_thumb_for_service, songs.get(position).getImage().toString());
                addNotification(NOTIFICATION_ID);
                break;
            case 4: // From seek bar
                player.seekTo(event.seekTo);
            case 5:
                manager.cancel(NOTIFICATION_ID);
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (!repeatSong) {
            EventBus.getDefault().post(new EventToService(EventToService.NEXT_BUTTON, 0));
            addNotification(NOTIFICATION_ID);
        } else {
            player.seekTo(0);
            player.start();
        }
    }

    private void addNotification(int id) {
        songName = ShPref.getString(R.string.song_name_for_service, "");
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play).setOngoing(true) // Again,
                .setContentTitle("Title").setContentText("Text")
                .setPriority(NotificationCompat.PRIORITY_MAX);


        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), MAIN_REQUEST_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT); //123
        builder.setContentIntent(contentIntent);
        contentViewSmall = new RemoteViews(getPackageName(), R.layout.custom_notification_small);
        contentViewBig = new RemoteViews(getPackageName(), R.layout.custom_notification);
        builder.setCustomContentView(contentViewSmall);
        if (null!= player && player.isPlaying()) {
            contentViewSmall.setImageViewResource(R.id.playNotificationImage, android.R.drawable.ic_media_pause);
        } else {
            contentViewSmall.setImageViewResource(R.id.playNotificationImage, R.drawable.play_notification);
        }
        contentViewSmall.setTextViewText(R.id.textViewNotification, songName);
        builder.setCustomBigContentView(contentViewBig);
        contentViewBig.setTextViewText(R.id.textViewNotification, songName);

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
        contentViewBig.setOnClickPendingIntent(R.id.playNotification, pendingPlayPausesIntent);
        contentViewSmall.setOnClickPendingIntent(R.id.playNotification, pendingPlayPausesIntent);
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

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (null != player) {
            player.start();
            String songArtist = ShPref.getString(R.string.song_artist_for_service, "");
            String songImage = ShPref.getString(R.string.song_thumb_for_service, "");
            String songPath = ShPref.getString(R.string.song_path_for_service, "");
            EventBus.getDefault().post(new MessageEvent("changePlayPauseButtonToPause", 0, 0, null, null, null));
            ShPref.put(getString(R.string.current_song_duratoin), player.getDuration());
            EventBus.getDefault().post(new MessageEvent("start song", player.getDuration(), 0, songName, songArtist, songPath));
            if (null == builder) {
                addNotification(NOTIFICATION_ID);
            }
            handler.post(updateUi);
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        handler.post(updateUi);
    }

    public static class NextButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //EventBus.getDefault().post(new MessageEvent("nextSong", 0, 0, "", "", ""));
            EventBus.getDefault().post(new EventToService(EventToService.NEXT_BUTTON, 0));
            Log.d("zaq", "from notification");

        }
    }

    public static class PreviewsButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //EventBus.getDefault().post(new MessageEvent("previousSong", 0, 0, "", "", ""));
            EventBus.getDefault().post(new EventToService(EventToService.PREVIOUS_BUTTON, 0));
            Log.d("zaq", "from notification");
        }
    }

    public static class PlayPauseButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.getDefault().post(new EventToService(EventToService.PLAY_BUTTON, 0));
            Log.d("zaq", "from notification");
        }
    }

    public static class PauseButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.getDefault().post(new EventToService(EventToService.PLAY_BUTTON, 0));
            Log.d("zaq", "from notification");
        }
    }

    public static class ExitButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.getDefault().post(new EventToService(EventToService.KILL_NOTIFICATION, 0));
            context.stopService(new Intent(context, PlaySongService.class));
            EventBus.getDefault().post(new MessageEvent("finish", 0, 0, null, null, null));
            Process.killProcess(Process.myPid());
            Log.d("zaq", "from notification exit");
        }
    }
}