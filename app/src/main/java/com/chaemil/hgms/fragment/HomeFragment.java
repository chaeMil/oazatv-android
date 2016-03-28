package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.HomepageAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Homepage;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.MyRequestService;

import org.json.JSONObject;

/**
 * Created by chaemil on 2.12.15.
 */
public class HomeFragment extends BaseFragment implements RequestFactoryListener {

    public static final int NEWEST_VIDEOS = 0;
    public static final int NEWEST_ALBUMS = 1;
    public static final int POPULAR_VIDEOS = 3;

    private Homepage homepage;
    private ListView newestVideos;
    private ListView newestAlbums;
    private ListView popularVideos;
    private HomepageAdapter newestVideosAdapter;
    private HomepageAdapter newestAlbumsAdapter;
    private HomepageAdapter popularVideosAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.home_fragment, container, false);

        getData();
        getUI(rootView);
        return rootView;

    }

    private void getData() {
        JsonObjectRequest homepage = RequestFactory.getHomepage(this);
        MyRequestService.getRequestQueue().add(homepage);
    }

    private void setupUI() {
        newestVideosAdapter = new HomepageAdapter(getActivity(), ((MainActivity) getActivity()), homepage, NEWEST_VIDEOS);
        newestVideos.setAdapter(newestVideosAdapter);
        newestAlbumsAdapter = new HomepageAdapter(getActivity(), ((MainActivity) getActivity()), homepage, NEWEST_ALBUMS);
        newestAlbums.setAdapter(newestAlbumsAdapter);
        popularVideosAdapter = new HomepageAdapter(getActivity(), ((MainActivity) getActivity()), homepage, POPULAR_VIDEOS);
        popularVideos.setAdapter(popularVideosAdapter);
    }

    private void getUI(ViewGroup rootView) {
        newestVideos = (ListView) rootView.findViewById(R.id.newest_videos);
        newestAlbums = (ListView) rootView.findViewById(R.id.newest_albums);
        popularVideos = (ListView) rootView.findViewById(R.id.popular_videos);
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_HOMEPAGE:

                homepage = ResponseFactory.parseHomepage(response);
                setupUI();

                break;

        }
    }
}
