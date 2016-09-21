package com.example.user.musicdownloader.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.musicdownloader.EventBus.messages.MessageSearchOnline;
import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.activities.PermissionsActivity;
import com.example.user.musicdownloader.data.Song;
import com.example.user.musicdownloader.services.PlaySongService;
import com.example.user.musicdownloader.tools.Contextor;
import com.example.user.musicdownloader.tools.PermissionChecker;
import com.example.user.musicdownloader.tools.ShPref;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

import static com.example.user.musicdownloader.fragments.fragmentSongPlayer.placeHolder;


public class RecyclerAdapterSongs extends RecyclerView.Adapter<RecyclerAdapterSongs.ViewHolder> {

    private static final int VIEW_TYPE_SEARCH_ONLINE = 0;
    private static final int VIEW_TYPE_REGULAR = 1;

    private final ArrayList<Song> songsList;
    private final boolean SHOW_SEARCH_ROW;
    public static final int TYPE_ALL_SONGS = 1;
    private String songQuery;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };




    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout;
        switch (viewType) {
            case VIEW_TYPE_SEARCH_ONLINE:
                layout = R.layout.row_search_online;
                break;
            default:
                layout = R.layout.item_cell_song;

        }
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(layout, parent, false), viewType);
    }

    public RecyclerAdapterSongs(ArrayList<Song> songs, String query) {
        this.songsList = songs;
        this.songQuery = query;
        this.SHOW_SEARCH_ROW = (query != null);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SEARCH_ONLINE){
            holder.textViewQuery.setText(String.format(placeHolder, songQuery));
            return;
        }
        if (SHOW_SEARCH_ROW){
            position--;
        }

        holder.title.setText(songsList.get(position).getName());
        holder.artist.setText(songsList.get(position).getArtist());
        //Picasso.with(mContext).load(songsList.get(p).getImage()).resize(34, 34).into(holder.thumbImageView);
        Picasso.with(holder.itemView.getContext()).load(songsList.get(position).getImage()).into(holder.thumbImageView);

    }

    private void showPopupMenu(final View v, final int p) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.pop_up_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.play:
                        playSong(p);
                        break;
//                    case R.id.add_to_playlist:
//                        popUpForPlaylist(v.getContext());
//                        break;
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
                            Uri newUri = v.getContext().getContentResolver().insert(uri, values);

                            RingtoneManager.setActualDefaultRingtoneUri(
                                    v.getContext(),
                                    RingtoneManager.TYPE_RINGTONE,
                                    newUri
                            );
                        } else {
                            PermissionsActivity.startActivityForResult((Activity) v.getContext(), PermissionsActivity.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE, permissions);
                        }

                        break;
                    case R.id.delete:

                        //verifyStoragePermissions(MyApplication.);

//                        File k = new File(String.valueOf(songsList.get(p).getUri()));
//                        boolean b = k.delete();
//                        k.deleteOnExit();
//                        notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
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


public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView title, artist;
    private ImageView thumbImageView, dots;
    public TextView textViewQuery
            ;

    public ViewHolder(View itemView, int viewType) {
        super(itemView);
        switch (viewType) {
            case VIEW_TYPE_REGULAR:
                title = (TextView) itemView.findViewById(R.id.cellSongTextView);
                artist = (TextView) itemView.findViewById(R.id.cellArtist_AlbumEditText);
                thumbImageView = (ImageView) itemView.findViewById(R.id.cellImageView);
                dots = (ImageView) itemView.findViewById(R.id.threeDotsItem);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playSong(getAdapterPosition());
                    }
                });
                dots.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showPopupMenu(view, getAdapterPosition());
                    }
                });
                break;
            case VIEW_TYPE_SEARCH_ONLINE:
                textViewQuery = (TextView)itemView.findViewById(R.id.query);
                textViewQuery.setOnClickListener(this);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        EventBus.getDefault().post(new MessageSearchOnline(songQuery));
    }
}


    @Override
    public int getItemCount() {
        int size = songsList.size();
        return SHOW_SEARCH_ROW ? ++size : size ;
    }

    @Override
    public int getItemViewType(int position) {
        if (SHOW_SEARCH_ROW && position == 0){
            return VIEW_TYPE_SEARCH_ONLINE;
        }
        return VIEW_TYPE_REGULAR;
    }
}
