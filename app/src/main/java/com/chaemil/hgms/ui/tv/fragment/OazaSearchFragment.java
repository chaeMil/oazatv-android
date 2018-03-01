package com.chaemil.hgms.ui.tv.fragment;

import android.os.Bundle;
import android.support.v17.leanback.app.SearchFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.text.TextUtils;

import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.api.Api;
import com.chaemil.hgms.service.api.JsonFutureCallback;
import com.chaemil.hgms.ui.tv.presenter.VideoPresenter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by Michal Mlejnek on 01/03/2018.
 */

public class OazaSearchFragment extends SearchFragment implements SearchFragment.SearchResultProvider {
    private static final int SEARCH_DELAY_MS = 300;
    private ArrayObjectAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ArrayObjectAdapter(new VideoPresenter());
        setSearchResultProvider(this);
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return adapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        adapter.clear();
        if (!TextUtils.isEmpty(newQuery)) {

        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.clear();
        if (!TextUtils.isEmpty(query)) {
            Api.getSearch(getActivity(), query, new JsonFutureCallback() {
                @Override
                public void onSuccess(int statusCode, JsonObject response) {
                    if (response != null && response.has("search")) {
                        ArrayList<Video> videos = parseVideosResponse(response);
                        for (Video video : videos) {
                            adapter.add(video);
                        }
                    }
                }

                @Override
                public void onError(int statusCode, Exception e, JsonObject response) {

                }
            });
        }
        return true;
    }

    private ArrayList<Video> parseVideosResponse(JsonObject response) {
        JsonArray jsonVideosArray = response.get("search").getAsJsonArray();
        ArrayList<Video> videos = new ArrayList<>();
        if (jsonVideosArray != null) {
            for (int i = 0; i < jsonVideosArray.size(); i++) {
                JsonObject jsonObject = jsonVideosArray.get(i).getAsJsonObject();
                if (jsonObject.get("type").getAsString().equals("video")) {
                    Video video = ResponseFactory.parseVideo(jsonObject);
                    videos.add(video);
                }
            }
        }
        return videos;
    }
}
