package com.chaemil.hgms.fragment;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.DownloadsAdapter;
import com.chaemil.hgms.model.Download;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.utils.FileUtils;
import com.chaemil.hgms.utils.GAUtils;
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

    private final Handler handler = new Handler(Looper.getMainLooper());
    private DownloadManager downloadManager;
    private DownloadsAdapter downloadsAdapter;
    private View emptyView;
    private RecyclerView recyclerView;

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
        }

    };

    private void setupUI() {
        int columns = getResources().getInteger(R.integer.archive_columns);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(columns, 1));

        DownloadsAdapter.Listener clickListener = new DownloadsAdapter.Listener() {
            @Override
            public void onItemClick(Download download) {
                if (download.isPaused()) {
                    downloadManager.resumeBatch(download.getBatchId());
                } else {
                    downloadManager.pauseBatch(download.getBatchId());
                }
                queryForDownloads();
            }
        };
        downloadsAdapter = new DownloadsAdapter(getActivity(), ((MainActivity) getActivity()),
                new ArrayList<Download>(), clickListener);
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
            int columns = getResources().getInteger(R.integer.archive_columns);
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(columns, 1));
        }
    }

    @Override
    public void onQueryResult(List<Download> downloads) {
        downloadsAdapter.updateDownloads(downloads);
        emptyView.setVisibility(downloads.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
