package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.model.Photo;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by chaemil on 21.7.15.
 */
public class PhotoFragment extends Fragment {
    private ImageView image;
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

    private void setupUI() {
        Picasso.with(getActivity())
                .load(photo.getThumb2048())
                .into(image, new Callback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                setupUI();
                            }
                        });

        label.setText(photo.getDescription());
    }

    private void getUI(ViewGroup rootView) {
        image = (ImageView) rootView.findViewById(R.id.image);
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
