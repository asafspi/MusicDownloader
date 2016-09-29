package com.musicplayer.freedownload.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.musicplayer.freedownload.EventBus.messages.MessageFromBackPressed;
import com.musicplayer.freedownload.EventBus.messages.MessageSearch;
import com.musicplayer.freedownload.R;
import com.musicplayer.freedownload.adapters.RecyclerAdapterAlbums;
import com.musicplayer.freedownload.adapters.RecyclerAdapterArtists;
import com.musicplayer.freedownload.adapters.RecyclerAdapterSongs;
import com.musicplayer.freedownload.data.Album;
import com.musicplayer.freedownload.data.Artist;
import com.musicplayer.freedownload.data.Song;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.musicplayer.freedownload.data.GetMusicData.downloads;
import static com.musicplayer.freedownload.data.GetMusicData.songs;

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
        mRecyclerView.setAdapter(new RecyclerAdapterAlbums(weak));
    }

    private void setRecyclerArtist() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        mRecyclerView.setAdapter(new RecyclerAdapterArtists(weak));
    }

    private void setRecyclerSongs(ArrayList<Song> songs, String query){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new RecyclerAdapterSongs(songs, query));

    }

    public void filterArtistList(Artist artist) {
        showFiltered = true;
        setRecyclerSongs(artist.getArtistSongs(), null);
    }

    public void filterAlbumList(Album album) {
        showFiltered = true;
        setRecyclerSongs(album.getAlbumSongs(), null);
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
        }
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageSearch event) {
        if (this.position == 0) {
            setRecyclerSongs(event.getQuerySongs(), event.getQuery());
        }
    }

}
