package com.musicplayer.freedownload.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.musicplayer.freedownload.EventBus.messages.MessageFromBackPressed;
import com.musicplayer.freedownload.EventBus.messages.MessageSearchOnline;
import com.musicplayer.freedownload.R;
import com.musicplayer.freedownload.activities.MainActivity;
import com.musicplayer.freedownload.adapters.RecyclerAdapterSearch;
import com.musicplayer.freedownload.data.Song;
import com.musicplayer.freedownload.tools.SearchHelper;
import com.musicplayer.freedownload.tools.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FragmentSearchTab extends Fragment implements SearchHelper.OnSearchFinishListener {

    private static ArrayList<Song> searchResultsSongs;
    private RecyclerView mRecyclerView;
    private View mProgressBar;
    private TextView textViewNoResult;;
    private Handler handler;
    private ConnectivityManager connectivityManager;


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
        connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        mProgressBar = rootView.findViewById(R.id.progressBar);
        textViewNoResult = (TextView) rootView.findViewById(R.id.text_no_result);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setRecyclerView();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private void setRecyclerView(){
        if (!Utils.isNetworkAvailable(getContext())){
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            textViewNoResult.setVisibility(View.VISIBLE);
            textViewNoResult.setText(getString(R.string.no_internet));
        }
        else
        if (MainActivity.query != null){
            if (searchResultsSongs != null && searchResultsSongs.size() > 0){
                mProgressBar.setVisibility(View.GONE);
                mRecyclerView.setAdapter(new RecyclerAdapterSearch(searchResultsSongs));
                textViewNoResult.setVisibility(View.GONE);
            } else {
                mProgressBar.setVisibility(View.VISIBLE); //display progressbar while waiting to server response
                SearchHelper.searchWeb(handler, new WeakReference<SearchHelper.OnSearchFinishListener>(this));
            }
        } else {
            searchResultsSongs = null;
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setAdapter(new RecyclerAdapterSearch(searchResultsSongs));
            textViewNoResult.setVisibility(View.VISIBLE);
            textViewNoResult.setText(getString(R.string.no_query));
        }
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageFromBackPressed event) {
        switch (event.getAction()) {
            case MessageFromBackPressed.FROM_BACK_PRESSED:
                if (4 != event.getPosition()){
                    return;
                }
                getActivity().finish();
                break;
        }
    }

    @Override
    public void onStartSearch(String query) {
        mProgressBar.setVisibility(View.VISIBLE);
        textViewNoResult.setVisibility(View.GONE);
        searchResultsSongs = null;
        mRecyclerView.invalidate();
        mRecyclerView.setAdapter(new RecyclerAdapterSearch(null));
    }

    @Override
    public void onSuccess(ArrayList<Song> songs) {
        FragmentSearchTab.searchResultsSongs = songs;
        setRecyclerWithResult();
    }

    @Override
    public void onFailure() {
        FragmentSearchTab.searchResultsSongs = null;
        Toast.makeText(getActivity(), getText(R.string.search_failure), Toast.LENGTH_SHORT).show();
        setRecyclerWithResult();
    }

    private void setRecyclerWithResult(){
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setAdapter(new RecyclerAdapterSearch(searchResultsSongs));
        if (searchResultsSongs != null && searchResultsSongs.size() > 0){
            textViewNoResult.setVisibility(View.GONE);
        } else {
            textViewNoResult.setVisibility(View.VISIBLE);
            textViewNoResult.setText(getString(R.string.search_no_result));
        }
    }


    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageSearchOnline event) {
        if (Utils.isNetworkAvailable(connectivityManager)) {
            SearchHelper.searchWeb(handler, new WeakReference<SearchHelper.OnSearchFinishListener>(this));
        }
    }



}
