package com.example.user.musicdownloader.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.musicdownloader.data.GetMusicData;
import com.example.user.musicdownloader.activities.MainActivity;
import com.example.user.musicdownloader.fragments.fragmentSongPlayer;
import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.data.Song;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ViewHolder> {

    private final ArrayList<String> artistsList;
    private final ArrayList<Song> allSongsList = GetMusicData.songs;
    private Context mContext;
    private final int itemType;
    private WeakReference<fragmentSongPlayer> week;
    public static final int TYPE_ARTIST = 1;
    public static final int TYPE_ALBUM = 2;

    @Override
    public ArtistsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (itemType) {
            case TYPE_ARTIST:
                View v1 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_cell_artist, parent, false);
                return new ArtistsAdapter.ViewHolder(v1);
            case TYPE_ALBUM:
                View v2 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_cell_album, parent, false);
                return new ArtistsAdapter.ViewHolder(v2);
            default:
                View v10 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_cell_album, parent, false);
                return new ArtistsAdapter.ViewHolder(v10);
        }
    }

    public ArtistsAdapter(ArrayList<String> artists, int itemType, Context context, WeakReference<fragmentSongPlayer> weak) {
        this.artistsList = artists;
        this.itemType = itemType;
        this.mContext = context;
        this.week = weak;
    }

    @Override
    public void onBindViewHolder(final ArtistsAdapter.ViewHolder holder, final int position) {
        final int p = holder.getAdapterPosition();
        switch (itemType) {
            case TYPE_ARTIST:
                holder.artistTextView.setText(artistsList.get(p));
                //holder.numberOfAlbumsTextView.setText(GetMusicData.getNumberOfSongsFromArtist(artistsList.get(p)));
                //holder.numberOfSongsTextView.setText(GetMusicData.getNumberOfSongsFromArtist(artistsList.get(p)));
                break;
            case TYPE_ALBUM:
                holder.title.setText(artistsList.get(p));
                for(int i = 0; i <allSongsList.size(); i++ ){
                    if(allSongsList.get(i).getAlbum().equals(artistsList.get(p))){
                        Picasso.with(mContext).load(allSongsList.get(i).getImage()).into(holder.albumImageView);
                        holder.artistTextView.setText(allSongsList.get(i).getArtist());
                        break;
                    }
                }
                break;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentSongPlayer fragment = week.get();
                if (fragment != null) {
                    switch (itemType) {
                        case TYPE_ARTIST:
                            fragment.filterSongList(artistsList.get(p), MainActivity.FROM_ADAPTER_ARTIST);
                            break;
                        case TYPE_ALBUM:
                            fragment.filterSongList(artistsList.get(p), MainActivity.FROM_ADAPTER_ALBUM);
                            break;
                    }
                }


            }
        });
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title, artistTextView, numberOfSongsTextView, numberOfAlbumsTextView;
        private ImageView albumImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            switch (itemType){
                case TYPE_ALBUM:
                    title = (TextView) itemView.findViewById(R.id.cellArtist_AlbumEditText);
                    artistTextView = (TextView) itemView.findViewById(R.id.cellArtist_ArtistEditText);
                    albumImageView = (ImageView) itemView.findViewById(R.id.cellAlbumImageView);
                    break;
                case TYPE_ARTIST:
                    artistTextView = (TextView) itemView.findViewById(R.id.cellArtist_ArtistEditText);
                    numberOfSongsTextView = (TextView) itemView.findViewById(R.id.cellArtist_numberOfSongsEditText);
                    numberOfAlbumsTextView = (TextView) itemView.findViewById(R.id.cellArtist_numberOfAlbumsEditText);
                    break;
            }

        }
    }
    @Override
    public int getItemCount() {
        return artistsList.size();
    }
}

