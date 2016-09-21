package com.example.user.musicdownloader.adapters;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.musicdownloader.R;
import com.example.user.musicdownloader.activities.PermissionsActivity;
import com.example.user.musicdownloader.data.SearchedSong;

import java.util.ArrayList;

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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView textLabel, textAlbum;
        View btnPlay, btnDownload;

        ViewHolder(View itemView) {
            super(itemView);
            textLabel = (TextView)itemView.findViewById(R.id.text_label);
            textAlbum = (TextView)itemView.findViewById(R.id.text_album);
            btnPlay = itemView.findViewById(R.id.play_btn);
            btnPlay.setOnClickListener(this);
            btnDownload = itemView.findViewById(R.id.down_btn);
            btnDownload.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.equals(btnDownload)){
                SearchedSong song = songs.get(getAdapterPosition());
                downloadFile(view.getContext(), song.getSongLink(), song.getSongLabel() + ".mp3");
            } else if (view.equals(btnPlay)){
                //TODO Asaf build method
            }
        }
    }

    public long downloadFile(Context context, String url, String fileName) {
        boolean permission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (permission) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(fileName);
            request.setDescription("Downloading from server");
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.allowScanningByMediaScanner();
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            return manager.enqueue(request);
        } else {
            Toast.makeText(context, "You need to allow writing to memory", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions((Activity)context, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionsActivity.REQUEST_CODE_PERMISSION_WRITE_SETTINGS);
            return -1;
        }

    }
}
