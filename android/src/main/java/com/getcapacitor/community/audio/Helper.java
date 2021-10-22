package com.getcapacitor.community.audio;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class Helper {
    static String mapPlayerStatus(PlaybackStateCompat state){
        String status = "OTHER";
        switch (state.getState()){
            case PlaybackStateCompat.STATE_PLAYING:
                status = "PLAYING";
                break;
            case PlaybackStateCompat.STATE_NONE:
                status = "NONE";
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                status = "STOPPED";
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                status = "PAUSED";
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                status = "BUFFERING";
                break;
            case PlaybackStateCompat.STATE_ERROR:
                status = "ERROR";
                break;
            case PlaybackStateCompat.STATE_CONNECTING:
                status = "CONNECTING";
                break;
        }
        return status;
    }

    static CompletableFuture<Bitmap> getBitmap(String url, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> loadFromUrl(url) , executor);
    }

    @WorkerThread
    private static @Nullable Bitmap loadFromUrl(String urlString) {
        InputStream in;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            in = connection.getInputStream();
            return BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            Log.e("NativeAudio", "Error loading bitmap", e);
        }
        return null;
    }
}
