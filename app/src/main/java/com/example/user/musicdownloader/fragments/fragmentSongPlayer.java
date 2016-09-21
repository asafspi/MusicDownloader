package com.example.user.musicdownloader.fragments;

import android.os.AsyncTask;
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
import com.example.user.musicdownloader.EventBus.messages.MessageSearchOnline;
import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.activities.MainActivity;
import com.example.user.musicdownloader.adapters.ArtistsAdapter;
import com.example.user.musicdownloader.adapters.RecyclerAdapterSearch;
import com.example.user.musicdownloader.adapters.RecyclerAdapterSongs;
import com.example.user.musicdownloader.data.GetMusicData;
import com.example.user.musicdownloader.data.SearchedSong;
import com.example.user.musicdownloader.data.Song;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.user.musicdownloader.data.GetMusicData.songs;

public class fragmentSongPlayer extends Fragment  {

    private static final int TAB_SONGS = 0;
    private static final int TAB_ARTIST = 1;
    private static final int TAB_ALBUM = 2;
    private static final int TAB_SEARCH = 3;
    private RecyclerView mRecyclerView;
    private RecyclerAdapterSongs songsAdapter;
    public WeakReference<fragmentSongPlayer> weak;
    private View mProgressBar;
    private View textViewNoResult;;

    private static final String ARG_SECTION_NUMBER = "section_number";
    int position;
    public static String placeHolder;
    private boolean showFilterered;

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
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        mProgressBar = rootView.findViewById(R.id.progressBar);
        textViewNoResult = rootView.findViewById(R.id.text_no_result);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        position = getArguments().getInt(ARG_SECTION_NUMBER);
        setRecycler();
        return rootView;
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
            case TAB_SEARCH:

                break;

        }
    }

    private void setRecyclerAlbums() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mRecyclerView.setAdapter(new ArtistsAdapter(GetMusicData.artists, ArtistsAdapter.TYPE_ARTIST, getContext(), weak));
    }

    private void setRecyclerArtist() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        mRecyclerView.setAdapter(new ArtistsAdapter(GetMusicData.albums, ArtistsAdapter.TYPE_ALBUM, getContext(), weak));
    }

    private void setRecyclerSongs(ArrayList<Song> songs, String query){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new RecyclerAdapterSongs(songs, query));

    }

    public void filterSongList(String title, int opt) {
        showFilterered = true;
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
                if (showFilterered){
                    setRecycler();
                    showFilterered = false;
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

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageSearchOnline event) {
        if (position == TAB_SEARCH) {
            MainActivity.query = event.getQuery();
            new SearchWebAsyncTask().execute(MainActivity.query);
        }
    }

    private class SearchWebAsyncTask extends AsyncTask<String, Void, ArrayList<SearchedSong>> {

        private Element connectionElement;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE); //display progressbar while waiting to server response
        }

        @Override
        protected ArrayList<SearchedSong> doInBackground(String... params) {
            String query = params[0];
            query = query.trim().replaceAll(" ", "+");
            try {
                connectionElement = Jsoup.connect("http://mp3.sogou.com/music.so?st=1&query=" + query + "&debug=null&comp=1" + "&len=30").timeout(8000).ignoreHttpErrors(true).get().body();//&page=" + this.f1478d
                final ArrayList<SearchedSong> songs = new ArrayList<>();
                String songLink = null;
                String songLabel = null;
                String songArtist = null;
                String songAlbum = null;
                LOOP: for (Element element : connectionElement.getElementsByClass("play_btn")){
                    Pattern p = Pattern.compile("#(.*?)#");
                    Matcher matcher = p.matcher(element.attr("onclick"));
                    int i = 0;
                    String s;
                    MATCHER: while (matcher.find()) {
                        s = matcher.group(1);
                        switch (i){
                            case 2:
                                if (s == null || s.length() < 10){ //link is broken
                                    continue LOOP;
                                }
                                songLink = s;
                                break;
                            case 3:
                                songLabel = s;
                                break;
                            case 5 :
                                songArtist = s;
                                break;
                            case 7:
                                songAlbum = s;
                                break MATCHER;

                        }
                        System.out.println(s);
                        i++;
                    }
                    SearchedSong song = new SearchedSong(songLink, songLabel, songArtist, songAlbum);
                    songs.add(song);
//                    System.out.println("_____________________________________________________________");
                }
                return songs;
//                Log.d("TAG", songs.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<SearchedSong> songs) {
            super.onPostExecute(songs);
            mProgressBar.setVisibility(View.GONE); //display progressbar while waiting to server response
            if (songs != null && songs.size() > 0) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setAdapter(new RecyclerAdapterSearch(songs));
                textViewNoResult.setVisibility(View.GONE);
            } else {
                textViewNoResult.setVisibility(View.VISIBLE);
            }

        }
    }

}
