package com.chaemil.hgms.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.model.Photo;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

/**
 * Created by chaemil on 21.7.15.
 */
public class PhotoFragment extends Fragment implements FutureCallback<Bitmap> {
    private SubsamplingScaleImageView image;
    private TextView label;
    private Photo photo;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.photoalbum_photo, container, false);

        photo = getArguments().getParcelable(Photo.PHOTO);

        getUI(rootView);
        setupUI();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setupUI() {

        Ion.with(getActivity())
                .load(photo.getThumb2048())
                .asBitmap()
                .setCallback(this);

        if (!photo.getDescription().trim().equals("")) {
            label.setText(photo.getDescription());
        } else {
            label.setVisibility(View.GONE);
        }

    }

    private void getUI(ViewGroup rootView) {
        image = (SubsamplingScaleImageView) rootView.findViewById(R.id.image);
        label = (TextView) rootView.findViewById(R.id.label);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
    }

    public static PhotoFragment newInstance(Photo photo) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Photo.PHOTO, photo);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCompleted(Exception e, Bitmap result) {
        image.setImage(ImageSource.bitmap(result));
        progressBar.setVisibility(View.GONE);
    }
}
