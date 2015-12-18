package com.chaemil.hgms.fragment;

import android.support.v4.app.Fragment;

import com.android.volley.VolleyError;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.utils.SmartLog;

import org.json.JSONObject;

/**
 * Created by chaemil on 18.12.15.
 */
public class BaseFragment extends Fragment implements RequestFactoryListener {

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {

    }

    @Override
    public void onErrorResponse(VolleyError exception) {
        SmartLog.Log(SmartLog.LogLevel.ERROR,
                "jsonResponse",
                String.valueOf(BaseActivity.responseError(exception, getActivity())));

        JSONObject jsonError = BaseActivity.responseError(exception, getActivity());

        SmartLog.Log(SmartLog.LogLevel.ERROR, "jsonError", String.valueOf(jsonError));
    }
}
