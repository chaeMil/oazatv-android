package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chaemil.hgms.R;

/**
 * Created by chaemil on 19.4.16.
 */
public class CategoriesFragment extends BaseFragment {

    private Bundle upUI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.categories_fragment, container, false);

        getUI(rootView);
        setupUI(savedInstanceState);

        return rootView;
    }

    private void getUI(ViewGroup rootView) {

    }

    public void setupUI(Bundle upUI) {
        this.upUI = upUI;
    }
}
