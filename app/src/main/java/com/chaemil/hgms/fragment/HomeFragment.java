package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.Homepage;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.MyRequestService;

import org.json.JSONObject;

/**
 * Created by chaemil on 2.12.15.
 */
public class HomeFragment extends BaseFragment implements RequestFactoryListener {

    private Homepage homepage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.home_fragment, container, false);

        getData();
        getUI(rootView);
        setupUI();

        return rootView;

    }

    private void getData() {
        JsonObjectRequest homepage = RequestFactory.getHomepage(this);
        MyRequestService.getRequestQueue().add(homepage);
    }

    private void setupUI() {

    }

    private void getUI(ViewGroup rootView) {

    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_HOMEPAGE:

                homepage = ResponseFactory.parseHomepage(response);

                break;

        }
    }
}
