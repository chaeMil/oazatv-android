package com.chaemil.hgms.ui.tv.fragment;

import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;

import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.api.Api;
import com.chaemil.hgms.service.api.JsonFutureCallback;
import com.chaemil.hgms.ui.tv.model.MovieRow;
import com.chaemil.hgms.ui.tv.presenter.MoviePresenter;
import com.chaemil.hgms.ui.tv.view.BackgroundManager;
import com.chaemil.hgms.utils.SmartLog;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TvMainFragment extends BrowseFragment implements OnItemViewSelectedListener {
    private static final String TAG = TvMainFragment.class.getSimpleName();

    private BackgroundManager mBackgroundManager;

    private static final int NOW_PLAYING = 0;
    private static final int TOP_RATED = 1;
    private static final int POPULAR = 2;
    private static final int UPCOMING = 3;

    SparseArray<MovieRow> mRows;

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

        mBackgroundManager = new BackgroundManager(getActivity());

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
                bindMovieResponse(videos, NOW_PLAYING);
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
            MovieRow row = mRows.get(i);
            // Adds a new ListRow to the adapter. Each row will contain a collection of Movies
            // That will be rendered using the MoviePresenter
            HeaderItem headerItem = new HeaderItem(row.getId(), row.getTitle());
            ListRow listRow = new ListRow(headerItem, row.getAdapter());
            rowsAdapter.add(listRow);
        }
        // Sets this fragments Adapter.
        // The setAdapter method is defined in the BrowseFragment of the Leanback Library
        setAdapter(rowsAdapter);

        setOnItemViewSelectedListener(this);
    }

    private void bindMovieResponse(ArrayList<Video> videos, int id) {
        MovieRow row = mRows.get(id);
        row.setPage(row.getPage() + 1);
        for (Video video : videos) {
            if (video.getThumbFile() != null) { // Avoid showing movie without posters
                row.getAdapter().add(video);
            }
        }
    }

    private void createDataRows() {
        mRows = new SparseArray<>();
        MoviePresenter moviePresenter = new MoviePresenter();
        mRows.put(NOW_PLAYING, new MovieRow()
                .setId(NOW_PLAYING)
                .setAdapter(new ArrayObjectAdapter(moviePresenter))
                .setTitle("Now Playing")
                .setPage(1)
        );
        mRows.put(TOP_RATED, new MovieRow()
                .setId(TOP_RATED)
                .setAdapter(new ArrayObjectAdapter(moviePresenter))
                .setTitle("Top Rated")
                .setPage(1)
        );
        mRows.put(POPULAR, new MovieRow()
                .setId(POPULAR)
                .setAdapter(new ArrayObjectAdapter(moviePresenter))
                .setTitle("Popular")
                .setPage(1)
        );
        mRows.put(UPCOMING, new MovieRow()
                .setId(UPCOMING)
                .setAdapter(new ArrayObjectAdapter(moviePresenter))
                .setTitle("Upcoming")
                .setPage(1)
        );
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        // Check if the item is a movie
        if (item instanceof Movie) {
            Video video = (Video) item;
            // Check if the movie has a backdrop
            if (video.getThumbFile() != null) {
                mBackgroundManager.loadImage(video.getThumbFile());
            } else {
                mBackgroundManager.setBackground(new ColorDrawable(getResources().getColor(R.color.black)));
            }
        }
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
        setSearchAffordanceColor(getResources().getColor(R.color.colorAccent));
    }
}