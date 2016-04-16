package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.DownloadedAdapter;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.utils.SmartLog;

import java.util.ArrayList;

/**
 * Created by chaemil on 2.12.15.
 */
public class DownloadedFragment extends BaseFragment {

    private GridView downloadedGridView;
    private DownloadedAdapter downloadedAdapter;
    private ArrayList<Video> downloadedItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.downloaded_fragment, container, false);

        downloadedItems = new ArrayList<>();

        getUI(rootView);
        getData();
        setupUI();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.DOWNLOADED_FRAGMENT);
    }

    private void getData() {
        downloadedItems.clear();
        downloadedItems.addAll(Video.getWholeDownloadQueue());
    }

    private void getUI(ViewGroup rootView) {
        downloadedGridView = (GridView) rootView.findViewById(R.id.downloaded_grid_view);
    }

    private void setupUI() {
        downloadedAdapter = new DownloadedAdapter(getActivity(),
                ((MainActivity) getActivity()),
                downloadedItems);

        downloadedGridView.setAdapter(downloadedAdapter);

        adjustLayout();
    }

    public void adjustLayout() {

        if (isAdded()) {
            final int columns = getResources().getInteger(R.integer.archive_columns);
            downloadedGridView.setNumColumns(columns);
        }

    }

    public void notifyDownloadFinished() {
        getData();
        downloadedAdapter.notifyDataSetChanged();
    }

    public void notifyDatasetChanged() {
        getData();
        downloadedAdapter.notifyDataSetChanged();
    }
}
