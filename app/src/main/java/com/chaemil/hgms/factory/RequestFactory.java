package com.chaemil.hgms.factory;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.chaemil.hgms.model.RequestType;

import org.json.JSONObject;

public class RequestFactory {
    public static final int DEFAULT_TIMEOUT_MS = 10000;


    private static Response.Listener<JSONObject> createMyReqSuccessListener(
            final RequestFactoryListener listener, final RequestType requestType) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                listener.onSuccessResponse(response, requestType);
            }
        };
    }

    private static Response.ErrorListener createMyReqErrorListener(
            final RequestFactoryListener listener) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onErrorResponse(error);
            }
        };
    }
}
