package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.android.volley.VolleyError;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.utils.SmartLog;
import com.orm.SugarContext;

import org.json.JSONObject;

/**
 * Created by chaemil on 18.12.15.
 */
public class BaseFragment extends Fragment implements RequestFactoryListener {

    private FragmentManager fragmentManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SugarContext.init(getActivity());
    }

    public void goBack() {
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStack();
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        //SmartLog.Log(SmartLog.LogLevel.DEBUG, "response", String.valueOf(response));
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        SmartLog.Log(SmartLog.LogLevel.ERROR,
                "jsonResponse",
                String.valueOf(BaseActivity.responseError(exception, getActivity())));

        JSONObject jsonError = BaseActivity.responseError(exception, getActivity());

        SmartLog.Log(SmartLog.LogLevel.ERROR, "jsonError", String.valueOf(jsonError));
    }
}
