package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
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
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.ArchiveAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.EndlessRecyclerOnScrollListener;
import com.chaemil.hgms.utils.HidingScrollListener;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by chaemil on 2.12.15.
 */
public class ArchiveFragment extends BaseFragment {

    private ArrayList<ArchiveItem> archive = new ArrayList<>();
    private RecyclerView archiveRecyclerView;
    private ProgressBar progress;
    private ArchiveAdapter archiveAdapter;
    private int actionBarHeight;
    private LinearLayoutManager gridLayoutManager;
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

    private void setupUI() {
        archiveAdapter = new ArchiveAdapter(
                getContext(),
                ((MainActivity) getActivity()).getPlayerFragment(),
                archive);

        gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        archiveRecyclerView.setLayoutManager(gridLayoutManager);
        archiveRecyclerView.setHasFixedSize(true);
        archiveRecyclerView.setAdapter(archiveAdapter);
        archiveRecyclerView.addOnScrollListener(onScrollListener(gridLayoutManager));

        setupToolbarHiding();

        adjustLayout();
    }

    public void adjustLayout() {

        final int columns = getResources().getInteger(R.integer.archive_columns);
        gridLayoutManager = new GridLayoutManager(getActivity(), columns);
        archiveRecyclerView.setLayoutManager(gridLayoutManager);

    }

    private EndlessRecyclerOnScrollListener onScrollListener(LinearLayoutManager linearLayoutManager) {
        return new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(final int currentPage) {
                progress.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        JsonObjectRequest loadMoreArchive = RequestFactory.getArchive(ArchiveFragment.this, currentPage);
                        MyRequestService.getRequestQueue().add(loadMoreArchive);
                    }
                }, 2000);
            }
        };
    }

    private void getUI(ViewGroup rootView) {
        archiveRecyclerView = (RecyclerView) rootView.findViewById(R.id.archive_recycler_view);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        appBar = ((MainActivity) getActivity()).getMainFragment().getAppBar();
    }

    public void setupToolbarHiding() {

        archiveRecyclerView.setOnScrollListener(new HidingScrollListener(actionBarHeight) {
            @Override
            public void onHide() {

            }

            @Override
            public void onShow() {

            }

            @Override
            public void onMoved(int distance) {
                appBar.setTranslationY(-distance);
            }
        });
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
