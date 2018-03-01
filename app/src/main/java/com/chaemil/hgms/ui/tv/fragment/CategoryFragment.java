package com.chaemil.hgms.ui.tv.fragment;

import android.graphics.Color;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Category;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.api.Api;
import com.chaemil.hgms.service.api.JsonFutureCallback;
import com.chaemil.hgms.ui.tv.activity.MainActivity;
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

public class CategoryFragment extends VerticalGridFragment implements OnItemViewClickedListener,
        OnItemViewSelectedListener {
    private static final String TAG = VideoPlaybackFragment.class.getSimpleName();
    private static final int NUM_COLUMNS = 3;
    private static final int PER_PAGE = 10;

    private Bundle arguments;
    private Category category;
    private ArrayObjectAdapter adapter;
    private int currentPage = 0;

    public static CategoryFragment newInstance() {
        Bundle args = new Bundle();
        CategoryFragment fragment = new CategoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arguments = getArguments();
        category = arguments.getParcelable("category");

        if (category != null) {
            setTitle(category.getName());
            setupCategoryView();
            loadCategoryVideos(category.getId(), currentPage);
        } else {
            ((MainActivity) getActivity()).goBack();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View rootView = super.onCreateView(inflater, container, savedInstanceState);
         if (category != null) {
             ((MainActivity) getActivity()).setBackgroundColor(Color.parseColor(category.getColor()));
         }
         return rootView;
    }

    private void loadCategoryVideos(int id, int page) {
        //TODO add pagination
        Api.getVideosFromCategory(getActivity(), id, page, PER_PAGE, new JsonFutureCallback() {
            @Override
            public void onSuccess(int statusCode, JsonObject response) {
                if (response != null && response.has("categories")
                        && response.get("categories").getAsJsonObject().has("videos") ) {
                    ArrayList<Video> videos = parseVideosResponse(response);
                    if (videos.size() > 0) {
                        onVideosLoad(videos);
                    } else {
                        showNothingMoreToLoad();
                    }
                }
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

            @Override
            public void onError(int statusCode, Exception e, JsonObject response) {

            }
        });
    }

    private ArrayList<Video> parseVideosResponse(JsonObject response) {
        ArrayList<Video> videos = new ArrayList<>();
        if (response.get("categories").getAsJsonObject().get("videos").isJsonArray()) {
            JsonArray jsonArray = response.get("categories").getAsJsonObject().get("videos").getAsJsonArray();
            for (JsonElement element : jsonArray) {
                JsonObject jsonVideo = element.getAsJsonObject();
                Video video = ResponseFactory.parseVideo(jsonVideo);
                videos.add(video);
            }
        }
        return videos;
    }

    private void setupCategoryView() {
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

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                              RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item != null) {
            if (item instanceof Video) {
                openVideoPlayer((Video) item);
            } else if (item instanceof LoadMoreItem) {
                loadCategoryVideos(category.getId(), currentPage + 1);
            } else if (item instanceof NoMoreToLoadItem) {
                //intentional do nothing
            }
        }
    }

    private void openVideoPlayer(Video video) {
        Bundle arguments = new Bundle();
        arguments.putParcelable("video", video);

        VideoPlaybackFragment videoPlaybackFragment = VideoPlaybackFragment.newInstance();
        videoPlaybackFragment.setArguments(arguments);

        ((MainActivity) getActivity()).addFragment(videoPlaybackFragment);
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                               RowPresenter.ViewHolder rowViewHolder, Row row) {
        
    }
}
