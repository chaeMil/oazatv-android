package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.avocarrot.json2view.DynamicView;
import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.RequestFactoryListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chaemil on 2.12.15.
 */
public class HomeFragment extends BaseFragment implements RequestFactoryListener {

    private JSONObject jsonObject;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.home_fragment, container, false);

        try {
            jsonObject = new JSONObject("");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getUI(rootView);
        setupUI();

        return rootView;

    }

    private void setupUI() {

    }

    private void getUI(ViewGroup rootView) {
        View sampleView = DynamicView.createView(getActivity(), jsonObject, rootView);
        if (sampleView != null) {
            rootView.addView(sampleView);
        }
    }
}
