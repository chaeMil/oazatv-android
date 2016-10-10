package com.chaemil.hgms.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
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
import com.chaemil.hgms.adapter.homepage_sections.SectionAppVersion;
import com.chaemil.hgms.adapter.homepage_sections.SectionContinueWatching;
import com.chaemil.hgms.adapter.homepage_sections.SectionFeatured;
import com.chaemil.hgms.adapter.homepage_sections.SectionNewAlbums;
import com.chaemil.hgms.adapter.homepage_sections.SectionNewVideos;
import com.chaemil.hgms.adapter.homepage_sections.SectionPopularVideos;
import com.chaemil.hgms.adapter.homepage_sections.SectionWebView;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.Homepage;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.GAUtils;
import com.chaemil.hgms.utils.NetworkUtils;
import com.github.johnpersano.supertoasts.SuperToast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * Created by chaemil on 2.12.15.
 */
public class HomeFragment extends BaseFragment implements RequestFactoryListener {

    private boolean init = false;
    private int initRetry = 2;
    private Homepage homepage;
    private RecyclerView homepageList;
    private StaggeredGridLayoutManager gridLayoutManager;
    private ArrayList<Video> videosToContinueWatching = new ArrayList<>();
    private SectionedRecyclerViewAdapter adapter;
    private MainActivity mainActivity;
    private int firstVisiblePosition = 0;

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
    public void onResume() {
        super.onResume();

        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.HOME_FRAGMENT);

        GAUtils.sendGAScreen(
                ((OazaApp) getActivity().getApplication()),
                "Home");
    }

    private void getData() {
        showProgress();
        List<Video> notFullyWatchedVideos = Video.getNotFullyWatchedVideos(true);
        for (Video video : notFullyWatchedVideos) {
            if (video.getCurrentTime() / 1000 > 30 ) { //more than 30 seconds watched
                videosToContinueWatching.add(video);
            }
        }

        if (NetworkUtils.isConnected(getActivity())) {
            JsonObjectRequest homepage = RequestFactory.getHomepage(this);
            RequestService.getRequestQueue().add(homepage);
        }
    }

    private void setupSections() {

        int sectionCount = 0;


        if (homepage != null) {
            if (homepage.latestAndroidAppVersion > BuildConfig.VERSION_CODE) {
                adapter.addSection(new SectionAppVersion(getContext(), mainActivity));

                sectionCount += 1;
            }
        }

        if (videosToContinueWatching.size() != 0) {
            adapter.addSection(new SectionContinueWatching(getContext(), mainActivity,
                    videosToContinueWatching));

            sectionCount += 1;
        }

        /*adapter.addSection(new SectionWebView(getContext(),
                mainActivity, Constants.HTTP + Constants.DOMAIN + "app_webviews/android.html"));
        sectionCount += 1;*/

        if (homepage != null) {

            if (homepage.featured.size() != 0) {
                adapter.addSection(new SectionFeatured(getContext(),
                        mainActivity,
                        homepage.featured));

                sectionCount += 1;
            }

            if (homepage.newestVideos.size() != 0) {
                adapter.addSection(new SectionNewVideos(getContext(),
                        mainActivity,
                        homepage.newestVideos));

                sectionCount += 1;
            }

            if (homepage.newestAlbums.size() != 0) {
                adapter.addSection(new SectionNewAlbums(getContext(),
                        mainActivity,
                        homepage.newestAlbums));

                sectionCount += 1;
            }

            if (homepage.popularVideos.size() != 0) {
                adapter.addSection(new SectionPopularVideos(getContext(),
                        mainActivity,
                        homepage.popularVideos));

                sectionCount += 1;
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
    }

    public void adjustLayout() {
        if (isAdded()) {
            setupGridManager();
        }
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_HOMEPAGE:

                hideProgress(500);
                homepage = ResponseFactory.parseHomepage(response);
                init = true;
                initRetry = 2;
                setupUI();

                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        super.onErrorResponse(exception, requestType);

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
}
