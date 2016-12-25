package com.example.rishabh.you4to;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.SimpleExoPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rishabh on 12/23/16.
 */

public class CurrentPlaylistFragment extends BaseFragment
        implements MyRecyclerView.ListAdapter.ItemClickCallback{

    private RecyclerView recyclerView;
    private MyRecyclerView.ListAdapter adapter;
    private List<MyRecyclerView.ListItem> listItems;
    private MyRecyclerView.ListData listData;

    private PlayerService mService;
    private boolean mBound = false;

    public CurrentPlaylistFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_recycler, container, false);
        listData = new MyRecyclerView.ListData();
        listItems = listData.getListData();

        recyclerView = (RecyclerView) v.findViewById(R.id.rec_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new MyRecyclerView.ListAdapter(listItems, getContext());
        recyclerView.setAdapter(adapter);
        adapter.setItemClickCallback(this);

        Intent intent = new Intent(getContext(), PlayerService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        return v;
    }

    @Override
    public void onItemClick(int p) {
//        MyRecyclerView.ListItem item = listData.getListData().get(p);
//        Intent intent = new Intent(getContext(), Detail.class);
//        intent.putExtra(Intent.EXTRA_TEXT, item.getText());
//        startActivity(intent);
        mService.setPosition(p);
        mService.startPlayback();
        adapter.getViews().get(p).getContainer().setBackgroundColor(Color.parseColor("#fdfdfd"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unbindService(mConnection);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        private SimpleExoPlayer player;
        private ArrayList<JSONObject> json;

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            player = mService.getPlayer();
            json = mService.getJson();

            String title[] = new String[json.size()];

            for(int x = 0; x < json.size(); x++){
                try {
                    title[x] = json.get(x).getString("title");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            listData.setListData(title);
            listItems = listData.getListData();
            adapter.setListData(listItems);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}