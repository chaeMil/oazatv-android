package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.CategoriesAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Category;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.MyRequestService;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chaemil on 19.4.16.
 */
public class CategoriesFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<Category> categories = new ArrayList<>();
    private ExpandableListView categoriesList;
    private CategoriesAdapter categoriesAdapter;
    private ProgressBar progress;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout connectionErrorWrapper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.categories_fragment, container, false);

        getData();
        getUI(rootView);
        setupUI();

        return rootView;
    }

    private void getData() {
        JsonObjectRequest request = RequestFactory.getCategories(this);
        MyRequestService.getRequestQueue().add(request);
    }

    private void getUI(ViewGroup rootView) {
        categoriesList = (ExpandableListView) rootView.findViewById(R.id.categories_list);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        connectionErrorWrapper = (LinearLayout) rootView.findViewById(R.id.connection_error_wrapper);
    }

    public void setupUI() {
        if (isAdded()) {
            categoriesAdapter = new CategoriesAdapter(getContext(), categories,
                    ((MainActivity) getActivity()));

            categoriesList.setAdapter(categoriesAdapter);
            categoriesList.setDividerHeight(0);
            swipeRefresh.setOnRefreshListener(this);
        }
    }

    public void adjustLayout() {
        if (isAdded()) {
            categoriesList.setAdapter(categoriesAdapter);
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
}
