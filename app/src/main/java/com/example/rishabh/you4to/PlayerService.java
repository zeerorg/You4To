package com.example.rishabh.you4to;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlayerService extends Service implements ExoPlayer.EventListener {

    private final IBinder mBinder = new LocalBinder();
    private ArrayList<JSONObject> json = new ArrayList<>();
    private SimpleExoPlayer player;
    private int position;
    private static boolean running = false;
    private static boolean playback = false;

    @Override
    public void onCreate() {
        Log.e("Service", "Started");
        running = true;
        position = 0;

        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);

        LoadControl loadControl = new DefaultLoadControl();

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        player.addListener(this);
        player.setPlayWhenReady(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Service", "Ended");
    }

    public PlayerService() {
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        PlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public void addSongJson(JSONObject js){
        json.add(js);
        Log.e("Service", "SOng Added");
    }

    public static boolean isRunning(){
        return running;
    }

    public void startPlayback(){
        playback = true;
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "You4To"), bandwidthMeter);
// Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
// This is the MediaSource representing the media to be played.
        MediaSource videoSource = null;
        try {
            videoSource = new ExtractorMediaSource(Uri.parse(json.get(position).getString("download_music")),
                    dataSourceFactory, extractorsFactory, null, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }

// Prepare the player with the source.
        player.prepare(videoSource);
    }

    public static boolean isPlaying(){
        return playback;
    }

    public SimpleExoPlayer getPlayer(){
        return player;
    }

    /** ExoPlayer events */

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if(playbackState == ExoPlayer.STATE_ENDED){
            position += 1;
            if(position == json.size()){
                Log.e("Service", "Finished playback");
                player.release();
                stopSelf();
            } else {
                Log.e("Service", "Track Change");
                startPlayback();
            }
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }
}
