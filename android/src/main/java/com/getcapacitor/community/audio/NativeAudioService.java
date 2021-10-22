package com.getcapacitor.community.audio;

import static android.media.AudioAttributes.ALLOW_CAPTURE_BY_NONE;
import static android.media.MediaPlayer.SEEK_CLOSEST;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.getcapacitor.community.audio.nativeaudio.R;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class NativeAudioService extends MediaBrowserServiceCompat implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnInfoListener, MediaPlayer.OnTimedTextListener {
    public static final String COMMAND_UPDATE_PLAYBACK_STATE = "COMMAND_UPDATE_PLAYBACK_STATE";

    public static final String COMMAND_GET_CURRENT_POSITION = "COMMAND_GET_CURRENT_POSITION";
    public static final String EXTRA_CURRENT_POSITION = "EXTRA_CURRENT_POSITION";

    private static final String LOG_TAG = "PLAYER";

    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final int PLAYER_NOTIFICATION_ID = 1234;
    private static final String NOTIFICATION_CHANNEL = "player_channel_id";

    private MediaSessionCompat mediaSession;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private ExecutorService artService;
    private volatile boolean artLoading;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate ");
        artService = Executors.newFixedThreadPool(1);
        mediaSession = new MediaSessionCompat(this, LOG_TAG);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);


        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        mediaSession.setCallback(new MediaSessionCallback());

        setSessionToken(mediaSession.getSessionToken());

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnTimedTextListener(this);

        audioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            audioManager.setAllowedCapturePolicy(ALLOW_CAPTURE_BY_NONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, "Player", NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            service.createNotificationChannel(channel);
        }
    }

    private void updatePlaybackState() {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        if (mediaPlayer.isPlaying()) {
            stateBuilder.setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_STOP);
            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1);
        } else {
            stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_STOP);
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition(), 1);
        }

        mediaSession.setPlaybackState(stateBuilder.build());
    }


    private void retrieveArtForNotification(String url) {
        if (url != null && !artLoading) {
            artLoading = true;
            Helper.getBitmap(url, artService).thenAccept(bitmap -> {
                mediaSession.setMetadata(new MediaMetadataCompat.Builder(mediaSession.getController().getMetadata()).putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap).build());
                artLoading = false;
                startForeground(PLAYER_NOTIFICATION_ID, buildNotification());
            });
        }
    }

    private Notification buildNotification() {
        MediaMetadataCompat mediaMetadata = mediaSession.getController().getMetadata();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL);

        if (mediaMetadata != null) {
            builder.setContentTitle(mediaMetadata.getText(MediaMetadataCompat.METADATA_KEY_TITLE))
                    .setContentText(mediaMetadata.getText(MediaMetadataCompat.METADATA_KEY_ARTIST));

            if (mediaMetadata.containsKey(MediaMetadataCompat.METADATA_KEY_ART)) {
                builder.setLargeIcon(mediaMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ART));
            } else if (mediaMetadata.containsKey(MediaMetadataCompat.METADATA_KEY_ART_URI)) {
                retrieveArtForNotification(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ART_URI));
            }
        } else {
            builder.setContentTitle("N/A").setContentText("N/A");
        }


        builder.setContentIntent(mediaSession.getController().getSessionActivity())

                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_play_notification);

                if (mediaPlayer.isPlaying()) {
                    builder.addAction(new NotificationCompat.Action(R.drawable.ic_pause, getString(R.string.pause),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE)));
                } else {
                    builder.addAction(new NotificationCompat.Action(R.drawable.ic_play, getString(R.string.play),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY)));
                }
                builder.addAction(new NotificationCompat.Action(R.drawable.ic_stop, getString(R.string.stop),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP)));

                builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP)));

        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind "+ intent.getAction());
        return super.onBind(intent);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Log.d(LOG_TAG, "onGetRoot "+ clientPackageName);
        return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(LOG_TAG, "onLoadChildren "+ parentId);
        result.sendResult(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand "+ intent);
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(LOG_TAG, "stopService "+ name.getAction());
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy ");
        super.onDestroy();
        artService.shutdown();
        mediaPlayer.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "MediaPlayer.onCompletion ");
        updatePlaybackState();
        startForeground(PLAYER_NOTIFICATION_ID, buildNotification());
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "MediaPlayer.onPrepared ");
        mp.start();
        mediaSession.setMetadata(new MediaMetadataCompat.Builder(mediaSession.getController().getMetadata()).putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration()).build());
        artLoading = false;
        startForeground(PLAYER_NOTIFICATION_ID, buildNotification());
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d(LOG_TAG, "MediaPlayer.onAudioFocusChange " + focusChange);
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            mediaSession.getController().getTransportControls().pause();
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        updatePlaybackState();
        Log.d(LOG_TAG, "MediaPlayer.onInfo " + what);
        return false;
    }

    @Override
    public void onTimedText(MediaPlayer mp, TimedText text) {
        Log.d(LOG_TAG, "MediaPlayer.onTimedText " + text.getText());
    }


    class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Log.d(LOG_TAG, "MediaSessionCallback.onMediaButtonEvent " + mediaButtonEvent.getAction()+ "/" + keyEvent);
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY) {
                onPlay();
                return true;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                onPause();
                return true;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_STOP) {
                onStop();
                return true;
            }

            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            Log.d(LOG_TAG, "MediaSessionCallback.onCommand " + command);
            super.onCommand(command, extras, cb);
            if (COMMAND_UPDATE_PLAYBACK_STATE.equals(command)) {
                updatePlaybackState();
            }else if (COMMAND_GET_CURRENT_POSITION.equals(command)) {
                updatePlaybackState();
                Bundle result = new Bundle();
                result.putInt(EXTRA_CURRENT_POSITION, mediaPlayer.getCurrentPosition());
                cb.send(0, result);
            }
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mediaPlayer.seekTo(pos, SEEK_CLOSEST);
            } else {
                mediaPlayer.seekTo((int)pos);
            }
            updatePlaybackState();
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            Log.d(LOG_TAG, "MediaSessionCallback.onPlayFromUri " + uri);
            super.onPlayFromUri(uri, extras);
            try {
                mediaPlayer.reset();
                if (uri.getAuthority().equals("android_asset")) {
                    mediaPlayer.setDataSource(getAssets().openFd(uri.getPath().substring(1)));
                } else {
                    mediaPlayer.setDataSource(uri.toString());
                }
                mediaPlayer.prepareAsync();

                MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, extras.getString(Constant.ALBUM))
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, extras.getString(Constant.ARTIST))
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, extras.getString(Constant.TRACK))
                        .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, extras.getString(Constant.COVER))
                       .build();
                artLoading = false;
                mediaSession.setMetadata(metadata);
                int focus = audioManager.requestAudioFocus(NativeAudioService.this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (focus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ) {
                    startService(new Intent(NativeAudioService.this, NativeAudioService.class));
                    mediaSession.setActive(true);
                    startForeground(PLAYER_NOTIFICATION_ID, buildNotification());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "MediaSessionCallback.onPlayFromUri Error " + uri +" " + e.getMessage(), e);
            }
        }

        @Override
        public void onPlay() {
            Log.d(LOG_TAG, "MediaSessionCallback.onPlay ");
            super.onPlay();
            audioManager.requestAudioFocus(NativeAudioService.this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            startService(new Intent(NativeAudioService.this, NativeAudioService.class));
            mediaSession.setActive(true);
            mediaPlayer.start();
            startForeground(PLAYER_NOTIFICATION_ID, buildNotification());
            updatePlaybackState();
        }

        @Override
        public void onStop() {
            Log.d(LOG_TAG, "MediaSessionCallback.onStop ");
            super.onStop();
            audioManager.abandonAudioFocus(NativeAudioService.this);
            stopSelf();
            mediaSession.setActive(false);
            mediaPlayer.stop();
            stopForeground(true);
        }

        @Override
        public void onPause() {
            Log.d(LOG_TAG, "MediaSessionCallback.onPause ");
            super.onPause();
            mediaPlayer.pause();
            startForeground(PLAYER_NOTIFICATION_ID, buildNotification());
            updatePlaybackState();
        }

    }
}