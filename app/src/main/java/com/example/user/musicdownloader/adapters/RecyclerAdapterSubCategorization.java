package com.example.user.musicdownloader.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.activities.MainActivity;
import com.example.user.musicdownloader.data.GetMusicData;
import com.example.user.musicdownloader.data.Song;
import com.example.user.musicdownloader.fragments.fragmentSongPlayer;
import com.example.user.musicdownloader.services.PlaySongService;
import com.example.user.musicdownloader.tools.Contextor;
import com.example.user.musicdownloader.tools.ShPref;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.user.musicdownloader.data.GetMusicData.songs;

public class RecyclerAdapterSubCategorization extends RecyclerView.Adapter<RecyclerAdapterSubCategorization.ViewHolder> {

    private final ArrayList<String> labels;
    private final int adapterType;
    private HashMap<String, Object> map;
    private WeakReference<fragmentSongPlayer> week;
    public static final int TYPE_ARTIST = 1;
    public static final int TYPE_ALBUM = 2;


    public RecyclerAdapterSubCategorization(ArrayList<String> labels, int type, WeakReference<fragmentSongPlayer> weak) {
        this.labels = labels;
        this.adapterType = type;
        this.week = weak;
        map = new HashMap<>();
        if (type == TYPE_ARTIST) {
            for (String string : labels) {
                map.put(string, GetMusicData.getNumberOfSongsFromArtist(string) + " Songs");
            }
        } else {
            for (String string : labels) {
                map.put(string, getAlbumForString(string));
            }
        }
    }

    @Override
    public RecyclerAdapterSubCategorization.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (adapterType) {
            case TYPE_ARTIST:
                View v1 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_cell_artist, parent, false);
                return new RecyclerAdapterSubCategorization.ViewHolder(v1);
            case TYPE_ALBUM:
                View v2 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_cell_album, parent, false);
                return new RecyclerAdapterSubCategorization.ViewHolder(v2);
            default:
                View v10 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_cell_album, parent, false);
                return new RecyclerAdapterSubCategorization.ViewHolder(v10);
        }
    }


    @Override
    public void onBindViewHolder(final RecyclerAdapterSubCategorization.ViewHolder holder, final int position) {
        final int p = holder.getAdapterPosition();
        switch (adapterType) {
            case TYPE_ARTIST:
                String artist = labels.get(p);
                holder.artistTextView.setText(artist);
                holder.numberOfSongsTextView.setText((String) map.get(artist));
                break;
            case TYPE_ALBUM:
                String album = labels.get(p);
                holder.title.setText(album);
                Picasso.with(holder.itemView.getContext()).load( ((Album)map.get(album)).image).into(holder.albumImageView);
                holder.artistTextView.setText(((Album)map.get(album)).artist);
                break;
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title, artistTextView, numberOfSongsTextView;
        private ImageView albumImageView, dots;

        ViewHolder(View itemView) {
            super(itemView);
            artistTextView = (TextView) itemView.findViewById(R.id.cellArtist_ArtistEditText);
            dots = (ImageView) itemView.findViewById(R.id.threeDotsItem);
            itemView.setOnClickListener(this);
            dots.setOnClickListener(this);
            switch (adapterType) {
                case TYPE_ALBUM:
                    title = (TextView) itemView.findViewById(R.id.cellArtist_AlbumEditText);
                    albumImageView = (ImageView) itemView.findViewById(R.id.cellAlbumImageView);
                    itemView.setOnClickListener(this);
                    break;
                case TYPE_ARTIST:
                    numberOfSongsTextView = (TextView) itemView.findViewById(R.id.cellArtist_numberOfSongsEditText);
                    break;
            }

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.threeDotsItem:
                    showPopupMenu(dots);
                    break;
                default:
                    fragmentSongPlayer fragment = week.get();
                    if (fragment != null) {
                        switch (adapterType) {
                            case TYPE_ARTIST:
                                fragment.filterSongList(labels.get(getAdapterPosition()), MainActivity.FROM_ADAPTER_ARTIST);
                                break;
                            case TYPE_ALBUM:
                                fragment.filterSongList(labels.get(getAdapterPosition()), MainActivity.FROM_ADAPTER_ALBUM);
                                break;
                        }
                    }
            }
        }

        private void showPopupMenu(ImageView dots) {

            PopupMenu popupMenu = new PopupMenu(dots.getContext(), dots);
            popupMenu.getMenuInflater().inflate(R.menu.pop_up_menu_artist, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(final MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.play:
                            playAlbum(getAdapterPosition());
                            break;
                        case R.id.add_to_queue:
                            playNext(getAdapterPosition());
                            break;
                        case R.id.delete:
                            deleteArtist();
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
            popupMenu.show();
        }

        private void deleteArtist() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            for (int i = 0; i < songs.size(); i++) {
                                if (songs.get(i).getArtist().equals(labels.get(getAdapterPosition()))) {
                                    File k = new File(String.valueOf(songs.get(i).getUri()));
                                    boolean b = k.delete();
                                }
                            }
                            labels.remove(getAdapterPosition());
                            notifyDataSetChanged();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setMessage("This action will delete all songs contains to this artist, are you sure you want to delete?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }

        private void playAlbum(int p) {
            switch (adapterType) {
                case TYPE_ALBUM:
                    for (int i = 0; i < songs.size(); i++) {
                        if (songs.get(i).getAlbum().equals(labels.get(p))) {
                            Context context = Contextor.getInstance().getContext();
                            PlaySongService.currentPlayedSong = songs.get(i);
                            PlaySongService.client = PlaySongService.CLIENT.ALBUMS;
                            context.startService(new Intent(context, PlaySongService.class));
                            return;
                        }
                    }
                    break;
                case TYPE_ARTIST:
                    for (int i = 0; i < songs.size(); i++) {
                        if (songs.get(i).getArtist().equals(labels.get(p))) {
                            Context context = Contextor.getInstance().getContext();
                            PlaySongService.currentPlayedSong = songs.get(i);
                            PlaySongService.client = PlaySongService.CLIENT.ARTIST;
                            context.startService(new Intent(context, PlaySongService.class));
                            return;
                        }
                    }
                    break;
            }
        }


        private void playNext(int p) {
            switch (adapterType) {
                case TYPE_ALBUM:
                    for (int i = 0; i < songs.size(); i++) {
                        if (songs.get(i).getAlbum().equals(labels.get(p))) {
                            ShPref.put(R.string.song_path_for_service, songs.get(i).getUri().toString());
                            ShPref.put(R.string.song_name_for_service, songs.get(i).getName());
                            ShPref.put(R.string.song_artist_for_service, songs.get(i).getArtist());
                            ShPref.put(R.string.song_thumb_for_service, songs.get(i).getImage().toString());
                            ShPref.put(R.string.song_position_in_array, i);
                            return;
                        }
                    }
                    break;
                case TYPE_ARTIST:
                    for (int i = 0; i < songs.size(); i++) {
                        if (songs.get(i).getArtist().equals(labels.get(p))) {
                            ShPref.put(R.string.song_path_for_service, songs.get(i).getUri().toString());
                            ShPref.put(R.string.song_name_for_service, songs.get(i).getName());
                            ShPref.put(R.string.song_artist_for_service, songs.get(i).getArtist());
                            ShPref.put(R.string.song_thumb_for_service, songs.get(i).getImage().toString());
                            ShPref.put(R.string.song_position_in_array, i);
                            return;
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    private Album getAlbumForString(String string) {
        for (Song song : songs) {
            if (song.getAlbum().equals(string)) {
                return new Album(song.getImage(), song.getArtist());
            }
        }
        return null;
    }

    private class Album{


        Uri image;
        String artist;

        public Album(Uri image, String artist) {
            this.image = image;
            this.artist = artist;
        }
    }
}

