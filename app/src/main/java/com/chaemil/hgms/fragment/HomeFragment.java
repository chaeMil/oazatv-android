package com.chaemil.hgms.fragment;

import android.os.Bundle;
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
import com.chaemil.hgms.adapter.HomepageAdapter;
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
import com.chaemil.hgms.utils.SmartLog;
import com.github.johnpersano.supertoasts.SuperToast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaemil on 2.12.15.
 */
public class HomeFragment extends BaseFragment implements RequestFactoryListener {

    private boolean init = false;
    private int initRetry = 2;
    private Homepage homepage;
    private HomepageAdapter listAdapter;
    private RecyclerView homepageList;
    private GridLayoutManager gridLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.home_fragment, container, false);

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
        List<Video> notFullyWatchedVideos = Video.getNotFullyWatchedVideos();
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "notFullyWatchedVideos", notFullyWatchedVideos.toString());

        if (NetworkUtils.isConnected(getActivity())) {
            JsonObjectRequest homepage = RequestFactory.getHomepage(this);
            RequestService.getRequestQueue().add(homepage);
        } else {
            ((MainActivity) getActivity()).getMainFragment().hideSplash(true);
        }
    }

    private void setupUI() {
        if (getActivity() != null) {
            if (homepage != null) {
                listAdapter = new HomepageAdapter(getActivity(), (MainActivity) getActivity(), homepage);

                setupGridManager();

                homepageList.setAdapter(listAdapter);

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
                switch(listAdapter.getItemViewType(position)){
                    case -2: //HEADER
                        return columns;
                    case -1: //ITEM
                        return 1;
                    default:
                        return -1;
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
