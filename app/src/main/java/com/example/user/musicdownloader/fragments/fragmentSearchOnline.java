package com.example.user.musicdownloader.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.user.musicdownloader.EventBus.messages.MessageFromBackPressed;
import com.example.user.musicdownloader.EventBus.messages.MessageSearchOnline;
import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.activities.MainActivity;
import com.example.user.musicdownloader.adapters.RecyclerAdapterSearch;
import com.example.user.musicdownloader.data.SearchedSong;
import com.example.user.musicdownloader.tools.SearchHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.example.user.musicdownloader.fragments.fragmentSongPlayer.placeHolder;

public class FragmentSearchOnline extends Fragment  {

    private RecyclerView mRecyclerView;
    private View mProgressBar;
    private View textViewNoResult;;
    private SearchWebAsyncTask searchWebAsyncTask;

    public FragmentSearchOnline() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FragmentSearchOnline newInstance() {
        return new FragmentSearchOnline();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        placeHolder = getContext().getString(R.string.search_web);
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        mProgressBar = rootView.findViewById(R.id.progressBar);
        textViewNoResult = rootView.findViewById(R.id.text_no_result);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }


    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageSearchOnline event) {
        MainActivity.query = event.getQuery();
        if (searchWebAsyncTask != null) {
            searchWebAsyncTask.cancel(true);
        }
        searchWebAsyncTask = new SearchWebAsyncTask();
        searchWebAsyncTask.execute(MainActivity.query);
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageFromBackPressed event) {
        switch (event.getAction()) {
            case MessageFromBackPressed.FROM_BACK_PRESSED:
                if (3 != event.getPosition()){
                    return;
                }
                getActivity().finish();
                break;
        }
    }


    private class SearchWebAsyncTask extends AsyncTask<String, Void, ArrayList<SearchedSong>> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE); //display progressbar while waiting to server response
        }

        @Override
        protected ArrayList<SearchedSong> doInBackground(String... params) {
            String query = params[0];
            query = query.trim().replaceAll(" ", "+");
            if (isCancelled()){
                return null;
            }
            return SearchHelper.searchWeb(query);
        }

        @Override
        protected void onPostExecute(ArrayList<SearchedSong> songs) {
            super.onPostExecute(songs);
            if (isCancelled()){
                return;
            }
            mProgressBar.setVisibility(View.GONE); //display progressbar while waiting to server response
            if (songs != null && songs.size() > 0) {
                mRecyclerView.setAdapter(new RecyclerAdapterSearch(songs));
                textViewNoResult.setVisibility(View.GONE);
            } else {
                mRecyclerView.setAdapter(new RecyclerAdapterSearch(songs));
                textViewNoResult.setVisibility(View.VISIBLE);
            }
            searchWebAsyncTask = null;
        }
    }

}
