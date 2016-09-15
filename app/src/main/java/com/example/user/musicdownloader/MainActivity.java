package com.example.user.musicdownloader;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.user.musicdownloader.GetMusicData.songs;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Runnable{

    WebView webView;
    EditText editText;
    Button button;
    String videoId;
    String shortVideoId;
    String link;
    ImageButton xImageButton;
    SeekBar progressBar;
    MediaPlayer mp;
    int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setVies();
        Boolean b = isStoragePermissionGranted();
        checkForIntent();
        //GetMusicData.getAllSongs(this);


//        progressBar = (SeekBar)findViewById(R.id.progressBar);
//        progressBar.setProgress(0);
//        progressBar.setMax(mp.getDuration());
//        new Thread(this).start();
    }


    private void checkForIntent() {

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        }
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            editText.setText(sharedText);
            getVideoId();
            GetMusicData.getDataFromJson(shortVideoId);
        }
    }



    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("ZAQ","Permission is granted");
                return true;
            } else {

                Log.v("ZAQ","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("ZAQ","Permission is granted");
            return true;
        }


    }

    private void getVideoId() {
        videoId = editText.getText().toString();
        shortVideoId = videoId.substring(17);
    }

    private void download(String link) {

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        //webView.setWebViewClient(new HelloWebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        //webView.setWebViewClient(new WebViewClient());﻿

        webView.loadUrl(link);
    }

    private void setVies() {
        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        xImageButton = (ImageButton) findViewById(R.id.x_imageButton);
        xImageButton.setOnClickListener(this);
        button.setOnClickListener(this);
        webView = (WebView) findViewById(R.id.webView);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button:
                getVideoId();
                GetMusicData.getDataFromJson(shortVideoId);
                break;
            case R.id.x_imageButton:
                editText.setText("");
                break;
        }
    }

    @Override
    public void run() {
        currentPosition= 0;
        int total = mp.getDuration();
        while (mp!=null && currentPosition<total && mp.isPlaying()) {
            try {
                Thread.sleep(1000);
                currentPosition= mp.getCurrentPosition();
            } catch (Exception e) {
                return;
            }
            progressBar.setProgress(currentPosition);
        }
    }
}
