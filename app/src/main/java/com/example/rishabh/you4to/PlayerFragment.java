package com.example.rishabh.you4to;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PlayerFragment extends Fragment
        implements Response.Listener<String>, Response.ErrorListener, MainActivity.FragmentInterface {

    private RequestQueue queue;
    private JSONObject json;
    private PlaybackControlView playerView;
    private PlayerService mService;
    private boolean mBound = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_player, container, false);
        playerView = (PlaybackControlView) v.findViewById(R.id.player_view);
        playerView.setShowDurationMs(0);

        Intent intent = new Intent(getContext(), PlayerService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        queue = Volley.newRequestQueue(getContext());

        // TODO: How data will be passed between fragments
        if (!getArguments().getString(Intent.EXTRA_TEXT).equals(""))
            queue.add(getServerData(getArguments().getString(Intent.EXTRA_TEXT)));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBound) {
            getContext().unbindService(mConnection);
            mBound = false;
        }
    }

    /** Response.Listener override*/
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
    }

    /** Response.ErrorListener override*/
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("Response", error.toString());
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            playerView.setPlayer(mService.getPlayer());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void addQueue(String url) {
        queue.add(getServerData(url));
    }
}
