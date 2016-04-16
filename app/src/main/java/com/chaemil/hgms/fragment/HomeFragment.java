package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.ArchiveAdapter;
import com.chaemil.hgms.adapter.HomepageAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Homepage;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.AnalyticsUtils;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.json.JSONObject;

import it.sephiroth.android.library.widget.HListView;

/**
 * Created by chaemil on 2.12.15.
 */
public class HomeFragment extends BaseFragment implements RequestFactoryListener {

    public static final int NEWEST_VIDEOS = 0;
    public static final int NEWEST_ALBUMS = 1;
    public static final int POPULAR_VIDEOS = 3;

    private static final int FEATURED = 0;
    private static final int NEW_AND_POPULAR = 1;

    private Homepage homepage;
    private HListView newestVideos;
    private HListView newestAlbums;
    private HListView popularVideos;
    private HomepageAdapter newestVideosAdapter;
    private HomepageAdapter newestAlbumsAdapter;
    private HomepageAdapter popularVideosAdapter;
    private Button featuredButton;
    private Button newAndPopularButton;
    private LinearLayout featuredWrapper;
    private LinearLayout newAndPopularWrapper;
    private int activeView = FEATURED;
    private ArchiveAdapter featuredAdapter;
    private GridView featuredGridView;

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
        if (getActivity() != null) {
            newestVideosAdapter = new HomepageAdapter(getActivity(), ((MainActivity) getActivity()), homepage, NEWEST_VIDEOS);
            newestVideos.setAdapter(newestVideosAdapter);
            newestAlbumsAdapter = new HomepageAdapter(getActivity(), ((MainActivity) getActivity()), homepage, NEWEST_ALBUMS);
            newestAlbums.setAdapter(newestAlbumsAdapter);
            popularVideosAdapter = new HomepageAdapter(getActivity(), ((MainActivity) getActivity()), homepage, POPULAR_VIDEOS);
            popularVideos.setAdapter(popularVideosAdapter);
            featuredAdapter = new ArchiveAdapter(getActivity(), (MainActivity) getActivity(), homepage.featured);
            featuredGridView.setAdapter(featuredAdapter);

            setupSwitcher();
        }
    }

    private void getUI(ViewGroup rootView) {
        newestVideos = (HListView) rootView.findViewById(R.id.newest_videos);
        newestAlbums = (HListView) rootView.findViewById(R.id.newest_albums);
        popularVideos = (HListView) rootView.findViewById(R.id.popular_videos);
        featuredButton = (Button) rootView.findViewById(R.id.featured_button);
        newAndPopularButton = (Button) rootView.findViewById(R.id.new_and_popular_button);
        featuredWrapper = (LinearLayout) rootView.findViewById(R.id.featured_wrapper);
        newAndPopularWrapper = (LinearLayout) rootView.findViewById(R.id.new_and_popular_wrapper);
        featuredGridView = (GridView) rootView.findViewById(R.id.featured_grid_view);
    }

    private void setupSwitcher() {

        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.featured_button:
                        if (activeView != FEATURED) {
                            featuredButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.app_round_button_bg_active));
                            newAndPopularButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.app_round_button_bg));
                            featuredWrapper.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.SlideInLeft).duration(250).playOn(featuredWrapper);
                            YoYo.with(Techniques.SlideOutRight).duration(250).playOn(newAndPopularWrapper);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    newAndPopularWrapper.setVisibility(View.GONE);
                                    activeView = FEATURED;
                                }
                            }, 250);
                        }
                        break;
                    case R.id.new_and_popular_button:
                        if (activeView != NEW_AND_POPULAR) {
                            newAndPopularButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.app_round_button_bg_active));
                            featuredButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.app_round_button_bg));
                            newAndPopularWrapper.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.SlideInRight).duration(250).playOn(newAndPopularWrapper);
                            YoYo.with(Techniques.SlideOutLeft).duration(250).playOn(featuredWrapper);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    featuredWrapper.setVisibility(View.GONE);
                                    activeView = NEW_AND_POPULAR;
                                }
                            }, 250);
                        }
                        break;
                }
            }
        };

        featuredButton.setOnClickListener(click);
        newAndPopularButton.setOnClickListener(click);

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
