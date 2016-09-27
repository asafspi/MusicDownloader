package com.example.user.musicdownloader.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.example.user.musicdownloader.EventBus.messages.MessageFromBackPressed;
import com.example.user.musicdownloader.EventBus.messages.MessageSearchOnline;
import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.activities.MainActivity;
import com.example.user.musicdownloader.adapters.RecyclerAdapterSearch;
import com.example.user.musicdownloader.data.SearchedSong;
import com.example.user.musicdownloader.data.Song;
import com.example.user.musicdownloader.tools.SearchHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FragmentSearchTab extends Fragment implements SearchHelper.OnSearchFinishListener {

    private static ArrayList<Song> searchResultsSongs;
    private RecyclerView mRecyclerView;
    private View mProgressBar;
    private View textViewNoResult;;
    private TextSwitcher textSwitcher;
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
        textSwitcher = (TextSwitcher)rootView.findViewById(R.id.text_switcher);
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                TextView myText = new TextView(getContext());
                myText.setGravity(Gravity.CENTER);
                myText.setTextColor(Color.WHITE);
                myText.setTextSize(30);
                return myText;
            }
        });
        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left);
//        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
        // set the animation type of textSwitcher
        textSwitcher.setInAnimation(in);
//        textSwitcher.setOutAnimation(out);
        setRecyclerView();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private void setRecyclerView(){
        if (MainActivity.query != null){
            if (searchResultsSongs != null && searchResultsSongs.size() > 0){
                mProgressBar.setVisibility(View.GONE);
                textSwitcher.setVisibility(View.GONE);

                mRecyclerView.setAdapter(new RecyclerAdapterSearch(searchResultsSongs));
                textViewNoResult.setVisibility(View.GONE);
            } else {
                mProgressBar.setVisibility(View.VISIBLE); //display progressbar while waiting to server response
                SearchHelper.searchWeb(handler, new WeakReference<SearchHelper.OnSearchFinishListener>(this));
            }
        } else {
            searchResultsSongs = null;
            mProgressBar.setVisibility(View.GONE);
            textSwitcher.setVisibility(View.GONE);
            mRecyclerView.setAdapter(new RecyclerAdapterSearch(searchResultsSongs));
            textViewNoResult.setVisibility(View.VISIBLE);
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
        textSwitcher.setText("Searching " + "\"" + query + "\"");
        textSwitcher.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        textViewNoResult.setVisibility(View.GONE);
        searchResultsSongs = null;
        mRecyclerView.invalidate();
        mRecyclerView.setAdapter(new RecyclerAdapterSearch(null));
    }

    @Override
    public void onSuccess(ArrayList<Song> songs) {
        FragmentSearchTab.searchResultsSongs = songs;
        setRecyclerView();
    }

    @Override
    public void onFailure() {

    }


    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageSearchOnline event) {
        SearchHelper.searchWeb(handler, new WeakReference<SearchHelper.OnSearchFinishListener>(this));
        Log.d("TAG", "onMessageEvent:");
    }



}
