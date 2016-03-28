package com.chaemil.hgms.factory;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.SmartLog;

import org.json.JSONObject;

import java.net.URLEncoder;

public class RequestFactory {
    public static final int DEFAULT_TIMEOUT_MS = 10000;

    public static JsonObjectRequest search(RequestFactoryListener listener, String query) {
        String url = Constants.API_SEARCH + URLEncoder.encode(query) + "/?limit=20";

        JSONObject jsonObject = new JSONObject();

        SmartLog.Log(SmartLog.LogLevel.INFO, "search", "get " + url);
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.SEARCH),
                createMyReqErrorListener(listener));
    }

    public static JsonObjectRequest getArchive(RequestFactoryListener listener, int page) {
        String url = Constants.API_GET_ARCHIVE + page;

        JSONObject jsonObject = new JSONObject();

        SmartLog.Log(SmartLog.LogLevel.INFO, "getArchive", "get " + url);
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.GET_ARCHIVE),
                createMyReqErrorListener(listener));
    }

    public static JsonObjectRequest getPhotoAlbum(RequestFactoryListener listener, String albumHash) {
        String url = Constants.API_GET_PHOTO_ALBUM + albumHash;

        JSONObject jsonObject = new JSONObject();

        SmartLog.Log(SmartLog.LogLevel.INFO, "getPhotoAlbum", "get " + url);
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.GET_PHOTO_ALBUM),
                createMyReqErrorListener(listener));
    }

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
