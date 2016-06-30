package com.chaemil.hgms.fragment;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.adapter.PauseResumeAdapter;
import com.chaemil.hgms.model.Download;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.utils.GAUtils;
import com.chaemil.hgms.utils.QueryForDownloadsAsyncTask;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.notifications.NotificationVisibility;
import com.novoda.downloadmanager.lib.Query;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.downloadmanager.lib.logger.LLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaemil on 2.12.15.
 */
public class DownloadedFragment extends BaseFragment implements QueryForDownloadsAsyncTask.Callback {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private DownloadManager downloadManager;
    private PauseResumeAdapter pauseResumeAdapter;
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
        setupQueryingExample();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.DOWNLOADED_FRAGMENT);

        GAUtils.sendGAScreen(
                (OazaApp) getActivity().getApplication(),
                "Downloaded");
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        PauseResumeAdapter.Listener clickListener = new PauseResumeAdapter.Listener() {
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
        pauseResumeAdapter = new PauseResumeAdapter(new ArrayList<Download>(), clickListener);
        recyclerView.setAdapter(pauseResumeAdapter);
    }

    private void setupQueryingExample() {
        queryForDownloads();
    }

    private void queryForDownloads() {
        Query orderedQuery = new Query().orderByLiveness();
        QueryForDownloadsAsyncTask.newInstance(downloadManager, this).execute(orderedQuery);
    }

    public void adjustLayout() {

    }

    public void notifyDownloadFinished() {

    }

    public void notifyDatasetChanged() {

    }

    @Override
    public void onQueryResult(List<Download> downloads) {
        pauseResumeAdapter.updateDownloads(downloads);
        emptyView.setVisibility(downloads.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
