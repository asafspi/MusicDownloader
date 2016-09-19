package com.example.user.musicdownloader.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.musicdownloader.Contextor;
import com.example.user.musicdownloader.PermissionChecker;
import com.example.user.musicdownloader.PermissionsActivity;
import com.example.user.musicdownloader.PlaySongService;
import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.ShPref;
import com.example.user.musicdownloader.Song;
import com.squareup.picasso.Picasso;

import java.io.File;
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
                //Picasso.with(mContext).load(songsList.get(p).getImage()).resize(34, 34).into(holder.thumbImageView);
                Picasso.with(mContext).load(songsList.get(p).getImage()).into(holder.thumbImageView);

                break;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSong(p);
            }
        });
        holder.dots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view, p);
            }
        });
    }

    private void showPopupMenu(View v, final int p) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.pop_up_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.play:
                        playSong(p);
                        break;
                    case R.id.add_to_playlist:

                        break;
                    case R.id.use_as_ringtone:

                        String[] permissions = new String[]{Manifest.permission.WRITE_SETTINGS};
                        if (PermissionChecker.isPermissionsGranted(permissions)) {
                            Log.d("zaq", "Permissions granted");
                            File k = new File(String.valueOf(songsList.get(p).getUri()));
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
                            values.put(MediaStore.MediaColumns.TITLE, songsList.get(p).getName());
                            values.put(MediaStore.MediaColumns.SIZE, 215454);
                            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                            values.put(MediaStore.Audio.Media.ARTIST, songsList.get(p).getArtist());
                            values.put(MediaStore.Audio.Media.DURATION, 230);
                            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                            values.put(MediaStore.Audio.Media.IS_ALARM, false);
                            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                            //Insert it into the database
                            Uri uri = MediaStore.Audio.Media.getContentUriForPath(k.getAbsolutePath());
                            Uri newUri = mContext.getContentResolver().insert(uri, values);

                            RingtoneManager.setActualDefaultRingtoneUri(
                                    mContext,
                                    RingtoneManager.TYPE_RINGTONE,
                                    newUri
                            );
                        } else {
                            PermissionsActivity.startActivityForResult((Activity) mContext, PermissionsActivity.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE, permissions);
                        }

                        break;
                    case R.id.delete:
                        File k = new File(String.valueOf(songsList.get(p).getUri()));
                        k.delete();
                        notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void playSong(int p) {
        Context context = Contextor.getInstance().getContext();
        ShPref.put(R.string.song_path_for_service, songsList.get(p).getUri().toString());
        ShPref.put(R.string.song_name_for_service, songsList.get(p).getName());
        ShPref.put(R.string.song_artist_for_service, songsList.get(p).getArtist());
        ShPref.put(R.string.song_thumb_for_service, songsList.get(p).getImage().toString());
        ShPref.put(R.string.song_position_in_array, p);
        context.stopService(new Intent(context, PlaySongService.class));
        context.startService(new Intent(context, PlaySongService.class));
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title, artist;
        private ImageView thumbImageView, dots;

        public ViewHolder(View itemView) {
            super(itemView);
            switch (songType) {
                case TYPE_ALL_SONGS:
                    title = (TextView) itemView.findViewById(R.id.cellSongTextView);
                    artist = (TextView) itemView.findViewById(R.id.cellArtist_AlbumEditText);
                    thumbImageView = (ImageView) itemView.findViewById(R.id.cellImageView);
                    dots = (ImageView) itemView.findViewById(R.id.threeDotsItem);
                    break;
            }
        }
    }


    @Override
    public int getItemCount() {
        return songsList.size();
    }
}
