package com.chaemil.hgms.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.adapter.PhotosAdapter;
import com.chaemil.hgms.utils.SmartLog;

import java.util.ArrayList;

public class PhotosFragment extends Fragment {
    private PhotosAdapter adapter;
    private GridView grid;
    private TextView photoHint;
    private ImageView arrowToCamera;

    private int thumbWidth;
    private ArrayList<String> filepaths = new ArrayList();
    private ArrayList<String> filenames = new ArrayList();

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
        adapter = new PhotosAdapter(getActivity(), thumbWidth, filepaths, filenames);
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

    public static PhotosFragment newInstance() {
        PhotosFragment fragment = new PhotosFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }
}