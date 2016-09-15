package com.example.user.musicdownloader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.user.musicdownloader.EventBus.EventToService;
import com.example.user.musicdownloader.EventBus.MessageEvent;
import com.example.user.musicdownloader.EventBus.MessageFromBackPressed;
import com.example.user.musicdownloader.adapters.ArtistsAdapter;
import com.example.user.musicdownloader.adapters.SongsAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.example.user.musicdownloader.GetMusicData.getAllSongs;
import static com.example.user.musicdownloader.GetMusicData.songs;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    public ViewPager mViewPager;
    private int currentPosition;
    private ImageButton nextSong, priviesSong, playPause;
    private TextView songNameTextView;
    private TextView artistNameTextView;
    private ImageView thumbSongImageView;
    private SeekBar mainSeekBar;
    private SearchView searchView;
    private Toolbar toolbar;
    public static int FROM_BACK_PRESSED = 1;
    public static int FROM_ADAPTER_ARTIST = 2;
    public static int FROM_ADAPTER_ALBUM = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (PermissionChecker.isPermissionsGranted(permissions)) {
            GetMusicData.getAllSongs(this);
            Log.d("zaq", "Permissions granted");
        } else {
            PermissionsActivity.startActivityForResult(this, PermissionsActivity.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE, permissions);
        }
        ((MyApplication) getApplication()).setMainActivity(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SearchView searchView = (SearchView)findViewById(R.id.searchView);
        searchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mViewPager.setCurrentItem(0);
                        break;
                }
                return true;
            }
        });
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        setVies();
        audioFocus();
    }

    private void audioFocus() {
        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        am.getMode();

        final AudioManager finalAm = am;
        AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {

            public void onAudioFocusChange(int focusChange) {
                Log.d("new", "focus: " + focusChange + " mode " + finalAm.getMode() + " is music " + finalAm.isMusicActive());


                if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
                    Log.d("new  ", "focus:started trans ");

                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) {
                    Log.d("new  ", "focus:started duck ");

                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    Log.d("new", "focus:paused  ");
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    Log.d("new", "focus:started ");
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    Log.d("new", "focus:stoped  ");
                    // am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                    // am.abandonAudioFocus(afChangeListener);
                    // Stop playback
                }
            }
        };
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("zaq == ", "onNewIntent");
        songNameTextView.setText(ShPref.getString(R.string.song_name_for_service, ""));
        artistNameTextView.setText(ShPref.getString(R.string.song_artist_for_service, ""));
        super.onNewIntent(intent);
    }


    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.message) {
            case "start song":
                mainSeekBar.setProgress(0);
                mainSeekBar.setMax(event.songDuration);
                songNameTextView.setText(event.songName);
                ShPref.put(getString(R.string.song_name_for_service), songNameTextView.getText().toString());
                artistNameTextView.setText(event.songArtist);
                ShPref.put(R.string.song_artist_for_service, artistNameTextView.getText().toString());
                songNameTextView.setSelected(true);
                songNameTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                songNameTextView.setSingleLine(true);
                break;
            case "from run":
                mainSeekBar.setProgress(event.currentDuration);
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
                playPause.setImageResource(android.R.drawable.ic_media_pause);
                break;
            case "changePlayPauseButtonToPlay":
                playPause.setImageResource(android.R.drawable.ic_media_play);
                break;
        }
    }

    private void setVies() {
        nextSong = (ImageButton) findViewById(R.id.nextButtonImageView);
        nextSong.setOnClickListener(this);
        priviesSong = (ImageButton) findViewById(R.id.previuseImageView);
        priviesSong.setOnClickListener(this);
        playPause = (ImageButton) findViewById(R.id.playPauseButtonImageButton);
        playPause.setOnClickListener(this);
        //thumbSongImageView = (ImageView) findViewById(R.id.playerImageView);
        mainSeekBar = (SeekBar) findViewById(R.id.seekBar);
        songNameTextView = (TextView) findViewById(R.id.songNameTextView);
        artistNameTextView = (TextView) findViewById(R.id.artistNameTextView);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playPauseButtonImageButton:
                playPause.setImageResource(android.R.drawable.ic_media_play);
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
        }
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
    public void onBackPressed() {
        int position = mViewPager.getCurrentItem();
        switch (position) {
            case 0:
                finish();
                break;
            case 1:
                EventBus.getDefault().post(new MessageFromBackPressed(MessageFromBackPressed.FROM_BACK_PRESSED));
                break;
            case 2:
                EventBus.getDefault().post(new MessageFromBackPressed(MessageFromBackPressed.FROM_BACK_PRESSED));
                break;
            case 3:
                break;
        }
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Songs";
                case 1:
                    return "Artists";
                case 2:
                    return "Albums";
            }
            return null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
