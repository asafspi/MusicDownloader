package com.example.user.musicdownloader;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.user.musicdownloader.EventBus.MessageFromBackPressed;
import com.example.user.musicdownloader.EventBus.MessageSearch;
import com.example.user.musicdownloader.adapters.ArtistsAdapter;
import com.example.user.musicdownloader.adapters.SongsAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class PlaceholderFragment extends Fragment  {

    private RecyclerView songsRecyclerView;
    private SongsAdapter songsAdapter;
    public WeakReference<PlaceholderFragment> weak;
    private TabLayout tabHost;
    private static final String ARG_SECTION_NUMBER = "section_number";
    int position;

    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        SearchView searchView;
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        weak = new WeakReference<>(this);
        View rootView = inflater.inflate(R.layout.fragment_main2, container, false);
        songsRecyclerView = (RecyclerView) rootView.findViewById(R.id.songsRecyclerView);
        songsRecyclerView.setHasFixedSize(true);
        songsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        tabHost = (TabLayout) getActivity().findViewById(R.id.tabs);

        position = getArguments().getInt(ARG_SECTION_NUMBER);
        switch (position) {
            case 0:
                songsAdapter = new SongsAdapter(GetMusicData.songs, SongsAdapter.TYPE_ALL_SONGS, getContext());
                songsRecyclerView.setAdapter(songsAdapter);
                break;
            case 1:
                ArtistsAdapter artistAdapter = new ArtistsAdapter(GetMusicData.artists, ArtistsAdapter.TYPE_ARTIST, getContext(), weak);
                songsRecyclerView.setAdapter(artistAdapter);
                break;
            case 2:
                songsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                ArtistsAdapter albumAdapter = new ArtistsAdapter(GetMusicData.albums, ArtistsAdapter.TYPE_ALBUM, getContext(), weak);
                songsRecyclerView.setAdapter(albumAdapter);
                break;
        }
        return rootView;
    }

    public void setRecyclerView(String title, int opt) {
        ArrayList<Song> songs = GetMusicData.songs;
        songsRecyclerView.setHasFixedSize(true);
        songsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        switch (opt) {
            case 1: // From back pressed
                ArtistsAdapter artistAdapter;
                if (position == 1) {
                    artistAdapter = new ArtistsAdapter(GetMusicData.artists, ArtistsAdapter.TYPE_ARTIST, getContext(), weak);
                    songsRecyclerView.setAdapter(artistAdapter);
                }
                if (position == 2) {
                    artistAdapter = new ArtistsAdapter(GetMusicData.albums, ArtistsAdapter.TYPE_ALBUM, getContext(), weak);
                    songsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    songsRecyclerView.setAdapter(artistAdapter);
                }

                break;

            case 2: //From adapter artist
                ArrayList<Song> artistSongs = new ArrayList<>();
                for (int i = 0; i < songs.size(); i++) {
                    if (songs.get(i).getArtist().equals(title)) {
                        artistSongs.add(songs.get(i));
                    }
                }
                songsAdapter = new SongsAdapter(artistSongs, SongsAdapter.TYPE_ALL_SONGS, getContext());
                songsRecyclerView.setAdapter(songsAdapter);
                break;
            case 3: //From adapter album
                ArrayList<Song> albumSongs = new ArrayList<>();
                for (int i = 0; i < songs.size(); i++) {
                    if (songs.get(i).getAlbum().equals(title)) {
                        albumSongs.add(songs.get(i));
                    }
                }
                songsAdapter = new SongsAdapter(albumSongs, SongsAdapter.TYPE_ALL_SONGS, getContext());
                songsRecyclerView.setAdapter(songsAdapter);
                break;
            case 4: //From Thread

                position = getArguments().getInt(ARG_SECTION_NUMBER);
                switch (position) {
                    case 0:
                        songsAdapter = new SongsAdapter(GetMusicData.songs, SongsAdapter.TYPE_ALL_SONGS, getContext());
                        songsRecyclerView.setAdapter(songsAdapter);
                        break;
                    case 1:
                        artistAdapter = new ArtistsAdapter(GetMusicData.artists, ArtistsAdapter.TYPE_ARTIST, getContext(), weak);
                        songsRecyclerView.setAdapter(artistAdapter);
                        break;
                    case 2:
                        ArtistsAdapter albumAdapter = new ArtistsAdapter(GetMusicData.albums, ArtistsAdapter.TYPE_ALBUM, getContext(), weak);
                        songsRecyclerView.setAdapter(albumAdapter);
                        break;
                }
                break;
        }
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageFromBackPressed event) {
        switch (event.action) {
            case 1: //MessageFromBackPressed.FROM_BACK_PRESSED
                setRecyclerView("", MainActivity.FROM_BACK_PRESSED);
                break;
            case 2:  // MessageFromBackPressed.FROM_THREAD
                setRecyclerView("", 4);
                break;
        }
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageSearch event) {
        SongsAdapter songsAdapter = new SongsAdapter(event.getQuerySongs(), SongsAdapter.TYPE_ALL_SONGS, getActivity());
        songsRecyclerView.setAdapter(songsAdapter);
    }

}
