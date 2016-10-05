package com.musicplayer.freedownload.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.musicplayer.freedownload.EventBus.EventToService;
import com.musicplayer.freedownload.EventBus.messages.MessageEvent;
import com.musicplayer.freedownload.EventBus.messages.MessageFromBackPressed;
import com.musicplayer.freedownload.EventBus.messages.MessageSearch;
import com.musicplayer.freedownload.EventBus.messages.MessageSearchOnline;
import com.musicplayer.freedownload.R;
import com.musicplayer.freedownload.adapters.MusicPlayerPagerAdapter;
import com.musicplayer.freedownload.data.GetMusicData;
import com.musicplayer.freedownload.data.Song;
import com.musicplayer.freedownload.recievers.RemoteControlReceiver;
import com.musicplayer.freedownload.services.PlaySongService;
import com.musicplayer.freedownload.tools.PermissionChecker;
import com.musicplayer.freedownload.tools.ShPref;
import com.musicplayer.freedownload.tools.Utils;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Method;
import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.musicplayer.freedownload.services.PlaySongService.currentPlayedSong;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, SearchView.OnQueryTextListener, ViewPager.OnPageChangeListener {

    public static final int FROM_BACK_PRESSED = 1;
    public static final int FROM_ADAPTER_ARTIST = 2;
    public static final int FROM_ADAPTER_ALBUM = 3;
    public static final int CODE_WRITE_BT_SETTINGS_PERMISSION = 100;
    public static String query;
    public static long downId;
    public static String pathId;
    public static Song requestedBTSong;

    public ViewPager mViewPager;
    private ImageButton playPause;
    private TextView songNameTextView, artistNameTextView, songDuration, runningTime;
    private SeekBar mainSeekBar;
    private SearchView searchView;
    private AudioManager mAudioManager;
    private ComponentName mRemoteControlResponder;
    private ImageButton repeatButton, shuffleButton;

    private String placeHolderSongName;

    //http://android-developers.blogspot.co.il/2010/06/allowing-applications-to-play-nicer.html
    private static Method mRegisterMediaButtonEventReceiver;
    private static Method mUnregisterMediaButtonEventReceiver;
    private boolean songPlaySet;
    private String startAppId = "208673432";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StartAppSDK.init(this, startAppId, true);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (PermissionChecker.isPermissionsGranted(permissions)) {
            GetMusicData.getAllSongs(getContentResolver(), getString(R.string.app_name));
            Log.d("zaq", "Permissions granted");
        } else {
            PermissionsActivity.startActivityForResult(this, PermissionsActivity.REQUEST_CODE_PERMISSION_WRITE_SETTINGS, permissions);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused){
                    mViewPager.post(new Runnable() {
                        @Override
                        public void run() {
                            int position = mViewPager.getCurrentItem();
                            if (position == 1 || position == 2 ) {
                                mViewPager.setCurrentItem(0);
                            }
                        }
                    });
                }
            }
        });
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mViewPager = (ViewPager) findViewById(R.id.container);

        MusicPlayerPagerAdapter mSectionsPagerAdapter = new MusicPlayerPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        setVies();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mRemoteControlResponder = new ComponentName(getPackageName(),
                RemoteControlReceiver.class.getName());
        initializeRemoteControlRegistrationMethods();
    }

    private void setVies() {
        placeHolderSongName = getString(R.string.place_holder_current_time);
        ImageButton nextSong = (ImageButton) findViewById(R.id.nextButtonImageView);
        nextSong.setOnClickListener(this);
        ImageButton priviesSong = (ImageButton) findViewById(R.id.previuseImageView);
        priviesSong.setOnClickListener(this);
        playPause = (ImageButton) findViewById(R.id.playPauseButtonImageButton);
        playPause.setOnClickListener(this);
        shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        shuffleButton.setOnClickListener(this);
        repeatButton = (ImageButton) findViewById(R.id.repeatButton);
        repeatButton.setOnClickListener(this);
        //thumbSongImageView = (ImageView) findViewById(R.id.playerImageView);
        mainSeekBar = (SeekBar) findViewById(R.id.seekBar);
        songNameTextView = (TextView) findViewById(R.id.songNameTextView);
        artistNameTextView = (TextView) findViewById(R.id.artistNameTextView);
        songDuration = (TextView) findViewById(R.id.totalTime);
        runningTime = (TextView) findViewById(R.id.runningTime);
        mainSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mainSeekBar.setProgress(0);
        mainSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                EventBus.getDefault().post(new EventToService(EventToService.SEEK_TO, seekBar.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                EventBus.getDefault().post(new EventToService(EventToService.SEEK_TO, seekBar.getProgress()));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        long timeInSeconds;
        int sec, min;
        switch (event.getEvent()) {
            case START_SONG:
                setSongUi(event.isPlaying());
                break;
            case FROM_RUN:
                if (!songPlaySet){
                    setSongUi(event.isPlaying());
                }
                timeInSeconds = PlaySongService.currentTimeValue / 1000;
                sec = (int) (timeInSeconds % 60);
                min = (int) ((timeInSeconds / 60)) % 60;
                mainSeekBar.setProgress(PlaySongService.currentTimeValue);
                if (sec < 10) {
                    runningTime.setText(String.valueOf(min + ":" + "0" + sec));
                } else {
                    runningTime.setText(String.valueOf(min + ":" + sec));
                }
                break;
            case SONG_END:
                mainSeekBar.setProgress(0);
                playPause.setImageResource(R.drawable.pause_icon);
                break;
            case PLAY_BTN_CLICKED:
                playPause.setImageResource(event.isPlaying() ? R.drawable.pause_icon : R.drawable.play_icon);
                break;
            case FINISH:
                finish();
                break;
        }
    }

    private void setSongUi(boolean isPlaying) {
        songPlaySet = true;
        playPause.setImageResource(isPlaying ? R.drawable.pause_icon : R.drawable.play_icon);
        mainSeekBar.setMax(PlaySongService.totalSongDuration);
        songNameTextView.setText(currentPlayedSong.getName());
        artistNameTextView.setText(String.format(placeHolderSongName, currentPlayedSong.getArtist()));
        songNameTextView.setSelected(true);
        songNameTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        songNameTextView.setSingleLine(true);
        int sec, min;
        long timeInSeconds;
        timeInSeconds = PlaySongService.totalSongDuration / 1000;
        sec = (int) (timeInSeconds % 60);
        min = (int) ((timeInSeconds / 60)) % 60;
        if (sec < 10) {
            songDuration.setText(String.valueOf(min + ":" + "0" + sec));
        } else {
            songDuration.setText(String.valueOf(min + ":" + sec));
        }
    }




    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        mAudioManager.registerMediaButtonEventReceiver(
                mRemoteControlResponder);
        searchView.setOnQueryTextListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playPauseButtonImageButton:
                playPause.setImageResource(R.drawable.play_icon);
                songNameTextView.setSelected(true);
                songNameTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                songNameTextView.setSingleLine(true);
                EventBus.getDefault().post(new EventToService(EventToService.PLAY_BUTTON));
                break;
            case R.id.nextButtonImageView:
                EventBus.getDefault().post(new EventToService(EventToService.NEXT_BUTTON));
                break;
            case R.id.previuseImageView:
                EventBus.getDefault().post(new EventToService(EventToService.PREVIOUS_BUTTON));
                break;
            case R.id.searchView:
                Log.d("zaq", "From search button");
                break;
            case R.id.shuffleButton:
                PlaySongService.shuffle = !PlaySongService.shuffle;
                if(PlaySongService.shuffle){
                    shuffleButton.setImageResource(R.drawable.shuffle_pressed_icon);
                    Toast.makeText(this, "Shuffle mode is on", Toast.LENGTH_SHORT).show();
                }else {
                    shuffleButton.setImageResource(R.drawable.shuffle_icon);
                    Toast.makeText(this, "Shuffle mode is off", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.repeatButton:
                PlaySongService.repeatSong = !PlaySongService.repeatSong;
                if(PlaySongService.repeatSong){
                    repeatButton.setImageResource(R.drawable.repeat_pressed_icon);
                    Toast.makeText(this, "Repeat mode is on", Toast.LENGTH_SHORT).show();
                }else {
                    repeatButton.setImageResource(R.drawable.repeat_icon);
                    Toast.makeText(this, "Repeat mode is off", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private static void initializeRemoteControlRegistrationMethods() {
        try {
            if (mRegisterMediaButtonEventReceiver == null) {
                mRegisterMediaButtonEventReceiver = AudioManager.class.getMethod(
                        "registerMediaButtonEventReceiver",
                        new Class[]{ComponentName.class});
            }
            if (mUnregisterMediaButtonEventReceiver == null) {
                mUnregisterMediaButtonEventReceiver = AudioManager.class.getMethod(
                        "unregisterMediaButtonEventReceiver",
                        new Class[]{ComponentName.class});
            }
      /* success, this device will take advantage of better remote */
      /* control event handling                                    */
        } catch (NoSuchMethodException nsme) {
      /* failure, still using the legacy behavior, but this app    */
      /* is future-proof!                                          */
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()){
            searchView.setQuery("", false);
            searchView.setIconified(true);
            return;
        }
        int position = mViewPager.getCurrentItem();
        switch (position) {
            case 3:
            case 0:
                StartAppAd.onBackPressed(this);
                finish();
                break;
            case 1:
            case 2://no break!!
                EventBus.getDefault().post(new MessageFromBackPressed(MessageFromBackPressed.FROM_BACK_PRESSED, position));
                break;
        }
    }



    @Override
    public void onStop() {
        super.onStop();
        songPlaySet = false;
        EventBus.getDefault().unregister(this);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioManager.unregisterMediaButtonEventReceiver(
                mRemoteControlResponder);
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("zaq == ", "onNewIntent");
        songNameTextView.setText(ShPref.getString(R.string.song_name_for_service, ""));
        artistNameTextView.setText(ShPref.getString(R.string.song_artist_for_service, ""));
        super.onNewIntent(intent);
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        EventBus.getDefault().post(new MessageSearchOnline(query));
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        MainActivity.query = query;
        if (mViewPager.getCurrentItem() == 0){
            ArrayList<Song> querySongs = new ArrayList<>();
            @SuppressWarnings("unchecked")
            ArrayList<Song> songs = (ArrayList<Song>) GetMusicData.songs.clone();
            for (Song song : songs){
                if (song.getName().toLowerCase().contains(query.toLowerCase())
                        || song.getArtist().toLowerCase().contains(query.toLowerCase())
                        || song.getAlbum().toLowerCase().contains(query.toLowerCase())) {
                    querySongs.add(song);
                }
            }
            EventBus.getDefault().post(new MessageSearch(querySongs, query));
        }
        return true;
    }


    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        switch (i){
            case 0:
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                break;
            case 4:
                searchView.setIconified(false);
                searchView.setFocusable(true);
                InputMethodManager imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }


    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_WRITE_BT_SETTINGS_PERMISSION  && Settings.System.canWrite(this)){
            Log.d("TAG", "CODE_WRITE_BT_SETTINGS_PERMISSION success");
            if (requestedBTSong != null) {
                Utils.setSongAsRingtone(this, requestedBTSong);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_WRITE_BT_SETTINGS_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestedBTSong != null) {
                Utils.setSongAsRingtone(this, requestedBTSong);
            }
        }
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageSearchOnline event) {
        if (mViewPager.getCurrentItem() != 4) {
            mViewPager.setCurrentItem(4);
            EventBus.getDefault().post(new MessageSearchOnline(query));
        }

    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageFromBackPressed event) {
        switch (event.getAction()) {
            case MessageFromBackPressed.FROM_THREAD:  //
                MusicPlayerPagerAdapter mSectionsPagerAdapter = new MusicPlayerPagerAdapter(getSupportFragmentManager());
                mViewPager.setAdapter(null);
                mViewPager.setAdapter(mSectionsPagerAdapter);
                break;
        }
    }
}
