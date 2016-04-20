package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;

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
public class CategoriesFragment extends BaseFragment {

    private ArrayList<Category> categories = new ArrayList<>();
    private ExpandableListView categoriesList;
    private CategoriesAdapter categoriesAdapter;
    private ProgressBar progress;

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
    }

    public void setupUI() {
        categoriesAdapter = new CategoriesAdapter(getContext(), categories,
                ((MainActivity) getActivity()));
        categoriesList.setAdapter(categoriesAdapter);
        categoriesList.setDividerHeight(0);
        categoriesList.setGroupIndicator(getResources()
                .getDrawable(R.drawable.categories_list_indicator));
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_CATEGORIES:
                if (response != null) {
                    categories = ResponseFactory.parseCategories(response);
                    progress.setVisibility(View.GONE);
                }

                setupUI();
                break;
        }
    }
}
