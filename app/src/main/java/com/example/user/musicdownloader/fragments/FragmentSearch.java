package com.example.user.musicdownloader.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.activities.MainActivity;
import com.example.user.musicdownloader.adapters.RecyclerAdapterSearch;
import com.example.user.musicdownloader.data.SearchedSong;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by B.E.L on 21/09/2016.
 */
public class FragmentSearch extends Fragment {

    private View mProgressBar;
    private RecyclerView mRecyclerView;
    private View textViewNoResult;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FragmentSearch newInstance() {
        return new FragmentSearch();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mProgressBar = rootView.findViewById(R.id.progressBar);
        textViewNoResult = rootView.findViewById(R.id.text_no_result);
        queryWeb();
        return rootView;
    }

    private void queryWeb(){
        new SearchWebAsyncTask().execute(MainActivity.query);
    }

    private class SearchWebAsyncTask extends AsyncTask<String, Void, ArrayList<SearchedSong>>{

        private   Element connectionElement;

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
                mRecyclerView.setAdapter(new RecyclerAdapterSearch(songs));
                textViewNoResult.setVisibility(View.GONE);
            } else {
                textViewNoResult.setVisibility(View.VISIBLE);
            }

        }
    }

}
