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

import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.PhotosAdapter;
import com.chaemil.hgms.adapter.PhotosViewPagerAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.MyRequestService;
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

    public PhotoAlbumFragment(PhotoAlbum album) {
        super();
        this.album = album;
    }

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

    public void setAlbum(PhotoAlbum album) {
        this.album = album;
        getData();
    }

    private void getData() {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "getData", album.getHash());
        JsonObjectRequest getPhotos = RequestFactory.getPhotoAlbum(this, album.getHash());
        MyRequestService.getRequestQueue().add(getPhotos);
    }

    private void getUI(View rootView) {
        grid = (GridView) rootView.findViewById(R.id.gridView);
        photosViewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        back = (ImageButton) rootView.findViewById(R.id.back);
    }

    private void setupUI() {
        back.setOnClickListener(this);
        thumbWidth = getThumbWidth();
        photosViewPager.setOffscreenPageLimit(2);
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
    }

    public void showPhoto(int position) {
        ((MainActivity) getActivity()).changeStatusBarColor(getResources().getColor(R.color.black));
        ((MainActivity) getActivity()).getMainFragment().getAppBar().setVisibility(View.GONE);
        photosViewPager.setCurrentItem(position, false);
        photosViewPager.setVisibility(View.VISIBLE);
        back.setVisibility(View.VISIBLE);
    }

    public void hidePhotos() {
        ((MainActivity) getActivity()).changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        ((MainActivity) getActivity()).getMainFragment().getAppBar().setVisibility(View.VISIBLE);
        photosViewPager.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
    }

    public void adjustLayout() {
        if (isAdded()) {
            grid.setColumnWidth(getThumbWidth());
        }
    }

    private int getThumbWidth() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        if (width < height) {
            return width / 3;
        } else {
            return width / 4;
        }
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        switch (requestType) {
            case GET_PHOTO_ALBUM:
                PhotoAlbum photoAlbum = ResponseFactory.parseAlbum(response);
                if (photoAlbum != null && photoAlbum.getPhotos().size() > 0) {
                    album.setPhotos(null);
                    album.setPhotos(photoAlbum.getPhotos());
                    setupUI();
                }
                break;
        }
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
        }
    }
}