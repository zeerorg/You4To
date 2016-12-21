package com.example.rishabh.you4to;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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

import java.util.HashMap;
import java.util.Map;

public class PlayerActivity extends AppCompatActivity {

    private RequestQueue queue;
    private JSONObject json;
    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        queue = Volley.newRequestQueue(this);
        JSONObject params = new JSONObject();
        try {
            params.put("link", getIntent().getExtras().getString(Intent.EXTRA_TEXT));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest jsonRequest = new StringRequest(Request.Method.POST,
                "https://you4to.herokuapp.com/api/get",
                //"http://httpbin.org/post",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e("Request complete", response);
                        //jsonDisplay.setText("Response: " + response.toString());
                        try {
                            json = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Uri uri;
                        try {
                            uri = Uri.parse(json.getString("download_music"));
                        } catch (JSONException e) {
                            uri = null;
                            e.printStackTrace();
                        }
                        // Measures bandwidth during playback. Can be null if not required.
                        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
                        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(PlayerActivity.this,
                                Util.getUserAgent(PlayerActivity.this, "yourApplicationName"), bandwidthMeter);
// Produces Extractor instances for parsing the media data.
                        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
// This is the MediaSource representing the media to be played.
                        MediaSource videoSource = new ExtractorMediaSource(uri,
                                dataSourceFactory, extractorsFactory, null, null);
// Prepare the player with the source.
                        player.prepare(videoSource);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("link", getIntent().getExtras().getString(Intent.EXTRA_TEXT));
                return params;
            }
        };
        queue.add(jsonRequest);

        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);

// 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

// 3. Create the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        player.setPlayWhenReady(true);
        playerView = ((SimpleExoPlayerView) findViewById(R.id.player_view));
        playerView.setPlayer(player);

    }

    private void getServerData(){

    }

    @Override
    protected void onDestroy() {
        super.onStop();
        player.release();
    }


}
