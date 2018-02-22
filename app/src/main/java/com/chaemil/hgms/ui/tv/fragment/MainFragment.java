package com.chaemil.hgms.ui.tv.fragment;

import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;
import android.util.SparseArray;

import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.api.Api;
import com.chaemil.hgms.service.api.JsonFutureCallback;
import com.chaemil.hgms.ui.tv.activity.MainActivity;
import com.chaemil.hgms.ui.tv.model.VideoRow;
import com.chaemil.hgms.ui.tv.presenter.VideoPresenter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class MainFragment extends BrowseFragment implements OnItemViewClickedListener {
    private static final String TAG = MainFragment.class.getSimpleName();

    private static final int LATEST = 0;
    private static final int FEATURED = 1;
    private static final int POPULAR = 2;

    SparseArray<VideoRow> rows;

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        setupUIElements();
        createDataRows();
        createRows();
        prepareEntranceTransition();
        loadHomepage();
    }

    private void loadHomepage() {
        Api.getHomePage(getActivity(), new JsonFutureCallback() {
            @Override
            public void onSuccess(int statusCode, JsonObject response) {
                if (response != null) {
                    if (response.has("newestVideos")) {
                        ArrayList<Video> newestVideos =
                                parseVideosResponse(response.get("newestVideos").getAsJsonArray());
                        bindMovieResponse(newestVideos, LATEST);
                    }

                    if (response.has("popularVideos")) {
                        ArrayList<Video> popularVideos =
                                parseVideosResponse(response.get("popularVideos").getAsJsonArray());
                        bindMovieResponse(popularVideos, POPULAR);
                    }

                    if (response.has("featured")) {
                        ArrayList<Video> featured =
                                parseVideosResponse(response.get("featured").getAsJsonArray());
                        bindMovieResponse(featured, FEATURED);
                    }
                }

                startEntranceTransition();
            }

            @Override
            public void onError(int statusCode, Exception e, JsonObject response) {

            }
        });
    }

    private ArrayList<Video> parseVideosResponse(JsonArray jsonVideosArray) {
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

    private void createRows() {
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        for (int i = 0; i < rows.size(); i++) {
            VideoRow row = rows.get(i);
            HeaderItem headerItem = new HeaderItem(row.getId(), row.getTitle());
            ListRow listRow = new ListRow(headerItem, row.getAdapter());
            rowsAdapter.add(listRow);
        }
        setAdapter(rowsAdapter);
        setOnItemViewClickedListener(this);
    }

    private void bindMovieResponse(ArrayList<Video> videos, int id) {
        VideoRow row = rows.get(id);
        row.setPage(row.getPage() + 1);
        for (Video video : videos) {
            if (video.getThumbFile() != null) {
                row.getAdapter().add(video);
            }
        }
    }

    private void createDataRows() {
        rows = new SparseArray<>();
        VideoPresenter moviePresenter = new VideoPresenter();
        rows.put(LATEST, new VideoRow()
                .setId(LATEST)
                .setAdapter(new ArrayObjectAdapter(moviePresenter))
                .setTitle(getString(R.string.latest))
                .setPage(1)
        );

        rows.put(FEATURED, new VideoRow()
                .setId(FEATURED)
                .setAdapter(new ArrayObjectAdapter(moviePresenter))
                .setTitle(getString(R.string.featured))
                .setPage(1)
        );

        rows.put(POPULAR, new VideoRow()
                .setId(POPULAR)
                .setAdapter(new ArrayObjectAdapter(moviePresenter))
                .setTitle(getString(R.string.popular))
                .setPage(1)
        );
    }

    private void setupUIElements() {
        setTitle(getString(R.string.app_name));
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(getResources().getColor(R.color.colorPrimary));
        setSearchAffordanceColor(getResources().getColor(R.color.white));
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item != null) {
            Video video = (Video) item;

            Bundle arguments = new Bundle();
            arguments.putParcelable("video", video);

            VideoPlaybackFragment videoPlaybackFragment = VideoPlaybackFragment.newInstance();
            videoPlaybackFragment.setArguments(arguments);

            ((MainActivity) getActivity()).addFragment(videoPlaybackFragment);
        }
    }
}