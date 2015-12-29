package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chaemil.hgms.R;

/**
 * Created by chaemil on 2.12.15.
 */
public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.home_fragment, container, false);


        getUI(rootView);
        setupUI();

        return rootView;

    }

    private void setupUI() {

    }

    private void getUI(ViewGroup rootView) {

    }
}
