package com.chaemil.hgms.ui.tv.fragment;

import android.os.Bundle;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;

import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.api.Api;
import com.chaemil.hgms.service.api.JsonFutureCallback;
import com.chaemil.hgms.ui.tv.activity.MainActivity;
import com.chaemil.hgms.ui.tv.presenter.VideoPresenter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by Michal Mlejnek on 22/02/2018.
 */

public class ArchiveFragment extends VerticalGridFragment implements OnItemViewClickedListener,
        OnItemViewSelectedListener {
    private static final String TAG = ArchiveFragment.class.getSimpleName();
    private static final int NUM_COLUMNS = 3;

    private Bundle arguments;
    private int page = 0;
    private ArrayObjectAdapter adapter;

    public static ArchiveFragment newInstance() {
        Bundle args = new Bundle();
        ArchiveFragment fragment = new ArchiveFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arguments = getArguments();
        setTitle(getString(R.string.archive));
        setupArchiveView();
        loadArchiveVideos();
    }

    private void loadArchiveVideos() {
        //TODO add pagination
        Api.getVideosFromArchive(getActivity(), page, new JsonFutureCallback() {
            @Override
            public void onSuccess(int statusCode, JsonObject response) {
                if (response != null && response.has("archive")) {
                    ArrayList<Video> videos = parseVideosResponse(response.get("archive").getAsJsonArray());
                    for (Video video : videos) {
                        adapter.add(video);
                    }
                    adapter.notifyArrayItemRangeChanged(0, videos.size());
                    startEntranceTransition();
                }
            }

            @Override
            public void onError(int statusCode, Exception e, JsonObject response) {

            }
        });
    }

    private void setupArchiveView() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        VideoPresenter videoPresenter = new VideoPresenter();
        adapter = new ArrayObjectAdapter(videoPresenter);
        setAdapter(adapter);

        setOnItemViewClickedListener(this);
        setOnItemViewSelectedListener(this);
    }

    private ArrayList<Video> parseVideosResponse(JsonArray jsonArray) {
        ArrayList<Video> videos = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            JsonObject jsonVideo = element.getAsJsonObject();
            Video video = ResponseFactory.parseVideo(jsonVideo);
            videos.add(video);
        }
        return videos;
    }

    private void openVideoPlayer(Video video) {
        Bundle arguments = new Bundle();
        arguments.putParcelable("video", video);

        VideoPlaybackFragment videoPlaybackFragment = VideoPlaybackFragment.newInstance();
        videoPlaybackFragment.setArguments(arguments);

        ((MainActivity) getActivity()).addFragment(videoPlaybackFragment);
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                              RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item != null) {
            openVideoPlayer((Video) item);
        }
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                               RowPresenter.ViewHolder rowViewHolder, Row row) {

    }
}
