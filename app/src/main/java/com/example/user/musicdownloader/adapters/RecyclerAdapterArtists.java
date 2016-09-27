package com.example.user.musicdownloader.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

public class RecyclerAdapterArtists extends RecyclerView.Adapter<RecyclerAdapterArtists.ViewHolder> {

    private final ArrayList<String> artistsList;
    private final ArrayList<Song> allSongsList = GetMusicData.songs;
    private final int itemType;
    private WeakReference<fragmentSongPlayer> week;
    public static final int TYPE_ARTIST = 1;
    public static final int TYPE_ALBUM = 2;

    @Override
    public RecyclerAdapterArtists.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (itemType) {
            case TYPE_ARTIST:
                View v1 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_cell_artist, parent, false);
                return new RecyclerAdapterArtists.ViewHolder(v1);
            case TYPE_ALBUM:
                View v2 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_cell_album, parent, false);
                return new RecyclerAdapterArtists.ViewHolder(v2);
            default:
                View v10 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_cell_album, parent, false);
                return new RecyclerAdapterArtists.ViewHolder(v10);
        }
    }

    public RecyclerAdapterArtists(ArrayList<String> artists, int itemType,WeakReference<fragmentSongPlayer> weak) {
        this.artistsList = artists;
        this.itemType = itemType;
        this.week = weak;
    }

    @Override
    public void onBindViewHolder(final RecyclerAdapterArtists.ViewHolder holder, final int position) {
        final int p = holder.getAdapterPosition();
        switch (itemType) {
            case TYPE_ARTIST:
                holder.artistTextView.setText(artistsList.get(p));
                holder.numberOfSongsTextView.setText(GetMusicData.getNumberOfSongsFromArtist(artistsList.get(p)) + " Songs");
                break;
            case TYPE_ALBUM:
                holder.title.setText(artistsList.get(p));
                for(int i = 0; i <allSongsList.size(); i++ ){
                    if(allSongsList.get(i).getAlbum().equals(artistsList.get(p))){
                        Picasso.with(holder.itemView.getContext()).load(allSongsList.get(i).getImage()).into(holder.albumImageView);
                        holder.artistTextView.setText(allSongsList.get(i).getArtist());
                        break;
                    }
                }
                break;
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title, artistTextView, numberOfSongsTextView;
        private ImageView albumImageView, dots;

        public ViewHolder(View itemView) {
            super(itemView);
            artistTextView = (TextView) itemView.findViewById(R.id.cellArtist_ArtistEditText);
            dots = (ImageView) itemView.findViewById(R.id.threeDotsItem);
            itemView.setOnClickListener(this);
            dots.setOnClickListener(this);
            switch (itemType){
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
            switch (view.getId()){
                case R.id.threeDotsItem:
                    showPopupMenu(dots, getAdapterPosition());
                    break;
                default:
                    fragmentSongPlayer fragment = week.get();
                    if (fragment != null) {
                        switch (itemType) {
                            case TYPE_ARTIST:
                                fragment.filterSongList(artistsList.get(getAdapterPosition()), MainActivity.FROM_ADAPTER_ARTIST);
                                break;
                            case TYPE_ALBUM:
                                fragment.filterSongList(artistsList.get(getAdapterPosition()), MainActivity.FROM_ADAPTER_ALBUM);
                                break;
                        }
                    }
            }
        }

        private void showPopupMenu(ImageView dots, int adapterPosition) {

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
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            for (int i = 0; i < GetMusicData.songs.size(); i++) {
                                if (GetMusicData.songs.get(i).getArtist().equals(artistsList.get(getAdapterPosition()))) {
                                    File k = new File(String.valueOf(GetMusicData.songs.get(i).getUri()));
                                    boolean b = k.delete();
                                }
                            }
                            artistsList.remove(getAdapterPosition());
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
            switch (itemType){
                case TYPE_ALBUM:
                    for (int i = 0; i < allSongsList.size(); i++){
                        if(allSongsList.get(i).getAlbum().equals(artistsList.get(p))){
                            Context context = Contextor.getInstance().getContext();
                            PlaySongService.currentPlayedSong = allSongsList.get(i);
                            context.startService(new Intent(context, PlaySongService.class));
                            return;
                        }
                    }
                    break;
                case TYPE_ARTIST:
                    for (int i = 0; i < allSongsList.size(); i++){
                        if(allSongsList.get(i).getArtist().equals(artistsList.get(p))){
                            Context context = Contextor.getInstance().getContext();
                            PlaySongService.currentPlayedSong = allSongsList.get(i);
                            context.startService(new Intent(context, PlaySongService.class));
                            return;
                        }
                    }
                    break;
            }
        }


        private void playNext(int p) {
            switch (itemType){
                case TYPE_ALBUM:
                    for (int i = 0; i < allSongsList.size(); i++){
                        if(allSongsList.get(i).getAlbum().equals(artistsList.get(p))){
                            ShPref.put(R.string.song_path_for_service, allSongsList.get(i).getUri().toString());
                            ShPref.put(R.string.song_name_for_service, allSongsList.get(i).getName());
                            ShPref.put(R.string.song_artist_for_service, allSongsList.get(i).getArtist());
                            ShPref.put(R.string.song_thumb_for_service, allSongsList.get(i).getImage().toString());
                            ShPref.put(R.string.song_position_in_array, i);
                            return;
                        }
                    }
                    break;
                case TYPE_ARTIST:
                    for (int i = 0; i < allSongsList.size(); i++){
                        if(allSongsList.get(i).getArtist().equals(artistsList.get(p))){
                            ShPref.put(R.string.song_path_for_service, allSongsList.get(i).getUri().toString());
                            ShPref.put(R.string.song_name_for_service, allSongsList.get(i).getName());
                            ShPref.put(R.string.song_artist_for_service, allSongsList.get(i).getArtist());
                            ShPref.put(R.string.song_thumb_for_service, allSongsList.get(i).getImage().toString());
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
        return artistsList.size();
    }
}

