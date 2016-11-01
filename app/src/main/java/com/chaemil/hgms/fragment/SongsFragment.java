package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chaemil.hgms.R;

/**
 * Created by chaemil on 1.11.16.
 */

public class SongsFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.songs_fragment, container, false);

        getUI(rootView);
        setupUI();

        return rootView;
    }

    private void setupUI() {

    }

    private void getUI(ViewGroup rootView) {

    }
}
