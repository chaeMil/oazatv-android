package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.MyRequestService;

import org.json.JSONObject;


/**
 * Created by chaemil on 2.12.15.
 */
public class ArchiveFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.archive_fragment, container, false);


        getUI(rootView);
        setupUI();
        getData();

        return rootView;

    }

    private void getData() {
        JsonObjectRequest getArchive = RequestFactory.getArchive(this, 0);
        MyRequestService.getRequestQueue().add(getArchive);
    }

    private void setupUI() {

    }

    private void getUI(ViewGroup rootView) {

    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);
    }

    @Override
    public void onErrorResponse(VolleyError exception) {
        super.onErrorResponse(exception);
    }
}
