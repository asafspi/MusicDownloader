package com.example.user.musicdownloader.activities;

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

import com.example.user.musicdownloader.EventBus.EventToService;
import com.example.user.musicdownloader.EventBus.messages.EventForSearchRecyclerView;
import com.example.user.musicdownloader.EventBus.messages.MessageEvent;
import com.example.user.musicdownloader.EventBus.messages.MessageFromBackPressed;
import com.example.user.musicdownloader.EventBus.messages.MessageSearch;
import com.example.user.musicdownloader.EventBus.messages.MessageSearchOnline;
import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.adapters.MusicPlayerPagerAdapter;
import com.example.user.musicdownloader.data.GetMusicData;
import com.example.user.musicdownloader.data.Song;
import com.example.user.musicdownloader.recievers.RemoteControlReceiver;
import com.example.user.musicdownloader.services.PlaySongService;
import com.example.user.musicdownloader.tools.PermissionChecker;
import com.example.user.musicdownloader.tools.ShPref;
import com.example.user.musicdownloader.tools.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.example.user.musicdownloader.data.GetMusicData.songs;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, SearchView.OnQueryTextListener, ViewPager.OnPageChangeListener {

    public static final int FROM_BACK_PRESSED = 1;
    public static final int FROM_ADAPTER_ARTIST = 2;
    public static final int FROM_ADAPTER_ALBUM = 3;
    public static final int CODE_WRITE_BT_SETTINGS_PERMISSION = 100;
    public static String query;
    public static long downId;
    public static String pathId;

    public ViewPager mViewPager;
    private ImageButton playPause;
    private TextView songNameTextView, artistNameTextView, songDuration, runningTime;
    private SeekBar mainSeekBar;
    private SearchView searchView;
    private AudioManager mAudioManager;
    private ComponentName mRemoteControlResponder;
    public static Song requestedBTSong;



    //http://android-developers.blogspot.co.il/2010/06/allowing-applications-to-play-nicer.html
    private static Method mRegisterMediaButtonEventReceiver;
    private static Method mUnregisterMediaButtonEventReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (PermissionChecker.isPermissionsGranted(permissions)) {
            GetMusicData.getAllSongs(this);
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
        MusicPlayerPagerAdapter mSectionsPagerAdapter = new MusicPlayerPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        long timeInSeconds;
        int sec, min;
        switch (event.message) {
            case "start song":
                mainSeekBar.setProgress(0);
                mainSeekBar.setMax(event.songDuration);
                songNameTextView.setText(event.songName);
                ShPref.put(getString(R.string.song_name_for_service), songNameTextView.getText().toString());
                artistNameTextView.setText(event.songArtist + " -");
                ShPref.put(R.string.song_artist_for_service, artistNameTextView.getText().toString());
                songNameTextView.setSelected(true);
                songNameTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                songNameTextView.setSingleLine(true);

                timeInSeconds = event.songDuration / 1000;
                sec = (int) (timeInSeconds % 60);
                min = (int) ((timeInSeconds / 60)) % 60;
                if (sec < 10) {
                    songDuration.setText(String.valueOf(min + ":" + "0" + sec));
                } else {
                    songDuration.setText(String.valueOf(min + ":" + sec));
                }
                break;
            case "from run":
                timeInSeconds = event.currentDuration / 1000;
                sec = (int) (timeInSeconds % 60);
                min = (int) ((timeInSeconds / 60)) % 60;
                mainSeekBar.setProgress(event.currentDuration);
                if (sec < 10) {
                    EventBus.getDefault().post(new EventForSearchRecyclerView(0));
                    runningTime.setText(String.valueOf(min + ":" + "0" + sec));
                } else {
                    runningTime.setText(String.valueOf(min + ":" + sec));
                }
                break;
            case "song ends":
                mainSeekBar.setProgress(0);
                //mainSeekBar.setProgress(event.currentDuration);
                break;
            case "nextSong":
                Utils.changeSong(1);
                break;
            case "previousSong":
                Utils.changeSong(-1);
                break;
            case "finish":
                finish();
                break;
            case "changePlayPauseButtonToPause":
                playPause.setImageResource(R.drawable.pause_icon);
                break;
            case "changePlayPauseButtonToPlay":
                playPause.setImageResource(R.drawable.play_icon);
                break;
        }
    }


    private void setVies() {
        ImageButton nextSong = (ImageButton) findViewById(R.id.nextButtonImageView);
        nextSong.setOnClickListener(this);
        ImageButton priviesSong = (ImageButton) findViewById(R.id.previuseImageView);
        priviesSong.setOnClickListener(this);
        playPause = (ImageButton) findViewById(R.id.playPauseButtonImageButton);
        playPause.setOnClickListener(this);
        ImageButton shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        shuffleButton.setOnClickListener(this);
        ImageButton repeatButton = (ImageButton) findViewById(R.id.repeatButton);
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

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        mAudioManager.registerMediaButtonEventReceiver(
                mRemoteControlResponder);
        searchView.setOnQueryTextListener(this);

    }

    private void changeSong(int i) {
        int position = 2;
        switch (i) {
            case 1:
                position = GetMusicData.getSongPosition(PlaySongService.songName) + 1;
                break;
            case -1:
                position = GetMusicData.getSongPosition(PlaySongService.songName) - 1;
                break;
        }
        ShPref.put(R.string.song_path_for_service, songs.get(position).getUri().toString());
        ShPref.put(R.string.song_name_for_service, songs.get(position).getName());
        ShPref.put(R.string.song_artist_for_service, songs.get(position).getArtist());
        ShPref.put(R.string.song_thumb_for_service, songs.get(position).getImage().toString());
        stopService(new Intent(this, PlaySongService.class));
        startService(new Intent(this, PlaySongService.class));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playPauseButtonImageButton:
                playPause.setImageResource(R.drawable.play_icon);
                songNameTextView.setSelected(true);
                songNameTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                songNameTextView.setSingleLine(true);
                EventBus.getDefault().post(new EventToService(EventToService.PLAY_BUTTON, 0));
                break;
            case R.id.nextButtonImageView:
                changeSong(1);
                break;
            case R.id.previuseImageView:
                changeSong(-1);
                break;
            case R.id.searchView:
                Log.d("zaq", "From search button");
                break;
            case R.id.shuffleButton:
                PlaySongService.shuffle = !PlaySongService.shuffle;
                if(PlaySongService.shuffle){
                    Toast.makeText(this, "Shuffle mode is on", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Shuffle mode is off", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.repeatButton:
                PlaySongService.repeatSong = !PlaySongService.repeatSong;
                if(PlaySongService.repeatSong){
                    Toast.makeText(this, "Repeat mode is on", Toast.LENGTH_SHORT).show();
                }else {
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


    private void registerRemoteControl() {
        try {
            if (mRegisterMediaButtonEventReceiver == null) {
                return;
            }
            mRegisterMediaButtonEventReceiver.invoke(mAudioManager,
                    mRemoteControlResponder);
        } catch (InvocationTargetException ite) {
            /* unpack original exception when possible */
            Throwable cause = ite.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                /* unexpected checked exception; wrap and re-throw */
                throw new RuntimeException(ite);
            }
        } catch (IllegalAccessException ie) {
            Log.e("zaq", "unexpected " + ie);
        }
    }

    private void unregisterRemoteControl() {
        try {
            if (mUnregisterMediaButtonEventReceiver == null) {
                return;
            }
            mUnregisterMediaButtonEventReceiver.invoke(mAudioManager,
                    mRemoteControlResponder);
        } catch (InvocationTargetException ite) {
            /* unpack original exception when possible */
            Throwable cause = ite.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                /* unexpected checked exception; wrap and re-throw */
                throw new RuntimeException(ite);
            }
        } catch (IllegalAccessException ie) {
            System.err.println("unexpected " + ie);
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
        EventBus.getDefault().unregister(this);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioManager.unregisterMediaButtonEventReceiver(
                mRemoteControlResponder);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("zaq == ", "onNewIntent");
        songNameTextView.setText(ShPref.getString(R.string.song_name_for_service, ""));
        artistNameTextView.setText(ShPref.getString(R.string.song_artist_for_service, ""));
        super.onNewIntent(intent);
    }

/*
    public void showMenu(View view) {
        PopupMenu popupMenu;
        popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(this);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_main, popupMenu.getMenu());
        popupMenu.show();

    }
*/

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
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (query == null || query.length() == 0) {
            MainActivity.query = null;
            return true;
        }
        MainActivity.query = query;

        if (mViewPager.getCurrentItem() == 4){
                EventBus.getDefault().post(new MessageSearchOnline(query));
        } else {
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
        mViewPager.setCurrentItem(4);
    }


}
