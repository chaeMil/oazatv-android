package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.SongsAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Song;
import com.chaemil.hgms.model.SongGroup;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.GAUtils;
import com.futuremind.recyclerviewfastscroll.FastScroller;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chaemil on 1.11.16.
 */

public class SongsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<SongGroup> songGroups = new ArrayList<>();
    private RecyclerView songsList;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout connectionErrorWrapper;
    private SongsAdapter adapter;
    private SongFragment songFragment;
    private MainActivity mainActivity;
    private FastScroller fastScroller;
    private ArrayList<Song> songs = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        getSongs();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.songs_fragment, container, false);

        getUI(rootView);
        setupUI();

        return rootView;
    }

    @Override
    public void onDestroy() {
        mainActivity = null;
        super.onDestroy();
    }

    private void setupUI() {
        swipeRefresh.setOnRefreshListener(this);

        if (getActivity() != null) {

            if (songs != null) {
                setupAdapter();
            }

            adjustLayout();
        }
    }

    public void adjustLayout() {

    }

    private void setupAdapter() {
        adapter = new SongsAdapter(mainActivity, mainActivity, this, songs);
        songsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        songsList.setAdapter(adapter);
        fastScroller.setRecyclerView(songsList);
    }

    private void getUI(ViewGroup rootView) {
        songsList = (RecyclerView) rootView.findViewById(R.id.grid_view);
        progress = (RelativeLayout) rootView.findViewById(R.id.progress);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        fastScroller = (FastScroller) rootView.findViewById(R.id.fastscroll);
        connectionErrorWrapper = (LinearLayout) rootView.findViewById(R.id.connection_error_wrapper);
    }

    private void getSongs() {
        showProgress();
        JsonObjectRequest getSongs = RequestFactory.getSongs(this);
        RequestService.getRequestQueue().add(getSongs);
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_SONGS:
                ArrayList<SongGroup> songGroups = ResponseFactory.parseSongs(response);
                if (songGroups != null && songGroups.size() > 0) {
                    for(SongGroup songGroup : songGroups) {
                        for(Song song : songGroup.getSongs()) {
                            songs.add(song);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                hideProgress();
                swipeRefresh.setRefreshing(false);
                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        super.onErrorResponse(exception, requestType);
        swipeRefresh.setRefreshing(false);
        connectionErrorWrapper.setVisibility(View.VISIBLE);
        hideProgress();
    }

    @Override
    public void onRefresh() {
        songGroups.clear();
        adapter.notifyDataSetChanged();
        getSongs();
    }

    public void setSongFragment(SongFragment songFragment) {
        this.songFragment = songFragment;
    }

    public SongFragment getSongFragment() {
        return songFragment;
    }
}
