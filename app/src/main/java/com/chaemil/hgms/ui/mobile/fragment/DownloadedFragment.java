package com.chaemil.hgms.ui.mobile.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.model.Download;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.ui.mobile.adapter.DownloadsAdapter;
import com.novoda.downloadmanager.DownloadManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaemil on 2.12.15.
 */
public class DownloadedFragment extends BaseFragment {

    public static String DOWNLOAD_MANAGER_ONCHANGE = "download_manager_onchange";
    private DownloadsAdapter downloadsAdapter;
    private View emptyView;
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager layoutManager;
    private BroadcastReceiver downloadChangeReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.downloaded_fragment, container, false);

        getUI(rootView);
        setupUI();
        createDownloadChangeReceiver();
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

    private void getUI(ViewGroup rootView) {
        emptyView = rootView.findViewById(R.id.none_downloaded);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.main_downloads_list);
    }

    private void createDownloadChangeReceiver() {
        unregisterDownloadChangeReceiver();

        if (downloadChangeReceiver == null) {
            downloadChangeReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    queryForDownloads();
                }
            };
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadedFragment.DOWNLOAD_MANAGER_ONCHANGE);
        filter.addAction(Video.NOTIFY_AUDIO_DELETE);

        getActivity().registerReceiver(downloadChangeReceiver, filter);
    }

    private void unregisterDownloadChangeReceiver() {
        if (downloadChangeReceiver != null) {
            try {
                getActivity().unregisterReceiver(downloadChangeReceiver);
                downloadChangeReceiver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void setup() {
    }

    private void setupUI() {
        layoutManager = new StaggeredGridLayoutManager(calculateColumns(), 1);
        recyclerView.setLayoutManager(layoutManager);
        downloadsAdapter = new DownloadsAdapter(getActivity(), new ArrayList<Download>());
        recyclerView.setAdapter(downloadsAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
    }

    private void queryForDownloads() {
        List<Download> downloads = Download.listAll(Download.class);
        onQueryResult(downloads);
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

    public void onQueryResult(List<Download> downloads) {
        downloadsAdapter.updateDownloads(downloads);
        emptyView.setVisibility(downloads.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
