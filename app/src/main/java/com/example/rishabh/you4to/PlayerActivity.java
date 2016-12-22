package com.example.rishabh.you4to;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PlayerActivity extends AppCompatActivity
        implements Response.Listener<String>, Response.ErrorListener {

    private RequestQueue queue;
    private ArrayList<JSONObject> json;
    private SimpleExoPlayer player;
    private PlaybackControlView playerView;
    private PlayerService mService;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        queue = Volley.newRequestQueue(this);
        json = new ArrayList<>();

        if (player != null)
            player.release();

        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);

        LoadControl loadControl = new DefaultLoadControl();

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        player.setPlayWhenReady(true);
        playerView = ((PlaybackControlView) findViewById(R.id.player_view));
        playerView.setPlayer(player);
        queue.add(getServerData(getIntent().getExtras().getString(Intent.EXTRA_TEXT)));
    }

    private StringRequest getServerData(String getUrl) {

        final String url = getUrl;

        StringRequest jsonRequest = new StringRequest(Request.Method.POST,
                "https://you4to.herokuapp.com/api/get",
                this, this) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("link", url);
                return params;
            }
        };

        return jsonRequest;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        //player.release();
    }

    // Response.Listener override
    @Override
    public void onResponse(String response) {
        Log.e("Request complete", response);
        try {
            json.add(new JSONObject(response));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Uri uri;
        try {
            uri = Uri.parse(json.get(json.size() - 1).getString("download_music"));
        } catch (JSONException e) {
            uri = null;
            e.printStackTrace();
        }

        JSONObject passJS = json.get(json.size()-1);
        Intent intent = new Intent(this, PlayerService.class);
//        Iterator<String> keys = passJS.keys();
//        while(keys.hasNext()){
//            String key = keys.next();
//            try {
//                intent.putExtra(key, passJS.getString(key));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
        if(!mBound) {
            if (PlayerService.isRunning()) {
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                mService.addSongJson(passJS);
            } else {
                startService(intent);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                if(mBound)  Log.e("Bounded", "True");
                mService.addSongJson(passJS);
                mService.startPlayback();
            }
        }


        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "You4To"), bandwidthMeter);
// Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
// This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(uri,
                dataSourceFactory, extractorsFactory, null, null);

        //ConcatenatingMediaSource audioList = new ConcatenatingMediaSource(videoSource);
// Prepare the player with the source.
        player.prepare(videoSource);
    }

    // Response.ErrorListener override
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("Response", error.toString());
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
