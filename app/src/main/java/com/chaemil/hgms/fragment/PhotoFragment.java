package com.chaemil.hgms.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by chaemil on 21.7.15.
 */
public class PhotoFragment extends Fragment {
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

        Picasso.with(getActivity())
                .load(photo.getThumb2048())
                .into(new Target() {

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        progressBar.setVisibility(View.GONE);
                        image.setImage(ImageSource.bitmap(bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

        label.setText(photo.getDescription());
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
}
