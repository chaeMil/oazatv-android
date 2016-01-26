package com.chaemil.hgms.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.adapter.PhotosAdapter;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.utils.SmartLog;

public class PhotosFragment extends Fragment {
    public static final String TAG = "PhotosFragment";
    private final PhotoAlbum album;
    private PhotosAdapter adapter;
    private GridView grid;

    private int thumbWidth;

    public PhotosFragment(PhotoAlbum album) {
        super();
        this.album = album;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "PhotosFragment", "onCreate");
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "PhotosFragment", "onCreateView");

        View view = inflater.inflate(R.layout.photos_fragment, container, false);

        getUI(view);
        setupUI();

        return view;
    }

    private void getUI(View rootView) {
        grid = (GridView) rootView.findViewById(R.id.gridView);
    }

    private void setupUI() {
        if (thumbWidth == 0) {
            thumbWidth = getThumbWidth();
            grid.setColumnWidth(getThumbWidth());
        }
        adapter = new PhotosAdapter(getActivity(), thumbWidth, album.getPhotos());
        grid.setAdapter(adapter);
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
}