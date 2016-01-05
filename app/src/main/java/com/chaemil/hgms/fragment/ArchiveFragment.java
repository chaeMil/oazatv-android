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

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.ArchiveAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.DimensUtils;
import com.chaemil.hgms.utils.EndlessScrollListener;

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
    private int actionBarHeight;
    private LinearLayout appBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getData(savedInstanceState);

        TypedValue tv = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.archive_fragment, container, false);


        getUI(rootView);
        setupUI();

        return rootView;

    }

    private void getData(Bundle savedInstanceState) {
        if (archive.size() == 0 || savedInstanceState == null) {
            JsonObjectRequest getArchive = RequestFactory.getArchive(this, 0);
            MyRequestService.getRequestQueue().add(getArchive);
        }
    }

    private void getArchivePage(int pageNumber) {
        JsonObjectRequest getArchivePage = RequestFactory.getArchive(this, pageNumber);
        MyRequestService.getRequestQueue().add(getArchivePage);
    }

    private void setupUI() {
        archiveAdapter = new ArchiveAdapter(getActivity(),
                R.layout.archive_item,
                ((MainActivity) getActivity()).getPlayerFragment(),
                archive);

        archiveGridView.setAdapter(archiveAdapter);
        archiveGridView.setOnScrollListener(endlessScrollListener(archiveGridView));

        setupToolbarHiding();

        adjustLayout();
    }

    private EndlessScrollListener endlessScrollListener(GridView gridView) {
        return new EndlessScrollListener(gridView, new EndlessScrollListener.RefreshList() {
            @Override
            public void onRefresh(int pageNumber) {
                getArchivePage(pageNumber);
            }
        });
    }

    public void adjustLayout() {

        final int columns = getResources().getInteger(R.integer.archive_columns);
        archiveGridView.setNumColumns(columns);

    }

    private void getUI(ViewGroup rootView) {
        archiveGridView = (GridView) rootView.findViewById(R.id.archive_grid_view);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        appBar = ((MainActivity) getActivity()).getMainFragment().getAppBar();
    }

    public void setupToolbarHiding() {

        /*archiveGridView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                int distance = oldScrollY - scrollY;
ic void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                int distance = oldScrollY - scrollY;

                appBar.setTranslationY(distance);
            }
        });
    }
                appBar.setTranslationY(distance);
            }
        });*/
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

                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError exception) {
        super.onErrorResponse(exception);
    }
}
