package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.ArchiveAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.DimensUtils;
import com.chaemil.hgms.utils.EndlessScrollListener;
import com.chaemil.hgms.utils.SmartLog;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by chaemil on 2.12.15.
 */
public class ArchiveFragment extends BaseFragment {

    private ArrayList<ArchiveItem> archive = new ArrayList<>();
    private GridView archiveGridView;
    private ProgressBar progress;
    private ArchiveAdapter archiveAdapter;
    private ProgressBar endlessProgress;

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
        MyRequestService.getRequestQueue().add(getArchivePage);
    }

    private void setupUI() {
        archiveAdapter = new ArchiveAdapter(getActivity(),
                (MainActivity) getActivity(),
                archive);

        archiveGridView.setAdapter(archiveAdapter);
        archiveGridView.setOnScrollListener(endlessScrollListener());

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
            final int columns = getResources().getInteger(R.integer.archive_columns);
            archiveGridView.setNumColumns(columns);
        }

    }

    private void getUI(ViewGroup rootView) {
        archiveGridView = (GridView) rootView.findViewById(R.id.archive_grid_view);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        endlessProgress = (ProgressBar) rootView.findViewById(R.id.endless_progress);
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
                }

                progress.setVisibility(View.GONE);
                endlessProgress.setVisibility(View.GONE);

                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError exception) {
        super.onErrorResponse(exception);
    }
}
