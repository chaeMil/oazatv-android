package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.BuildConfig;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.homepage_sections.BaseSection;
import com.chaemil.hgms.adapter.homepage_sections.SectionAppVersion;
import com.chaemil.hgms.adapter.homepage_sections.SectionContinueWatching;
import com.chaemil.hgms.adapter.homepage_sections.SectionFeatured;
import com.chaemil.hgms.adapter.homepage_sections.SectionNewAlbums;
import com.chaemil.hgms.adapter.homepage_sections.SectionNewVideos;
import com.chaemil.hgms.adapter.homepage_sections.SectionPopularVideos;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Homepage;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.GAUtils;
import com.chaemil.hgms.utils.NetworkUtils;
import com.chaemil.hgms.utils.SharedPrefUtils;
import com.github.johnpersano.supertoasts.SuperToast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * Created by chaemil on 2.12.15.
 */
public class HomeFragment extends BaseFragment implements RequestFactoryListener, SwipeRefreshLayout.OnRefreshListener {

    private boolean init = false;
    private int initRetry = 2;
    private Homepage homepage;
    private RecyclerView homepageList;
    private StaggeredGridLayoutManager gridLayoutManager;
    private ArrayList<Video> videosToContinueWatching = new ArrayList<>();
    private SectionedRecyclerViewAdapter adapter;
    private MainActivity mainActivity;
    private int firstVisiblePosition = 0;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.home_fragment, container, false);

        mainActivity = (MainActivity) getActivity();

        getData();
        getUI(rootView);
        setupUI();
        return rootView;

    }

    @Override
    public void onDestroy() {
        mainActivity = null;
        super.onDestroy();
    }

    private void getLocalData() {
        videosToContinueWatching.clear();

        List<Video> notFullyWatchedVideos = Video.getNotFullyWatchedVideos(true);
        Set<String> hiddenVideos = SharedPrefUtils.getInstance(mainActivity).getHiddenVideos();

        for (int i = 0; i < notFullyWatchedVideos.size() && i < 8; i++) {
            Video video = notFullyWatchedVideos.get(i);
            if (video.getCurrentTime() / 1000 > 30 && !hiddenVideos.contains(video.getHash())) { //more than 30 seconds watched
                videosToContinueWatching.add(video);
            }
        }
    }

    private void getData() {
        showProgress();
        getLocalData();

        if (NetworkUtils.isConnected(getActivity())) {
            JsonObjectRequest homepage = RequestFactory.getHomepage(this);
            RequestService.getRequestQueue().add(homepage);
        }
    }

    private void setupSections() {

        int sectionCount = 0;

        if (homepage != null) {
            if (homepage.latestAndroidAppVersion > BuildConfig.VERSION_CODE) {
                adapter.addSection(new SectionAppVersion(getActivity(), mainActivity));

                sectionCount += 1;
            }
        }

        if (videosToContinueWatching.size() != 0) {
            adapter.addSection(SectionContinueWatching.TAG, new SectionContinueWatching(getActivity(),
                    mainActivity, videosToContinueWatching));

            sectionCount += 1;
        }

        /*adapter.addSection(new SectionWebView(getContext(),
                mainActivity, Constants.HTTP + Constants.DOMAIN + "app_webviews/android.html"));
        sectionCount += 1;*/

        if (homepage != null) {

            ArrayList<BaseSection> sections = new ArrayList<>();

            if (homepage.featured.size() != 0) {
                sections.add(new SectionFeatured(getActivity(),
                        mainActivity,
                        homepage.featured));

                sectionCount += 1;
            }

            if (homepage.newestVideos.size() != 0) {
                sections.add(new SectionNewVideos(getActivity(),
                        mainActivity,
                        homepage.newestVideos));

                sectionCount += 1;
            }

            if (homepage.newestAlbums.size() != 0) {
                sections.add(new SectionNewAlbums(getActivity(),
                        mainActivity,
                        homepage.newestAlbums));

                sectionCount += 1;
            }

            if (homepage.popularVideos.size() != 0) {
                sections.add(new SectionPopularVideos(getActivity(),
                        mainActivity,
                        homepage.popularVideos));

                sectionCount += 1;
            }

            Collections.shuffle(sections);
            for (BaseSection section : sections) {
                adapter.addSection(section);
            }
        }
    }

    private void setupUI() {
        if (getActivity() != null) {

            if (homepage != null) {
                adapter = new SectionedRecyclerViewAdapter();
                setupSections();
                setupGridManager();
                homepageList.setAdapter(adapter);
                swipeRefresh.setOnRefreshListener(this);
            }

            adjustLayout();
        }
    }

    private void setupGridManager() {
        final int columns = getResources().getInteger(R.integer.archive_columns);
        gridLayoutManager = new StaggeredGridLayoutManager(columns,
                StaggeredGridLayoutManager.VERTICAL);
        homepageList.setLayoutManager(gridLayoutManager);
    }

    private void getUI(ViewGroup rootView) {
        homepageList = (RecyclerView) rootView.findViewById(R.id.home_list);
        progress = (RelativeLayout) rootView.findViewById(R.id.progress);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
    }

    public void adjustLayout() {
        if (isAdded()) {
            setupGridManager();
        }
    }

    @Override
    public void onSuccessResponse(final JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_HOMEPAGE:

                hideProgress(500);
                swipeRefresh.setRefreshing(false);
                homepage = ResponseFactory.parseHomepage(response);
                init = true;
                initRetry = 2;

                delay(new Runnable() {
                    @Override
                    public void run() {
                        setupUI();
                    }
                }, 500);

                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        super.onErrorResponse(exception, requestType);

        swipeRefresh.setRefreshing(false);
        hideProgress(500);

        if (!init) {
            initRetry -= 1;

            if (initRetry <= 0) {
                if (getActivity() != null
                        && ((MainActivity) getActivity()).getMainFragment() != null) {

                    ((MainActivity) getActivity()).getMainFragment().goToDownloaded();
                    SuperToast.create(getActivity(), getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            } else {
                JsonObjectRequest request = RequestFactory.getHomepage(this);
                RequestService.getRequestQueue().add(request);
            }
        }
    }

    public void refreshContinueWatching() {
        getLocalData();
        if (videosToContinueWatching.size() <= 0) {
            adapter.removeSection(SectionContinueWatching.TAG);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        if (NetworkUtils.isConnected(mainActivity)) {
            adapter.removeAllSections();
            getData();
        }
    }
}
