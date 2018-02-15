package com.chaemil.hgms.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.ui.adapter.ArchiveAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.EndlessScrollListener;
import com.chaemil.hgms.utils.SmartLog;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by chaemil on 2.12.15.
 */
public class ArchiveFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<ArchiveItem> archive = new ArrayList<>();
    private RecyclerView archiveGridView;
    private ProgressBar progress;
    private ArchiveAdapter archiveAdapter;
    private ProgressBar endlessProgress;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout connectionErrorWrapper;
    private GridLayoutManager gridLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getArchivePage(1);

        TypedValue tv = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.archive_fragment, container, false);

        getUI(rootView);
        setupUI();

        return rootView;

    }

    private void getArchivePage(final int pageNumber) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SmartLog.d("getArchivePage", String.valueOf(pageNumber));
                JsonObjectRequest getArchivePage = RequestFactory.getArchive(ArchiveFragment.this, pageNumber);
                RequestService.getRequestQueue().add(getArchivePage);
            }
        }, 750);
    }

    private void setupUI() {
        if (isAdded()) {
            createAdapter();
            setupAdapter();
            adjustLayout();
        }
    }

    private void createAdapter() {
        if (archiveAdapter == null) {
            archiveAdapter = new ArchiveAdapter(getActivity(),
                    AdapterUtils.getArchiveLayout(getActivity()),
                    archive);
        }
    }

    private void setupAdapter() {
        if (archiveAdapter != null) {
            archiveGridView.setAdapter(archiveAdapter);
            setupGridManager();
            archiveGridView.addOnScrollListener(new EndlessScrollListener(gridLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    getArchivePage(page + 1);
                    endlessProgress.setVisibility(View.VISIBLE);
                }
            });
            swipeRefresh.setOnRefreshListener(this);
            archiveAdapter.notifyDataSetChanged();
        }
    }

    private void setupGridManager() {
        final int columns = getResources().getInteger(R.integer.archive_columns);
        if (gridLayoutManager == null) {
            gridLayoutManager = new GridLayoutManager(getActivity(), columns);
            archiveGridView.setLayoutManager(gridLayoutManager);
        } else {
            gridLayoutManager.setSpanCount(columns);
        }
    }

    public void adjustLayout() {
        if (isAdded()) {
            setupGridManager();
        }
    }

    private void getUI(ViewGroup rootView) {
        archiveGridView = (RecyclerView) rootView.findViewById(R.id.archive_grid_view);
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
                    endlessProgress.setVisibility(View.GONE);
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
        archiveAdapter.notifyDataSetChanged();
        getArchivePage(1);
    }

    public void notifyAudioDeleted() {
        if (archiveAdapter != null) {
            archiveAdapter.notifyDataSetChanged();
        }
    }
}
