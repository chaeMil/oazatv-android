package com.chaemil.hgms.fragment;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.adapter.PhotosAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.SmartLog;

import org.json.JSONObject;

public class PhotoAlbumFragment extends Fragment implements RequestFactoryListener {
    public static final String TAG = "PhotoAlbumFragment";
    private PhotoAlbum album;
    private PhotosAdapter adapter;
    private GridView grid;

    private int thumbWidth;

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
    }

    private void setupUI() {
        thumbWidth = getThumbWidth();
        grid.setColumnWidth(getThumbWidth());
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
            return height / 4;
        }
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        switch (requestType) {
            case GET_PHOTO_ALBUM:
                PhotoAlbum photoAlbum = ResponseFactory.parseAlbum(response);
                if (photoAlbum != null && photoAlbum.getPhotos().size() > 0) {
                    setupUI();
                    album.setPhotos(photoAlbum.getPhotos());
                    adapter = new PhotosAdapter(getActivity(), thumbWidth, album.getPhotos());
                    grid.setAdapter(adapter);
                }
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError exception) {

    }
}