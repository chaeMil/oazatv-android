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

import java.util.ArrayList;
import java.util.List;

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

        getUI(rootView);
        getData();
        setupUI();

        return rootView;
    }

    private void getData() {
        downloadedItems = Video.getAllDownloadedVideos();
    }

    private void getUI(ViewGroup rootView) {
        downloadedGridView = (GridView) rootView.findViewById(R.id.downloaded_grid_view);
    }

    private void setupUI() {
        downloadedAdapter = new DownloadedAdapter(getActivity(),
                R.id.download_audio,
                ((MainActivity) getActivity()),
                downloadedItems);

        downloadedGridView.setAdapter(downloadedAdapter);
    }
}
