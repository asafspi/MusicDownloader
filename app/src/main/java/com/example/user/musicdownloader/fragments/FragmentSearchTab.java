package com.example.user.musicdownloader.fragments;

import android.os.Bundle;
import android.os.Handler;
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

public class FragmentSearchTab extends Fragment implements SearchHelper.OnSearchFinishListener {

    private static ArrayList<SearchedSong> searchResultsSongs;
    private RecyclerView mRecyclerView;
    private View mProgressBar;
    private View textViewNoResult;;
    private Handler handler;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FragmentSearchTab newInstance() {
        return new FragmentSearchTab();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        handler = new Handler();
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        mProgressBar = rootView.findViewById(R.id.progressBar);
        textViewNoResult = rootView.findViewById(R.id.text_no_result);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (MainActivity.query != null){
            mProgressBar.setVisibility(View.VISIBLE); //display progressbar while waiting to server response
            SearchHelper.searchWeb(handler, this);
        } else {
            setRecyclerView();
        }
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity.query = null;
    }

    private void setRecyclerView(){
        mProgressBar.setVisibility(View.GONE);
        if (searchResultsSongs != null && searchResultsSongs.size() > 0) {
            mRecyclerView.setAdapter(new RecyclerAdapterSearch(searchResultsSongs));
            textViewNoResult.setVisibility(View.GONE);
        } else {
            mRecyclerView.setAdapter(new RecyclerAdapterSearch(searchResultsSongs));
            textViewNoResult.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void onSuccess(ArrayList<SearchedSong> songs) {
        FragmentSearchTab.searchResultsSongs = songs;
        setRecyclerView();
    }

    @Override
    public void onFailure() {

    }


    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageSearchOnline event) {
        MainActivity.query = event.getQuery();
        SearchHelper.searchWeb(handler, this);
    }



}