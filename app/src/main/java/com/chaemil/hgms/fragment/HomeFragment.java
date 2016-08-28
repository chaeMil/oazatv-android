package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.homepage_sections.SectionContinueWatching;
import com.chaemil.hgms.adapter.homepage_sections.SectionFeatured;
import com.chaemil.hgms.adapter.homepage_sections.SectionNewVideos;
import com.chaemil.hgms.adapter.homepage_sections.SectionPopularVideos;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.Homepage;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.RequestService;
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
    private GridLayoutManager gridLayoutManager;
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
        List<Video> notFullyWatchedVideos = Video.getNotFullyWatchedVideos(true);
        for (Video video : notFullyWatchedVideos) {
            if (video.getCurrentTime() / 1000 > 30 ) { //more than 30 seconds watched
                videosToContinueWatching.add(video);
            }
        }

        if (NetworkUtils.isConnected(getActivity())) {
            JsonObjectRequest homepage = RequestFactory.getHomepage(this);
            RequestService.getRequestQueue().add(homepage);
        } else {
            ((MainActivity) getActivity()).getMainFragment().hideSplash(true);
        }
    }

    private void setupSections() {

        int sectionCount = 0;

        if (videosToContinueWatching.size() != 0) {
            adapter.addSection(new SectionContinueWatching(getContext(), mainActivity,
                    videosToContinueWatching));

            sectionCount += 1;
        }

        if (homepage != null) {

            if (homepage.featured.size() != 0) {
                ArrayList<ArchiveItem> videos = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    videos.add(homepage.featured.get(i));
                }
                adapter.addSection(new SectionFeatured(getContext(), mainActivity, videos));

                sectionCount += 1;
            }

            if (homepage.newestVideos.size() != 0) {
                ArrayList<Video> videos = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    videos.add(homepage.newestVideos.get(i));
                }
                adapter.addSection(new SectionNewVideos(getContext(), mainActivity, videos));

                sectionCount += 1;
            }

            if (homepage.popularVideos.size() != 0) {
                ArrayList<Video> videos = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    videos.add(homepage.popularVideos.get(i));
                }
                adapter.addSection(new SectionPopularVideos(getContext(), mainActivity, videos));

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

            } else {
                if (((MainActivity) getActivity()).getMainFragment() != null) {
                    ((MainActivity) getActivity()).getMainFragment().showContinue();
                }
            }

            MainFragment mainFragment = ((MainActivity) getActivity()).getMainFragment();
            if (mainFragment != null && init) {
                mainFragment.hideSplash(true);
            }

            adjustLayout();
        }
    }

    private void setupGridManager() {
        final int columns = getResources().getInteger(R.integer.archive_columns);
        gridLayoutManager = new GridLayoutManager(getActivity(), columns);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(adapter.getSectionItemViewType(position)){
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
                        return columns;
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_FOOTER:
                        return columns;
                    default:
                        return 1;
                }
            }
        });

        homepageList.setLayoutManager(gridLayoutManager);
    }

    private void getUI(ViewGroup rootView) {
        homepageList = (RecyclerView) rootView.findViewById(R.id.home_list);
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

        if (!init) {
            initRetry -= 1;

            if (initRetry <= 0) {
                if (getActivity() != null
                        && ((MainActivity) getActivity()).getMainFragment() != null) {

                    ((MainActivity) getActivity()).getMainFragment().goToDownloaded();
                    ((MainActivity) getActivity()).getMainFragment().hideSplash(true);
                    SuperToast.create(getActivity(), getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            } else {
                JsonObjectRequest request = RequestFactory.getHomepage(this);
                RequestService.getRequestQueue().add(request);
            }
        }
    }
}
