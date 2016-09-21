package com.example.user.musicdownloader.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.user.musicdownloader.data.SearchedSong;

import java.util.ArrayList;

/**
 * Created by B.E.L on 21/09/2016.
 */

public class RecyclerAdapterSearch extends RecyclerView.Adapter<RecyclerAdapterSearch.ViewHolder> {

    private ArrayList<SearchedSong> songs;

    public RecyclerAdapterSearch(ArrayList<SearchedSong> songs) {
        this.songs = songs;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
