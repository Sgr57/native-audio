package com.getcapacitor.community.audio;

import static com.getcapacitor.community.audio.Constant.*;

import android.Manifest;
import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

@CapacitorPlugin(
        permissions = {@Permission(strings = {Manifest.permission.MODIFY_AUDIO_SETTINGS})}
)
public class NativeAudio extends Plugin {

    public static final String TAG = "NativeAudio";

    private MediaBrowserCompat mediaBrowser;
    private Track currentTrack;

    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            Log.d(TAG, "onConnected ");
            super.onConnected();
            try {
                MediaControllerCompat mediaController = new MediaControllerCompat(getActivity(), mediaBrowser.getSessionToken());
                MediaControllerCompat.setMediaController(getActivity(), mediaController);
                mediaController.registerCallback(new MediaControllerCompat.Callback() {
                    @Override
                    public void onPlaybackStateChanged(PlaybackStateCompat state) {
                        super.onPlaybackStateChanged(state);
                        String status = Helper.mapPlayerStatus(state);
                        Log.d(TAG, "onPlaybackStateChanged " + state.getState() + "(" + status + ") - " + state.getPosition());
                        JSObject json = new JSObject();
                        json.put("status", status);
                        json.put("position", state.getPosition() / 1000);
                        notifyListeners("playbackStateChanged", json);
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "Error on media controller", e);
            }

        }
    };

    @Override
    public void load() {
        super.load();
        Log.d(TAG, "load ");
        mediaBrowser = new MediaBrowserCompat(getContext(), new ComponentName(getContext(), NativeAudioService.class), connectionCallbacks, null);
        mediaBrowser.connect();
    }

    @Override
    protected void handleOnDestroy() {
        super.handleOnDestroy();
        mediaBrowser.disconnect();
    }

    @PluginMethod
    public void configure(PluginCall call) {
        Log.d(TAG, "configure " + call);
        call.resolve();
    }

    @PluginMethod
    public void preload(final PluginCall call) {
        try {
            Log.d(TAG, "preload " + call);
            currentTrack = new Track();
            if (Boolean.TRUE == call.getBoolean(IS_URL, false)) {
                currentTrack.url = call.getString(ASSET_PATH);
            } else {
                currentTrack.url = "file://android_asset/" + call.getString(ASSET_PATH);
            }
            currentTrack.trackName = call.getString(TRACK, null);
            currentTrack.artistName = call.getString(ARTIST, null);
            currentTrack.coverArtUrl = call.getString(COVER, null);
            currentTrack.albumName = call.getString(ALBUM, null);
            call.resolve();
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void play(final PluginCall call) {
        try {
            long desiredTime = call.getFloat(CURRENT_TIME, 0f).longValue();
            MediaControllerCompat mc = MediaControllerCompat.getMediaController(getActivity());
            Bundle metadata = new Bundle();
            metadata.putString(TRACK, currentTrack.trackName);
            metadata.putString(ARTIST, currentTrack.artistName);
            metadata.putString(COVER, currentTrack.coverArtUrl);
            metadata.putString(ALBUM, currentTrack.albumName);
            mc.getTransportControls().playFromUri(Uri.parse(currentTrack.url), metadata);
            // start from time
            if (desiredTime != 0L) {
                mc.getTransportControls().seekTo(desiredTime * 1000);
            }
            call.resolve();
        }catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void getCurrentTime(final PluginCall call) {
        try {
            MediaControllerCompat mc = MediaControllerCompat.getMediaController(getActivity());
            mc.sendCommand(NativeAudioService.COMMAND_GET_CURRENT_POSITION, null, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    super.onReceiveResult(resultCode, resultData);
                    call.resolve(new JSObject().put(CURRENT_TIME, resultData.getInt(NativeAudioService.EXTRA_CURRENT_POSITION) / 1000));
                }
            });
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void setCurrentTime(final PluginCall call) {
        try {
            long desiredTime = call.getFloat(CURRENT_TIME, 0f).longValue();
            MediaControllerCompat mc = MediaControllerCompat.getMediaController(getActivity());
            mc.getTransportControls().seekTo(desiredTime * 1000);
            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void getDuration(final PluginCall call) {
        try {
            MediaControllerCompat mc = MediaControllerCompat.getMediaController(getActivity());
            if (mc.getMetadata() != null) {
                call.resolve(new JSObject().put(DURATION, mc.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION) / 1000));
            } else {
                call.resolve(new JSObject().put(DURATION, -1));
            }
        } catch (Exception ex) {

            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void loop(final PluginCall call) {
        call.reject("NOT YET IMPLEMENTED");
    }

    @PluginMethod
    public void pause(PluginCall call) {
        try {
            MediaControllerCompat mc = MediaControllerCompat.getMediaController(getActivity());
            mc.getTransportControls().pause();
            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void resume(PluginCall call) {
        try {
            MediaControllerCompat mc = MediaControllerCompat.getMediaController(getActivity());
            mc.getTransportControls().play();
            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void stop(PluginCall call) {
        try {
            MediaControllerCompat mc = MediaControllerCompat.getMediaController(getActivity());
            mc.getTransportControls().stop();
            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void unload(PluginCall call) {
        try {
            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void setVolume(PluginCall call) {
        try {
            MediaControllerCompat mc = MediaControllerCompat.getMediaController(getActivity());
            float volume = call.getFloat(VOLUME, 0.5f);
            if (volume > 1.0f) volume = 1.0f;
            int volumeAbs = (int) (mc.getPlaybackInfo().getMaxVolume() * volume);
            mc.setVolumeTo(volumeAbs, 0);
            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }

    @PluginMethod
    public void getVolume(PluginCall call) {
        try {
            MediaControllerCompat mc = MediaControllerCompat.getMediaController(getActivity());
            float volume = ((float) mc.getPlaybackInfo().getCurrentVolume()) / mc.getPlaybackInfo().getMaxVolume();

            JSObject ret = new JSObject();
            ret.put(VOLUME, volume);
            call.resolve(ret);
        } catch (Exception ex) {
            call.reject(ex.getMessage());
        }
    }
}
