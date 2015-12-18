package com.chaemil.hgms.factory;

import com.android.volley.VolleyError;
import com.chaemil.hgms.model.RequestType;

import org.json.JSONObject;

public interface RequestFactoryListener {
    public void onSuccessResponse(JSONObject response, RequestType requestType);
    public void onErrorResponse(VolleyError exception);
}