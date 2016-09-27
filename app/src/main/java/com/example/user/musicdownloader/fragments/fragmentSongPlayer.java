package com.example.user.musicdownloader.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.user.musicdownloader.EventBus.messages.MessageFromBackPressed;
import com.example.user.musicdownloader.EventBus.messages.MessageSearch;
import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.activities.MainActivity;
import com.example.user.musicdownloader.adapters.RecyclerAdapterSongs;
import com.example.user.musicdownloader.adapters.RecyclerAdapterSubCategorization;
import com.example.user.musicdownloader.data.GetMusicData;
import com.example.user.musicdownloader.data.Song;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.example.user.musicdownloader.data.GetMusicData.downloads;
import static com.example.user.musicdownloader.data.GetMusicData.songs;

public class fragmentSongPlayer extends Fragment  {

    private static final int TAB_SONGS = 0;
    private static final int TAB_ARTIST = 1;
    private static final int TAB_ALBUM = 2;
    private static final int TAB_DOWNLOADS = 3;
    private RecyclerView mRecyclerView;
    public WeakReference<fragmentSongPlayer> weak;

    private static final String ARG_SECTION_NUMBER = "section_number";
    int position;
    public static String placeHolder;
    private boolean showFiltered;

    public fragmentSongPlayer() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static fragmentSongPlayer newInstance(int sectionNumber) {
        fragmentSongPlayer fragment = new fragmentSongPlayer();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        placeHolder = getContext().getString(R.string.search_web);
        weak = new WeakReference<>(this);
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_recycler, container, false);
        mRecyclerView.setHasFixedSize(true);

        position = getArguments().getInt(ARG_SECTION_NUMBER);
        setRecycler();
        return mRecyclerView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private void setRecycler(){
        switch (position) {
            case TAB_SONGS:
                setRecyclerSongs(songs, null);
                break;
            case TAB_ARTIST:
                setRecyclerArtist();
                break;
            case TAB_ALBUM:
                setRecyclerAlbums();
                break;
            case TAB_DOWNLOADS:
                setRecyclerSongs(downloads, null);
                break;
        }
    }

    private void setRecyclerAlbums() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mRecyclerView.setAdapter(new RecyclerAdapterSubCategorization(GetMusicData.albums, RecyclerAdapterSubCategorization.TYPE_ALBUM,  weak));
    }

    private void setRecyclerArtist() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        mRecyclerView.setAdapter(new RecyclerAdapterSubCategorization(GetMusicData.artists, RecyclerAdapterSubCategorization.TYPE_ARTIST,  weak));
    }

    private void setRecyclerSongs(ArrayList<Song> songs, String query){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new RecyclerAdapterSongs(songs, query));

    }

    public void filterSongList(String title, int opt) {
        showFiltered = true;
        switch (opt) {
            case MainActivity.FROM_ADAPTER_ARTIST: //From adapter artist
                ArrayList<Song> artistSongs = new ArrayList<>();
                for (int i = 0; i < GetMusicData.songs.size(); i++) {
                    if (GetMusicData.songs.get(i).getArtist().equals(title)) {
                        artistSongs.add(GetMusicData.songs.get(i));
                    }
                }
                setRecyclerSongs(artistSongs, null);
                break;
            case MainActivity.FROM_ADAPTER_ALBUM: //From adapter album
                ArrayList<Song> albumSongs = new ArrayList<>();
                for (int i = 0; i < GetMusicData.songs.size(); i++) {
                    if (GetMusicData.songs.get(i).getAlbum().equals(title)) {
                        albumSongs.add(GetMusicData.songs.get(i));
                    }
                }
                setRecyclerSongs(albumSongs, null);
                break;
        }
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageFromBackPressed event) {
        switch (event.getAction()) {
            case MessageFromBackPressed.FROM_BACK_PRESSED:
                if (position != event.getPosition()){
                    return;
                }
                if (showFiltered){
                    setRecycler();
                    showFiltered = false;
                } else {
                    getActivity().finish();
                }
                break;
            case MessageFromBackPressed.FROM_THREAD:  //
                setRecycler();
                break;
        }
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageSearch event) {
        setRecyclerSongs(event.getQuerySongs(), event.getQuery());
    }

}
