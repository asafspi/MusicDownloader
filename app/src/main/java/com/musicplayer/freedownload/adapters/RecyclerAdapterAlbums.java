package com.musicplayer.freedownload.adapters;

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

import com.musicplayer.freedownload.R;
import com.musicplayer.freedownload.data.Album;
import com.musicplayer.freedownload.data.Song;
import com.musicplayer.freedownload.fragments.fragmentSongPlayer;
import com.musicplayer.freedownload.services.PlaySongService;
import com.musicplayer.freedownload.tools.Contextor;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.ref.WeakReference;

import static com.musicplayer.freedownload.data.GetMusicData.albums;

public class RecyclerAdapterAlbums extends RecyclerView.Adapter<RecyclerAdapterAlbums.ViewHolder> {

    private WeakReference<fragmentSongPlayer> week;


    public RecyclerAdapterAlbums(WeakReference<fragmentSongPlayer> weak) {
        this.week = weak;
    }

    @Override
    public RecyclerAdapterAlbums.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v2 = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_cell_album, parent, false);
        return new RecyclerAdapterAlbums.ViewHolder(v2);
    }


    @Override
    public void onBindViewHolder(final RecyclerAdapterAlbums.ViewHolder holder, final int position) {
        Album album = albums.get(position);
        holder.title.setText(album.getAlbumName());
        Picasso.with(holder.itemView.getContext()).load(album.getAlbumUri()).into(holder.albumImageView);
        holder.artistTextView.setText(album.getArtistName());
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title, artistTextView;
        private ImageView albumImageView, dots;

        ViewHolder(View itemView) {
            super(itemView);
            artistTextView = (TextView) itemView.findViewById(R.id.cellArtist_ArtistEditText);
            dots = (ImageView) itemView.findViewById(R.id.threeDotsItem);
            itemView.setOnClickListener(this);
            dots.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.cellArtist_AlbumEditText);
            albumImageView = (ImageView) itemView.findViewById(R.id.cellAlbumImageView);
            itemView.setOnClickListener(this);


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
                        fragment.filterAlbumList(albums.get(getAdapterPosition()));
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
                            addToQueue(getAdapterPosition());
                            break;
                        case R.id.delete:
                            deleteAlbum();
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
            popupMenu.show();
        }

        private void deleteAlbum() {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Album album = albums.get(getAdapterPosition());
                            for (Song song : album.getAlbumSongs()) {
                                File k = new File(String.valueOf(song.getUri()));
                                boolean b = k.delete();
                            }
                            albums.remove(album);
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
            Album album = albums.get(p);
            Context context = Contextor.getInstance().getContext();
            PlaySongService.currentArraySong = album.getAlbumSongs();
            PlaySongService.currentPlayedSong = album.getAlbumSongs().get(0);
            PlaySongService.client = PlaySongService.CLIENT.ALBUMS;
            context.startService(new Intent(context, PlaySongService.class));
        }


        private void addToQueue(int p) {
            Album album = albums.get(p);
            if (album.getAlbumSongs().equals(PlaySongService.currentArraySong)){
                return;
            }
            Context context = Contextor.getInstance().getContext();
            PlaySongService.currentArraySong = album.getAlbumSongs();
            PlaySongService.client = PlaySongService.CLIENT.ALBUMS;
            Intent intent = new Intent(context, PlaySongService.class);
            intent.putExtra(PlaySongService.EXTRA_ADDED_TO_QUEUE, true);
            context.startService(intent);
        }
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }



}

