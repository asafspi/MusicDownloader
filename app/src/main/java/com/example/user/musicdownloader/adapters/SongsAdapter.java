package com.example.user.musicdownloader.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.musicdownloader.Contextor;
import com.example.user.musicdownloader.PlaySongService;
import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.ShPref;
import com.example.user.musicdownloader.Song;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private final ArrayList<Song> songsList;
    private Context mContext;
    private final int songType;
    public static final int TYPE_ALL_SONGS = 1;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ALL_SONGS:
                View v1 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_cell_song, parent, false);
                return new ViewHolder(v1);
            default:
                View v10 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_cell_song, parent, false);
                return new ViewHolder(v10);
        }
    }

    public SongsAdapter(ArrayList<Song> songs, int songType, Context context) {
        this.songsList = songs;
        this.songType = songType;
        this.mContext = context;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final int p = holder.getAdapterPosition();
        switch (songType) {
            case TYPE_ALL_SONGS:
                holder.title.setText(songsList.get(p).getName());
                holder.artist.setText(songsList.get(p).getArtist());
                Picasso.with(mContext).load(songsList.get(p).getImage()).into(holder.thumbImageView);
                break;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = Contextor.getInstance().getContext();
                ShPref.put(R.string.song_path_for_service, songsList.get(p).getUri().toString());
                ShPref.put(R.string.song_name_for_service, songsList.get(p).getName());
                ShPref.put(R.string.song_artist_for_service, songsList.get(p).getArtist());
                ShPref.put(R.string.song_thumb_for_service, songsList.get(p).getImage().toString());
                ShPref.put(R.string.song_position_in_array, p);
                context.stopService(new Intent(context, PlaySongService.class));
                context.startService(new Intent(context, PlaySongService.class));
            }
        });
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title, artist;
        private ImageView thumbImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            switch (songType) {
                case TYPE_ALL_SONGS:
                    title = (TextView) itemView.findViewById(R.id.cellSongTextView);
                    artist = (TextView) itemView.findViewById(R.id.cellArtistTextView);
                    thumbImageView = (ImageView) itemView.findViewById(R.id.cellImageView);
                    break;
            }
        }
    }


    @Override
    public int getItemCount() {
        return songsList.size();
    }
}
