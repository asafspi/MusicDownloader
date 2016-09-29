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
import com.musicplayer.freedownload.data.Artist;
import com.musicplayer.freedownload.data.Song;
import com.musicplayer.freedownload.fragments.fragmentSongPlayer;
import com.musicplayer.freedownload.services.PlaySongService;
import com.musicplayer.freedownload.tools.Contextor;

import java.io.File;
import java.lang.ref.WeakReference;

import static com.musicplayer.freedownload.data.GetMusicData.artists;

public class RecyclerAdapterArtists extends RecyclerView.Adapter<RecyclerAdapterArtists.ViewHolder> {

    private WeakReference<fragmentSongPlayer> week;

    public RecyclerAdapterArtists( WeakReference<fragmentSongPlayer> weak) {
        this.week = weak;
    }

    @Override
    public RecyclerAdapterArtists.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_cell_artist, parent, false);
        return new RecyclerAdapterArtists.ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final RecyclerAdapterArtists.ViewHolder holder, final int position) {
        Artist artist = artists.get(position);
        holder.artistTextView.setText(artist.getArtistName());
        holder.numberOfSongsTextView.setText(artist.getNumberOfSongs());
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView artistTextView, numberOfSongsTextView;
        private ImageView dots;

        ViewHolder(View itemView) {
            super(itemView);
            artistTextView = (TextView) itemView.findViewById(R.id.cellArtist_ArtistEditText);
            dots = (ImageView) itemView.findViewById(R.id.threeDotsItem);
            itemView.setOnClickListener(this);
            dots.setOnClickListener(this);
            numberOfSongsTextView = (TextView) itemView.findViewById(R.id.cellArtist_numberOfSongsEditText);

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
                        fragment.filterArtistList(artists.get(getAdapterPosition()));
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
                            playArtist(getAdapterPosition());
                            break;
                        case R.id.add_to_queue:
                            addToQueue(getAdapterPosition());
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
                            Artist artist = artists.get(getAdapterPosition());
                            for (Song song : artist.getArtistSongs()) {
                                    File k = new File(String.valueOf(song.getUri()));
                                    boolean b = k.delete();
                            }
                            artists.remove(artist);
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

        private void playArtist(int p) {
            Artist artist = artists.get(p);
            Context context = Contextor.getInstance().getContext();
            PlaySongService.currentArraySong = artist.getArtistSongs();
            PlaySongService.currentPlayedSong = artist.getArtistSongs().get(0);
            PlaySongService.client = PlaySongService.CLIENT.ARTIST;
            context.startService(new Intent(context, PlaySongService.class));
        }


        private void addToQueue(int p) {
            Artist artist = artists.get(p);
            if (artist.getArtistSongs().equals(PlaySongService.currentArraySong)){
                return;
            }
            Context context = Contextor.getInstance().getContext();
            PlaySongService.currentArraySong = artist.getArtistSongs();
            PlaySongService.client = PlaySongService.CLIENT.ARTIST;
            Intent intent = new Intent(context, PlaySongService.class);
            intent.putExtra(PlaySongService.EXTRA_ADDED_TO_QUEUE, true);
            context.startService(intent);
        }
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

}

