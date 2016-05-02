package com.chaemil.hgms.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.PhotosAdapter;
import com.chaemil.hgms.adapter.PhotosViewPagerAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Photo;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.DimensUtils;
import com.chaemil.hgms.utils.SmartLog;

import org.json.JSONObject;

public class PhotoAlbumFragment extends BaseFragment implements RequestFactoryListener, View.OnClickListener {
    public static final String TAG = "PhotoAlbumFragment";
    private PhotoAlbum album;
    private PhotosAdapter adapter;
    private GridView grid;
    private int thumbWidth;
    private ViewPager photosViewPager;
    private PhotosViewPagerAdapter photosAdapter;
    private ImageButton back;
    private ImageView download;
    private ProgressBar progress;
    private ImageView retry;
    private TextView description;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "PhotoAlbumFragment", "onCreate");
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "PhotoAlbumFragment", "onCreateView");

        View view = inflater.inflate(R.layout.photos_fragment, container, false);
        getUI(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (album != null && album.getHash() != null) {
            AnalyticsService.getInstance().setPage(AnalyticsService.Pages.PHOTOALBUM_FRAGMENT + "albumHash: " + album.getHash());
        }
    }

    public void setAlbum(PhotoAlbum album) {
        this.album = album;
        getData();
    }

    public PhotoAlbum getAlbum() {
        return album;
    }

    private void getData() {
        if (progress != null) {
            progress.setVisibility(View.VISIBLE);
        }
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "getData", album.getHash());
        JsonObjectRequest getPhotos = RequestFactory.getPhotoAlbum(this, album.getHash());
        MyRequestService.getRequestQueue().add(getPhotos);
    }

    private void getUI(View rootView) {
        grid = (GridView) rootView.findViewById(R.id.gridView);
        photosViewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        back = (ImageButton) rootView.findViewById(R.id.back);
        download = (ImageView) rootView.findViewById(R.id.download);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        retry = (ImageView) rootView.findViewById(R.id.retry);
        description = (TextView) rootView.findViewById(R.id.description);
    }

    private void setupUI() {
        download.setOnClickListener(this);
        back.setOnClickListener(this);
        retry.setOnClickListener(this);
        thumbWidth = getThumbWidth();
        photosViewPager.setOffscreenPageLimit(1);
        photosViewPager.removeAllViews();
        grid.setColumnWidth(getThumbWidth());
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showPhoto(position);
            }
        });

        adapter = new PhotosAdapter(getActivity(), thumbWidth, album.getPhotos());
        grid.setAdapter(adapter);
        photosAdapter = new PhotosViewPagerAdapter(getChildFragmentManager(), album.getPhotos());
        photosViewPager.setAdapter(photosAdapter);

        adapter.notifyDataSetChanged();
        photosAdapter.notifyDataSetChanged();

        adjustLayout();
    }

    public void showPhoto(int position) {
        ((MainActivity) getActivity()).getMainFragment().getAppBar().setVisibility(View.GONE);
        photosViewPager.setCurrentItem(position, false);
        photosViewPager.setVisibility(View.VISIBLE);
        back.setVisibility(View.VISIBLE);
        download.setVisibility(View.VISIBLE);
    }

    public void hidePhotos() {
        ((MainActivity) getActivity()).getMainFragment().getAppBar().setVisibility(View.VISIBLE);
        photosViewPager.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        download.setVisibility(View.GONE);
    }

    public void adjustLayout() {
        if (isAdded()) {
            grid.setColumnWidth(getThumbWidth());

            if (album.getDescription().equals("")) {
                description.setVisibility(View.GONE);
            } else {
                description.setText(album.getDescription());
                grid.setPadding(0,
                        DimensUtils.getTextViewHeight(description),
                        0, 0);
            }
        }
    }

    private int getThumbWidth() {
        if (getActivity() != null) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            if (width < height) {
                return width / 3;
            } else {
                return width / 5;
            }
        }
        return 0;
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        switch (requestType) {
            case GET_PHOTO_ALBUM:
                PhotoAlbum photoAlbum = ResponseFactory.parseAlbum(response);
                progress.setVisibility(View.GONE);
                if (photoAlbum != null && photoAlbum.getPhotos().size() > 0) {
                    album.setPhotos(null);
                    album.setPhotos(photoAlbum.getPhotos());
                    setupUI();
                }
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        super.onErrorResponse(exception, requestType);

        progress.setVisibility(View.GONE);
        retry.setVisibility(View.VISIBLE);
    }

    public GridView getGrid() {
        return grid;
    }

    public ViewPager getPhotosViewPager() {
        return photosViewPager;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                hidePhotos();
                break;
            case R.id.download:
                Photo photo = album.getPhotos().get(photosViewPager.getCurrentItem());
                ((BaseActivity) getActivity()).downloadPhoto(photo);
                break;
            case R.id.retry:
                retry.setVisibility(View.GONE);
                getData();
                break;
        }
    }
}