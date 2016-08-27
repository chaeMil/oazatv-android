package com.chaemil.hgms.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.chaemil.hgms.utils.SmartLog;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chaemil on 19.4.16.
 */
public class CategoriesFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<Category> categories = new ArrayList<>();
    private TagView categoriesList;
    private CategoriesAdapter categoriesAdapter;
    private ProgressBar progress;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout connectionErrorWrapper;
    private GridLayoutManager gridLayoutManager;
    private ArrayList<Tag> categoryTags = new ArrayList<>();

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
        categoriesList = (TagView) rootView.findViewById(R.id.categories_list);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        connectionErrorWrapper = (LinearLayout) rootView.findViewById(R.id.connection_error_wrapper);
    }

    public void setupUI() {
        if (isAdded()) {
            swipeRefresh.setOnRefreshListener(this);

            categoriesList.settextPaddingBottom(10f);
            categoriesList.setTextPaddingLeft(10f);
            categoriesList.setTextPaddingRight(10f);
            categoriesList.setTextPaddingTop(10f);
            categoriesList.addTags(categoryTags);
            categoriesList.setOnTagClickListener(new TagView.OnTagClickListener() {
                @Override
                public void onTagClick(Tag tag, int i) {
                    SmartLog.Log(SmartLog.LogLevel.DEBUG, "category", categories.get(i).getName());
                }
            });

        }
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_CATEGORIES:
                if (response != null) {
                    categories = ResponseFactory.parseCategories(response);
                    if (categories != null) {
                        categoryTags.clear();

                        for (Category category : categories) {
                            Tag categoryTag = new Tag(category.getName());
                            categoryTag.tagTextColor = getResources().getColor(R.color.white);
                            categoryTag.layoutColor = Color.parseColor(category.getColor());
                            categoryTag.tagTextSize = 18f;
                            categoryTag.radius = 4f;
                            categoryTags.add(categoryTag);
                        }
                    }
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
