package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.sections.SongsSection;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Song;
import com.chaemil.hgms.model.SongGroup;
import com.chaemil.hgms.service.RequestService;
import com.futuremind.recyclerviewfastscroll.FastScroller;

import org.json.JSONObject;

import java.util.ArrayList;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * Created by chaemil on 1.11.16.
 */

public class SongsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<SongGroup> songGroups = new ArrayList<>();
    private RecyclerView songsList;
    private LinearLayout connectionErrorWrapper;
    private SectionedRecyclerViewAdapter adapter;
    private SongFragment songFragment;
    private MainActivity mainActivity;
    private FastScroller fastScroller;
    private ArrayList<Song> songs = new ArrayList<>();
    private RelativeLayout mainLayout;
    private StaggeredGridLayoutManager layoutManager;

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
    public void onResume() {
        super.onResume();
        adjustLayout();
    }

    public void exit() {
        mainActivity = null;
    }

    private void setupUI() {
        if (getActivity() != null) {

            if (songs != null) {
                setupAdapter();
            }

            adjustLayout();
        }
    }

    public void adjustLayout() {
        if (isAdded()) {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                layoutManager.setSpanCount(calculateColumns());
            }
        }
    }

    private void setupAdapter() {
        if (adapter == null) {
            adapter = new SectionedRecyclerViewAdapter();
        }
        if (layoutManager == null) {
            layoutManager = new StaggeredGridLayoutManager(calculateColumns(),
                    StaggeredGridLayoutManager.VERTICAL);
        }
        songsList.setLayoutManager(layoutManager);
        songsList.setAdapter(adapter);
        fastScroller.setRecyclerView(songsList);
    }

    private void getUI(ViewGroup rootView) {
        mainLayout = (RelativeLayout) rootView.findViewById(R.id.main_layout);
        songsList = (RecyclerView) rootView.findViewById(R.id.grid_view);
        progress = (RelativeLayout) rootView.findViewById(R.id.progress);
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
                    adapter.removeAllSections();

                    for(SongGroup songGroup : songGroups) {
                        SongsSection songsSection = new SongsSection(this, getActivity(), songGroup);
                        adapter.addSection(songsSection);
                    }
                }
                adapter.notifyDataSetChanged();
                hideProgress();
                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        super.onErrorResponse(exception, requestType);
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
