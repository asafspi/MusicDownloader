package com.example.user.musicdownloader.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.user.musicdownloader.R;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_search_result, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SearchedSong song = songs.get(position);
        holder.textLabel.setText(song.getSongLabel());
        holder.textAlbum.setText(song.getSongAlbum());
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView textLabel, textAlbum;

        public ViewHolder(View itemView) {
            super(itemView);
            textLabel = (TextView)itemView.findViewById(R.id.text_label);
            textAlbum = (TextView)itemView.findViewById(R.id.text_album);
        }
    }
}
