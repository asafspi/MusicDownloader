package com.musicplayer.freedownload.adapters;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.musicplayer.freedownload.EventBus.messages.EventForSearchRecyclerView;
import com.musicplayer.freedownload.R;
import com.musicplayer.freedownload.activities.MainActivity;
import com.musicplayer.freedownload.activities.PermissionsActivity;
import com.musicplayer.freedownload.data.GetMusicData;
import com.musicplayer.freedownload.data.Song;
import com.musicplayer.freedownload.services.PlaySongService;
import com.startapp.android.publish.StartAppAd;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

import static com.musicplayer.freedownload.activities.MainActivity.downId;

public class RecyclerAdapterSearch extends RecyclerView.Adapter<RecyclerAdapterSearch.ViewHolder> {

    private ArrayList<Song> songs;
    private static int counter = 0;

    public RecyclerAdapterSearch(ArrayList<Song> songs) {
        this.songs = songs;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_search_result, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.textLabel.setText(song.getName());
        holder.textAlbum.setText(song.getAlbum());
        holder.rowProgressBar.setVisibility(song.isLoadedToPlayer() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        if (songs == null) {
            return 0;
        }
        return songs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView textLabel, textAlbum;
        View btnPlay, btnDownload;
        ProgressBar rowProgressBar;

        ViewHolder(View itemView) {

            super(itemView);
            textLabel = (TextView)itemView.findViewById(R.id.text_label);
            textAlbum = (TextView)itemView.findViewById(R.id.text_album);
            btnPlay = itemView.findViewById(R.id.play_btn);
            btnPlay.setOnClickListener(this);
            btnDownload = itemView.findViewById(R.id.down_btn);
            btnDownload.setOnClickListener(this);
            rowProgressBar = (ProgressBar) itemView.findViewById(R.id.rowProgressBar);
        }

        @Override
        public void onClick(View view) {
            Song song = songs.get(getAdapterPosition());
            if (view.equals(btnDownload)){
                if(counter == 0 || counter % 3 == 0 ){
                    StartAppAd.showAd(view.getContext());
                }
                downloadFile(view.getContext(), song.getUri().toString(), song.getName() + ".mp3");
                counter ++;

            } else if (view.equals(btnPlay)){
                rowProgressBar.setVisibility(View.VISIBLE);
                PlaySongService.currentArraySong= songs;
                PlaySongService.currentPlayedSong = song;
                PlaySongService.client = PlaySongService.CLIENT.WEB;
                itemView.getContext().startService(new Intent(itemView.getContext(), PlaySongService.class));
                song.setLoadedToPlayer(true);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventForSearchRecyclerView event) {
        notifyDataSetChanged();
    }

    private void downloadFile(Context context, String url, String fileName) {
        boolean permission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (permission) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(fileName);
            request.setDescription("Downloading from server");
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC + File.separator + context.getString(R.string.app_name), fileName);
            request.allowScanningByMediaScanner();
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            downId =  manager.enqueue(request);
            MainActivity.pathId = fileName;
            Log.d("TAG", "downId: " + Long.toString(downId));
            Toast.makeText(context, "Download in progress", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(context, "You need to allow writing to memory", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions((Activity)context, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionsActivity.REQUEST_CODE_PERMISSION_WRITE_SETTINGS);
            downId = -1;
        }
    }
}
