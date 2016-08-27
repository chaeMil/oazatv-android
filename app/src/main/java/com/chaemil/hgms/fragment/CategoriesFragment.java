package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.CategoriesAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Category;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.GAUtils;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chaemil on 19.4.16.
 */
public class CategoriesFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<Category> categories = new ArrayList<>();
    private RecyclerView categoriesList;
    private CategoriesAdapter categoriesAdapter;
    private ProgressBar progress;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout connectionErrorWrapper;
    private GridLayoutManager gridLayoutManager;
    private CategoryFragment categoryFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.categories_fragment, container, false);

        getData();
        getUI(rootView);
        setupUI();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        GAUtils.sendGAScreen(
                ((OazaApp) getActivity().getApplication()),
                "Categories");
    }

    private void getData() {
        JsonObjectRequest request = RequestFactory.getCategories(this, false, 0, 0, 0);
        RequestService.getRequestQueue().add(request);
    }

    private void getUI(ViewGroup rootView) {
        categoriesList = (RecyclerView) rootView.findViewById(R.id.categories_list);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        connectionErrorWrapper = (LinearLayout) rootView.findViewById(R.id.connection_error_wrapper);
    }

    public void setupUI() {
        if (isAdded()) {
            categoriesAdapter = new CategoriesAdapter(getContext(),
                    this,
                    categories,
                    ((MainActivity) getActivity()));

            setupGridManager();
            categoriesList.setAdapter(categoriesAdapter);
            swipeRefresh.setOnRefreshListener(this);
        }
    }

    private void setupGridManager() {
        final int columns = getResources().getInteger(R.integer.archive_columns);
        if (gridLayoutManager == null) {
            gridLayoutManager = new GridLayoutManager(getActivity(), columns);
            categoriesList.setLayoutManager(gridLayoutManager);
        } else {
            gridLayoutManager.setSpanCount(columns);
        }
    }

    public void adjustLayout() {
        if (isAdded()) {
            setupGridManager();
            if (categoryFragment != null) {
                categoryFragment.adjustLayout();
            }
        }
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_CATEGORIES:
                if (response != null) {
                    categories = ResponseFactory.parseCategories(response);
                    progress.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    connectionErrorWrapper.setVisibility(View.GONE);
                }

                setupUI();

                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        super.onErrorResponse(exception, requestType);

        swipeRefresh.setRefreshing(false);
        connectionErrorWrapper.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        getData();
    }

    public void setCategoryFragment(CategoryFragment categoryFragment) {
        this.categoryFragment = categoryFragment;
    }
}
