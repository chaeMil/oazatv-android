package com.chaemil.hgms.ui.mobile.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.ui.mobile.activity.MainActivity;
import com.chaemil.hgms.ui.mobile.adapter.ArchiveAdapter;
import com.chaemil.hgms.ui.mobile.adapter.utils.HidingScrollListener;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Category;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.EndlessScrollListener;
import com.chaemil.hgms.utils.SmartLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by chaemil on 2.12.15.
 */
public class CategoryFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener {

    public static final String CATEGORY = "category";
    public static final String TAG = "CategoryFragment";
    private static final int PER_PAGE = 10;
    private ArrayList<Video> archive = new ArrayList<>();
    private RecyclerView archiveGridView;
    private ProgressBar progress;
    private ArchiveAdapter archiveAdapter;
    private ProgressBar endlessProgress;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout connectionErrorWrapper;
    private GridLayoutManager gridLayoutManager;
    private Category category;
    private RelativeLayout categoryToolbar;
    private TextView categoryName;
    private ImageView back;
    private MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mainActivity = (MainActivity) getActivity();
        mainActivity.setCategoryFragment(this);

        Bundle bundle = getArguments();
        category = bundle.getParcelable(CATEGORY);
        if (category == null) {
            goBack();
        }

        TypedValue tv = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
    }

    public void exit() {
        mainActivity.setCategoryFragment(null);
        mainActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.category_fragment, container, false);

        getUI(rootView);
        setupUI();

        getCategoryPage(0);

        return rootView;

    }

    private void getCategoryPage(final int pageNumber) {
        if (pageNumber == 0) {
            endlessProgress.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        } else {
            progress.setVisibility(View.GONE);
            endlessProgress.setVisibility(View.VISIBLE);
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SmartLog.d("getCategoryPage", String.valueOf(pageNumber));
                JsonObjectRequest getCategoryPage = RequestFactory.getCategories(CategoryFragment.this,
                        true, category.getId(), pageNumber, PER_PAGE);
                RequestService.getRequestQueue().add(getCategoryPage);
            }
        }, 750);
    }

    private void setupUI() {
        setupAdapter();
        adjustLayout();

        categoryToolbar.setBackgroundColor(Color.parseColor(category.getColor()));
        categoryName.setText(category.getName());
        back.setOnClickListener(this);
    }

    private void setupAdapter() {
        if (archiveAdapter == null) {
            archiveAdapter = new ArchiveAdapter(getActivity(),
                    archive,
                    AdapterUtils.getArchiveLayout(getActivity()));

            archiveGridView.setOnScrollListener(new HidingScrollListener() {
                @Override
                public void onHide() {
                    categoryToolbar.animate()
                            .translationY(-categoryToolbar.getHeight())
                            .setInterpolator(new AccelerateInterpolator(2));
                }

                @Override
                public void onShow() {
                    categoryToolbar.animate()
                            .translationY(0)
                            .setInterpolator(new DecelerateInterpolator(2));
                }
            });

            archiveGridView.setAdapter(archiveAdapter);
            setupGridManager();
            archiveGridView.addOnScrollListener(new EndlessScrollListener(gridLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    getCategoryPage(page + 1);
                }
            });
            swipeRefresh.setOnRefreshListener(this);
        }

        archiveAdapter.notifyDataSetChanged();
    }

    private void setupGridManager() {
        final int columns = getResources().getInteger(R.integer.archive_columns);
        if (gridLayoutManager == null) {
            gridLayoutManager = new GridLayoutManager(getActivity(), columns);
            archiveGridView.setLayoutManager(gridLayoutManager);
        } else {
            gridLayoutManager.setSpanCount(columns);
        }
    }

    public void adjustLayout() {
        if (isAdded()) {
            setupGridManager();
        }
    }

    private void getUI(ViewGroup rootView) {
        archiveGridView = (RecyclerView) rootView.findViewById(R.id.archive_grid_view);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        endlessProgress = (ProgressBar) rootView.findViewById(R.id.endless_progress);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        connectionErrorWrapper = (LinearLayout) rootView.findViewById(R.id.connection_error_wrapper);
        categoryName = (TextView) rootView.findViewById(R.id.category_name);
        back = (ImageView) rootView.findViewById(R.id.back);
        categoryToolbar = (RelativeLayout) rootView.findViewById(R.id.category_toolbar);
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_CATEGORIES:

                JSONArray videosJsonArray = null;
                try {
                    videosJsonArray = response
                            .getJSONObject(Constants.JSON_CATEGORIES)
                            .getJSONArray(Constants.JSON_VIDEOS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (videosJsonArray != null) {
                    for (int i = 0; i < videosJsonArray.length(); i++) {
                        try {
                            Video video = ResponseFactory.parseVideo(videosJsonArray.getJSONObject(i));
                            if (video != null) {
                                archive.add(video);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (category.getVideos() != null) {
                    archive.addAll(category.getVideos());
                }

                setupAdapter();

                endlessProgress.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                connectionErrorWrapper.setVisibility(View.GONE);

                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        super.onErrorResponse(exception, requestType);
        swipeRefresh.setRefreshing(false);
        connectionErrorWrapper.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        endlessProgress.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        archive.clear();
        archiveAdapter.notifyDataSetChanged();
        swipeRefresh.setRefreshing(false);
        getCategoryPage(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                goBack();
                break;
        }
    }

    public void notifyAudioDeleted() {
        if (archiveAdapter != null) {
            archiveAdapter.notifyDataSetChanged();
        }
    }
}
