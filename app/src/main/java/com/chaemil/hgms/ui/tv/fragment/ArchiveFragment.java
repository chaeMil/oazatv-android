package com.chaemil.hgms.ui.tv.fragment;

import android.os.Bundle;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.FocusHighlight;
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
import com.chaemil.hgms.ui.tv.model.HomeArchiveItem;
import com.chaemil.hgms.ui.tv.model.LoadMoreItem;
import com.chaemil.hgms.ui.tv.model.NoMoreToLoadItem;
import com.chaemil.hgms.ui.tv.presenter.VideoPresenter;
import com.chaemil.hgms.utils.StringUtils;
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
    private int currentPage = 1;
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
        loadArchiveVideos(currentPage);
    }

    private void loadArchiveVideos(int page) {
        //TODO add pagination
        Api.getVideosFromArchive(getActivity(), page, new JsonFutureCallback() {
            @Override
            public void onSuccess(int statusCode, JsonObject response) {
                if (response != null && response.has("archive")) {
                    ArrayList<Video> videos = parseVideosResponse(response);
                    if (videos.size() > 0) {
                        onVideosLoad(videos);
                    } else {
                        showNothingMoreToLoad();
                    }
                }
            }

            @Override
            public void onError(int statusCode, Exception e, JsonObject response) {

            }

            private void onVideosLoad(ArrayList<Video> videos) {
                if (adapter.size() != 0 && adapter.get(adapter.size() - 1) != null) {
                    adapter.replace(adapter.size() - 1, videos.get(0));
                }
                for (int i = 1; i < videos.size(); i++) {
                    adapter.add(videos.get(i));
                }
                LoadMoreItem loadMoreItem = new LoadMoreItem(getString(R.string.load_more),
                        StringUtils.colorToHex(getResources().getColor(R.color.md_blue_grey_800)));
                adapter.add(loadMoreItem);
                startEntranceTransition();
                currentPage = page;
            }

            private void showNothingMoreToLoad() {
                NoMoreToLoadItem noMoreToLoadItem = new NoMoreToLoadItem(getString(R.string.no_more_to_load),
                        StringUtils.colorToHex(getResources().getColor(R.color.md_blue_grey_800)));
                if (adapter.size() != 0 && adapter.get(adapter.size() - 1) != null) {
                    adapter.replace(adapter.size() - 1, noMoreToLoadItem);
                }
            }
        });
    }

    private void setupArchiveView() {
        VerticalGridPresenter gridPresenter =
                new VerticalGridPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM, false);
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        VideoPresenter videoPresenter = new VideoPresenter();
        adapter = new ArrayObjectAdapter(videoPresenter);
        setAdapter(adapter);

        setOnItemViewClickedListener(this);
        setOnItemViewSelectedListener(this);
    }

    private ArrayList<Video> parseVideosResponse(JsonObject response) {
        ArrayList<Video> videos = new ArrayList<>();
        if (response.get("archive").isJsonArray()) {
            JsonArray jsonArray = response.get("archive").getAsJsonArray();
            for (JsonElement element : jsonArray) {
                JsonObject jsonVideo = element.getAsJsonObject();
                Video video = ResponseFactory.parseVideo(jsonVideo);
                videos.add(video);
            }
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
            if (item instanceof Video) {
                openVideoPlayer((Video) item);
            } else if (item instanceof LoadMoreItem) {
                loadArchiveVideos(currentPage + 1);
            } else if (item instanceof NoMoreToLoadItem) {
                //intentional do nothing
            }
        }
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                               RowPresenter.ViewHolder rowViewHolder, Row row) {

    }
}
