package com.chaemil.hgms.ui.mobile.fragment;

import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chaemil.hgms.R;
import com.chaemil.hgms.ui.mobile.adapter.DownloadsAdapter;
import com.chaemil.hgms.model.Download;
import com.chaemil.hgms.utils.QueryForDownloadsAsyncTask;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaemil on 2.12.15.
 */
public class DownloadedFragment extends BaseFragment implements QueryForDownloadsAsyncTask.Callback {

    public static String DOWNLOAD_MANAGER_ONCHANGE = "download_manager_onchange";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private DownloadManager downloadManager;
    private DownloadsAdapter downloadsAdapter;
    private View emptyView;
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager layoutManager;
    private RelativeLayout mainLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.downloaded_fragment, container, false);

        getUI(rootView);
        setup();
        setupUI();
        queryForDownloads();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        adjustLayout();
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        adjustLayout();
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getContentResolver()
                .registerContentObserver(downloadManager.getDownloadsWithoutProgressUri(),
                        true,
                        updateSelf);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().getContentResolver().unregisterContentObserver(updateSelf);
    }

    private void getUI(ViewGroup rootView) {
        mainLayout = (RelativeLayout) rootView.findViewById(R.id.main_layout);
        emptyView = rootView.findViewById(R.id.none_downloaded);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.main_downloads_list);
    }

    private void setup() {
        downloadManager = DownloadManagerBuilder.from(getActivity()).build();
    }

    private final ContentObserver updateSelf = new ContentObserver(handler) {

        @Override
        public void onChange(boolean selfChange) {
            queryForDownloads();
            getActivity().sendBroadcast(new Intent(DOWNLOAD_MANAGER_ONCHANGE));
        }

    };

    private void setupUI() {
        layoutManager = new StaggeredGridLayoutManager(calculateColumns(), 1);
        recyclerView.setLayoutManager(layoutManager);
        downloadsAdapter = new DownloadsAdapter(getActivity(), new ArrayList<Download>());
        recyclerView.setAdapter(downloadsAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
    }

    private void queryForDownloads() {
        Query orderedQuery = new Query().orderByLiveness();
        QueryForDownloadsAsyncTask.newInstance(downloadManager, this).execute(orderedQuery);
    }

    public void adjustLayout() {
        if (isAdded()) {
            if (layoutManager != null) {
                layoutManager.setSpanCount(calculateColumns());
            }
            if (downloadsAdapter != null) {
                downloadsAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onQueryResult(List<Download> downloads) {
        downloadsAdapter.updateDownloads(downloads);
        emptyView.setVisibility(downloads.isEmpty() ? View.VISIBLE : View.GONE);
    }
}