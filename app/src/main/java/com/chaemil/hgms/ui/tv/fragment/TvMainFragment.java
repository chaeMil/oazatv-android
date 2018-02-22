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
import com.chaemil.hgms.ui.tv.activity.TvMainActivity;
import com.chaemil.hgms.ui.tv.model.VideoRow;
import com.chaemil.hgms.ui.tv.presenter.VideoPresenter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class TvMainFragment extends BrowseFragment implements OnItemViewClickedListener {
    private static final String TAG = TvMainFragment.class.getSimpleName();

    private static final int ARCHIVE = 0;

    SparseArray<VideoRow> mRows;

    public static TvMainFragment newInstance() {
        Bundle args = new Bundle();
        TvMainFragment fragment = new TvMainFragment();
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
        fetchPopularMovies();
    }

    private void fetchPopularMovies() {
        Api.getVideosFromArchive(getActivity(), 1, new JsonFutureCallback() {
            @Override
            public void onSuccess(int statusCode, JsonObject response) {
                ArrayList<Video> videos = parseVideosResponse(response);
                bindMovieResponse(videos, ARCHIVE);
                startEntranceTransition();
            }

            @Override
            public void onError(int statusCode, Exception e, JsonObject response) {

            }
        });
    }

    private ArrayList<Video> parseVideosResponse(JsonObject response) {
        ArrayList<Video> videos = new ArrayList<>();
        if (response != null) {
            JsonArray jsonVideosArray = response.get("archive").getAsJsonArray();
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
        // Creates the RowsAdapter for the Fragment
        // The ListRowPresenter tells to render ListRow objects
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        for (int i = 0; i < mRows.size(); i++) {
            VideoRow row = mRows.get(i);
            // Adds a new ListRow to the adapter. Each row will contain a collection of Movies
            // That will be rendered using the VideoPresenter
            HeaderItem headerItem = new HeaderItem(row.getId(), row.getTitle());
            ListRow listRow = new ListRow(headerItem, row.getAdapter());
            rowsAdapter.add(listRow);
        }
        // Sets this fragments Adapter.
        // The setAdapter method is defined in the BrowseFragment of the Leanback Library
        setAdapter(rowsAdapter);
        setOnItemViewClickedListener(this);
    }

    private void bindMovieResponse(ArrayList<Video> videos, int id) {
        VideoRow row = mRows.get(id);
        row.setPage(row.getPage() + 1);
        for (Video video : videos) {
            if (video.getThumbFile() != null) { // Avoid showing movie without posters
                row.getAdapter().add(video);
            }
        }
    }

    private void createDataRows() {
        mRows = new SparseArray<>();
        VideoPresenter moviePresenter = new VideoPresenter();
        mRows.put(ARCHIVE, new VideoRow()
                .setId(ARCHIVE)
                .setAdapter(new ArrayObjectAdapter(moviePresenter))
                .setTitle(getString(R.string.archive))
                .setPage(1)
        );
    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(R.drawable.videos_by_google_banner));
        setTitle(getString(R.string.app_name)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.colorPrimary));
        // set search icon color
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

            ((TvMainActivity) getActivity()).addFragment(videoPlaybackFragment);
        }
    }
}