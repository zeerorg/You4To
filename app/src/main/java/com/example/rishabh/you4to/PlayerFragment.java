package com.example.rishabh.you4to;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlaybackControlView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PlayerFragment extends Fragment
        implements Response.Listener<String>, Response.ErrorListener, View.OnClickListener, MainActivity.ServiceCallbacks {

    private RequestQueue queue;
    private View v;
    private JSONObject json;
    private PlaybackControlView playerView;
    private PlayerService mService;
    private boolean mBound = false;
    private TextView title;
    private boolean retryConnect = false;

    private String playlistUrl;
    private String url;
    private int playlistPosition = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_player, container, false);
        playerView = (PlaybackControlView) v.findViewById(R.id.player_view);
        title = (TextView) v.findViewById(R.id.Title);
        playerView.setShowDurationMs(0);

        Intent intent = new Intent(getContext(), PlayerService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        queue = Volley.newRequestQueue(getContext());

        if (!getArguments().getString(Intent.EXTRA_TEXT).equals("")) {
            url = getArguments().getString(Intent.EXTRA_TEXT);

            if (url.contains("list=")) {
                url = url.substring(url.indexOf("http"));
                playlistUrl = url;
                playlistPosition = 1;
                addPlaylist(playlistUrl, playlistPosition);
            } else {
                playlistPosition = -1;
                queue.add(getServerData(url));
            }
        }
        return v;
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

    private void addPlaylist(String getUrl, int playlistPosition) {
        final String url = getUrl;

        final int pos = playlistPosition;

        StringRequest jsonRequest = new StringRequest(Request.Method.POST,
                "https://you4to.herokuapp.com/api/get/playlist",
                this, this) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("link", url);
                params.put("position", pos + "");
                return params;
            }
        };
        queue.add(jsonRequest);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBound) {
            getContext().unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     * Response.Listener override
     */
    @Override
    public void onResponse(String response) {
        Log.e("Request complete", response);
        try {
            json = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (PlayerService.isPlaying()) {
            mService.addSongJson(json);
        } else {
            mService.addSongJson(json);
            mService.startPlayback();
        }
        title.setText(mService.getTitle());

        if (playlistPosition > 10) {  // Only curate top 10 songs from playlist
            playlistPosition = -1;
        }

        if (playlistPosition != -1) {
            playlistPosition += 1;
            addPlaylist(playlistUrl, playlistPosition);
        }
    }

    /**
     * Response.ErrorListener override
     */
    @Override
    public void onErrorResponse(VolleyError error) {
        if(!retryConnect){
            if (playlistPosition != -1)
                addPlaylist(playlistUrl, playlistPosition);
            else
                queue.add(getServerData(url));
        } else {
            Log.e("Response", error.toString());
            Snackbar.make(v, error.toString(), Snackbar.LENGTH_LONG)
                    .setAction("Retry", this)
                    .show();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        private SimpleExoPlayer player;

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            player = mService.getPlayer();
            playerView.setPlayer(player);
            title.setText(mService.getTitle());
            mService.setCallbacks(PlayerFragment.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onClick(View view) {
        queue.add(getServerData(getArguments().getString(Intent.EXTRA_TEXT)));
    }

    @Override
    public void doSomething(String textUpdate) {
        title.setText(textUpdate);
    }
}
