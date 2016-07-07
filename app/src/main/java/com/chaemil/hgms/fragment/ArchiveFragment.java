package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.ArchiveAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.EndlessScrollListener;
import com.chaemil.hgms.utils.GAUtils;
import com.chaemil.hgms.utils.SmartLog;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by chaemil on 2.12.15.
 */
public class ArchiveFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<ArchiveItem> archive = new ArrayList<>();
    private GridView archiveGridView;
    private ProgressBar progress;
    private ArchiveAdapter archiveAdapter;
    private ProgressBar endlessProgress;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout connectionErrorWrapper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getArchivePage(1);

        TypedValue tv = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.ARCHIVE_FRAGMENT);

        GAUtils.sendGAScreen(
                ((OazaApp) getActivity().getApplication()),
                "Archive");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.archive_fragment, container, false);


        getUI(rootView);
        setupUI();

        return rootView;

    }

    private void getArchivePage(int pageNumber) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "getArchivePage", String.valueOf(pageNumber));
        JsonObjectRequest getArchivePage = RequestFactory.getArchive(this, pageNumber);
        RequestService.getRequestQueue().add(getArchivePage);
    }

    private void setupUI() {
        archiveAdapter = new ArchiveAdapter(getActivity(),
                R.layout.archive_item,
                (MainActivity) getActivity(),
                archive);

        archiveGridView.setAdapter(archiveAdapter);
        archiveGridView.setOnScrollListener(endlessScrollListener());
        swipeRefresh.setOnRefreshListener(this);

        adjustLayout();
    }

    private EndlessScrollListener endlessScrollListener() {
        return new EndlessScrollListener(0, 0) {
            @Override
            public void onLoadMore(final int page, int totalItemsCount) {

                endlessProgress.setVisibility(View.VISIBLE);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getArchivePage(page);
                    }
                }, 500);

            }
        };
    }

    public void adjustLayout() {

        if (isAdded()) {
            int columns = getResources().getInteger(R.integer.archive_columns);
            archiveGridView.setNumColumns(columns);
        }

    }

    private void getUI(ViewGroup rootView) {
        archiveGridView = (GridView) rootView.findViewById(R.id.archive_grid_view);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        endlessProgress = (ProgressBar) rootView.findViewById(R.id.endless_progress);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        connectionErrorWrapper = (LinearLayout) rootView.findViewById(R.id.connection_error_wrapper);
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_ARCHIVE:

                ArrayList<ArchiveItem> newItems;
                newItems = ResponseFactory.parseArchive(response);

                if (newItems != null) {
                    archive.addAll(newItems);
                    archiveAdapter.notifyDataSetChanged();
                    progress.setVisibility(View.GONE);
                    endlessProgress.setVisibility(View.VISIBLE);
                    swipeRefresh.setRefreshing(false);
                    connectionErrorWrapper.setVisibility(View.GONE);
                }


                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        super.onErrorResponse(exception, requestType);
        swipeRefresh.setRefreshing(false);
        connectionErrorWrapper.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        endlessProgress.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        archive.clear();
        getArchivePage(1);
    }
}
