package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.adapter.songs_sections.SongsSection;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.SongGroup;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.GAUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * Created by chaemil on 1.11.16.
 */

public class SongsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<SongGroup> songGroups = new ArrayList<>();
    private RecyclerView gridView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout connectionErrorWrapper;
    private StaggeredGridLayoutManager gridLayoutManager;
    private SectionedRecyclerViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
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

    private void setupUI() {
        if (getActivity() != null) {

            if (songGroups != null) {
                adapter = new SectionedRecyclerViewAdapter();
                setupSections();
                setupGridManager();
                gridView.setAdapter(adapter);

            }

            adjustLayout();
        }
    }

    private void setupSections() {
        if (songGroups != null) {
            for(SongGroup songGroup : songGroups) {

                if (songGroup.getSongs() != null && songGroup.getSongs().size() > 0) {
                    SongsSection songsSection = new SongsSection(getActivity(), songGroup);
                    adapter.addSection(songsSection);
                }

            }

            adapter.notifyDataSetChanged();
        }
    }

    public void adjustLayout() {
        if (isAdded()) {
            setupGridManager();
        }
    }

    private void setupGridManager() {
        final int columns = getResources().getInteger(R.integer.archive_columns);
        gridLayoutManager = new StaggeredGridLayoutManager(columns,
                StaggeredGridLayoutManager.VERTICAL);
        gridView.setLayoutManager(gridLayoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.SONGS_FRAGMENT);

        GAUtils.sendGAScreen(
                ((OazaApp) getActivity().getApplication()),
                "Songs");
    }

    private void getUI(ViewGroup rootView) {
        gridView = (RecyclerView) rootView.findViewById(R.id.grid_view);
        progress = (RelativeLayout) rootView.findViewById(R.id.progress);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
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
                    this.songGroups.clear();
                    this.songGroups.addAll(songGroups);
                }
                setupSections();
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
}
