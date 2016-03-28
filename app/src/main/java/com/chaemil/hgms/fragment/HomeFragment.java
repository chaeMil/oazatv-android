package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.avocarrot.json2view.DynamicView;
import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.BlockDefinition;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.SmartLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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

        getBlockDefinitions();
        getUI(rootView);
        setupUI();

        return rootView;

    }

    private void getBlockDefinitions() {
        JsonObjectRequest request = RequestFactory.getBlockDefinitions(this);
        MyRequestService.getRequestQueue().add(request);
    }

    private void setupUI() {

    }

    private void getUI(ViewGroup rootView) {
        View sampleView = DynamicView.createView(getActivity(), jsonObject, rootView);
        if (sampleView != null) {
            rootView.addView(sampleView);
        }
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_BLOCK_DEFINITIONS:
                ArrayList<BlockDefinition> definitions = ResponseFactory.parseBlockDefinitions(response);

                if (definitions != null && definitions.size() > 0) {
                    try {
                        BlockDefinition.deleteAll(BlockDefinition.class);
                    } catch (Exception e) {
                        SmartLog.Log(SmartLog.LogLevel.WARN, "exception", e.toString());
                    }

                    for (BlockDefinition definition : definitions) {
                        definition.save();
                    }
                }

                break;
        }
    }
}
