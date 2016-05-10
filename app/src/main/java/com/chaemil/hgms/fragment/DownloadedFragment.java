package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.DownloadedAdapter;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.utils.DimensUtils;
import com.chaemil.hgms.utils.FileUtils;
import com.chaemil.hgms.utils.GAUtils;
import com.chaemil.hgms.utils.SmartLog;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chaemil on 2.12.15.
 */
public class DownloadedFragment extends BaseFragment {

    private GridView downloadedGridView;
    private DownloadedAdapter downloadedAdapter;
    private ArrayList<Video> downloadedItems;
    private LinearLayout spaceWrapper;
    private View oazaSpace;
    private TextView spaceText;
    private RelativeLayout noneDownloaded;
    private TimerTask timerTask;
    private Timer timer;

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
        setupTimer();

        GAUtils.sendGAScreen(
                ((OazaApp) getActivity().getApplication()),
                "Downloaded");
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }

    private void setupTimer() {
        stopTimer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDatasetChanged();
                    }
                });
            }
        };
        resetTimer();
    }

    private void resetTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 5000, 5000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.post(new Runnable() {
            @Override
            public void run() {
                setupSpaceGraph();
            }
        });
    }

    private void getData() {
        downloadedItems.clear();
        downloadedItems.addAll(Video.getWholeDownloadQueue());
    }

    private void getUI(ViewGroup rootView) {
        downloadedGridView = (GridView) rootView.findViewById(R.id.downloaded_grid_view);
        spaceWrapper = (LinearLayout) rootView.findViewById(R.id.space_wrapper);
        oazaSpace = rootView.findViewById(R.id.oaza_space);
        spaceText = (TextView) rootView.findViewById(R.id.space_text);
        noneDownloaded = (RelativeLayout) rootView.findViewById(R.id.none_downloaded);
    }

    private void setupUI() {
        downloadedAdapter = new DownloadedAdapter(getActivity(),
                ((MainActivity) getActivity()),
                downloadedItems);

        downloadedGridView.setAdapter(downloadedAdapter);

        adjustLayout();
        setupSpaceGraph();
    }

    private void setupSpaceGraph() {
        int totalSpaceInt = (int) getTotalSpace() / 1024;
        int freeSpaceInt = (int) getFreeSpace() / 1024;
        int oazaSpaceInt = (int) getOazaSpace() / 1024;

        int graphWidth = DimensUtils.getDisplayWidth(getActivity());
        double oazaSpacePercent = (double) oazaSpaceInt / (double) totalSpaceInt * 100.0;
        double oazaSpaceWidth = graphWidth / 100.0 * oazaSpacePercent;
        oazaSpace.setLayoutParams(new LinearLayout.LayoutParams((int) oazaSpaceWidth,
                ViewGroup.LayoutParams.MATCH_PARENT));

        spaceText.setText(getString(R.string.app_name) + ": " + FileUtils.readableFileSize((long) oazaSpaceInt * 1024) + " | " +
                        getString(R.string.space_free) + ": " + FileUtils.readableFileSize((long) freeSpaceInt * 1024));
    }

    private float getFreeSpace() {
        return FileUtils.getFreeSpace();
    }

    private float getTotalSpace() {
        return FileUtils.getExternalStorageSize();
    }

    private float getOazaSpace() {
        return FileUtils.getFolderSize(getContext().getExternalFilesDir(null));
    }

    public void adjustLayout() {

        if (isAdded()) {
            final int columns = getResources().getInteger(R.integer.archive_columns);
            downloadedGridView.setNumColumns(columns);
        }

    }

    public void notifyDownloadFinished() {
        notifyDatasetChanged();
    }

    public void notifyDatasetChanged() {
        getData();
        downloadedAdapter.notifyDataSetChanged();
        downloadedAdapter.notifyDataSetInvalidated();
        setupSpaceGraph();

        if (downloadedItems.size() <= 0) {
            noneDownloaded.setVisibility(View.VISIBLE);
        } else {
            noneDownloaded.setVisibility(View.GONE);
        }
    }
}
