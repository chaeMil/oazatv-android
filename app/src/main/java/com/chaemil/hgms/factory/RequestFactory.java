package com.chaemil.hgms.factory;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.utils.AnalyticsUtils;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.SmartLog;

import org.json.JSONObject;

import java.net.URLEncoder;

public class RequestFactory {
    public static final int DEFAULT_TIMEOUT_MS = 10000;

    public static JsonObjectRequest search(RequestFactoryListener listener, String query) {
        String url = Constants.API_SEARCH + URLEncoder.encode(query) + "/?limit=20";

        JSONObject jsonObject = new JSONObject();

        SmartLog.i("search", "get " + url);
        SmartLog.d("json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.SEARCH),
                createMyReqErrorListener(listener, RequestType.SEARCH));
    }

    public static JsonObjectRequest getArchive(RequestFactoryListener listener, int page) {
        String url = Constants.API_GET_ARCHIVE + page;

        JSONObject jsonObject = new JSONObject();

        SmartLog.i("getArchive", "get " + url);
        SmartLog.d("json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.GET_ARCHIVE),
                createMyReqErrorListener(listener, RequestType.GET_ARCHIVE));
    }

    public static JsonObjectRequest getPhotoAlbum(RequestFactoryListener listener, String albumHash) {
        String url = Constants.API_GET_PHOTO_ALBUM + albumHash;

        JSONObject jsonObject = new JSONObject();

        SmartLog.i("getPhotoAlbum", "get " + url);
        SmartLog.d("json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.GET_PHOTO_ALBUM),
                createMyReqErrorListener(listener, RequestType.GET_PHOTO_ALBUM));
    }

    public static JsonObjectRequest getHomepage(RequestFactoryListener listener) {
        String url = Constants.API_GET_HOMEPAGE;
        int appVersionCode = AnalyticsUtils.getAppVersionCode();
        url += "?appVersionCode=" + appVersionCode;

        JSONObject jsonObject = new JSONObject();

        SmartLog.i("getHomePage", "get " + url);
        SmartLog.d("json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.GET_HOMEPAGE),
                createMyReqErrorListener(listener, RequestType.GET_HOMEPAGE));
    }

    public static JsonObjectRequest getLiveStream(RequestFactoryListener listener) {
        String url = Constants.API_GET_LIVESTREAM;

        JSONObject jsonObject = new JSONObject();

        SmartLog.i("getLiveStream", "get " + url);
        SmartLog.d("json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.GET_LIVESTREAM),
                createMyReqErrorListener(listener, RequestType.GET_LIVESTREAM));
    }

    public static JsonObjectRequest postAnalyticsAlive(RequestFactoryListener listener,
                                                       Context context, String ip, String page) {
        String userId = AnalyticsUtils.getDeviceUniqueID(context);
        String os = AnalyticsUtils.getAndroidVersion();
        String browser = AnalyticsUtils.getAppVersion();

        String url = Constants.API_POST_ANALYTICS_PING_ALIVE
                + "?oazaUserId=" + URLEncoder.encode(userId)
                + "&ip=" + URLEncoder.encode(ip)
                + "&os=" + URLEncoder.encode(os)
                + "&browser=" + URLEncoder.encode(browser)
                + "&page=" + URLEncoder.encode(page);

        JSONObject jsonObject = new JSONObject();

        SmartLog.i("postAnalyticsAlive", "post " + url);
        SmartLog.d("json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.POST_ANALYTICS_ALIVE),
                createMyReqErrorListener(listener, RequestType.POST_ANALYTICS_ALIVE));
    }

    public static JsonObjectRequest postVideoView(RequestFactoryListener listener, String hash) {
        if (OazaApp.DEVELOPMENT) {
            hash = "";
        }
        String url = Constants.API_POST_VIEW_PREFIX + hash + Constants.API_POST_VIEW_SUFFIX;

        JSONObject jsonObject = new JSONObject();

        SmartLog.i("postVideoView", "post " + url);
        SmartLog.d("json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.POST_VIDEO_VIEW),
                createMyReqErrorListener(listener, RequestType.POST_VIDEO_VIEW));
    }

    public static JsonObjectRequest getCategories(RequestFactoryListener listener, boolean videos,
                                                  int categoryId, int page, int perPage) {

        int videosInt;
        if (videos) {
            videosInt = 1;
        } else {
            videosInt = 0;
        }

        String url = Constants.API_GET_CATEGORIES;

        if (categoryId != 0) {
            url += "?categoryId=" + categoryId +
                    "&page=" + page +
                    "&perPage=" + perPage;
        } else {
            url += "?videos=" + videosInt;
        }

        JSONObject jsonObject = new JSONObject();

        SmartLog.i("getCategories", "get " + url);
        SmartLog.d("json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.GET_CATEGORIES),
                createMyReqErrorListener(listener, RequestType.GET_CATEGORIES));
    }

    public static JsonObjectRequest getVideo(RequestFactoryListener listener, String videoHash) {
        String url = Constants.API_GET_VIDEO + videoHash;

        JSONObject jsonObject = new JSONObject();

        SmartLog.i("getVideo", "get " + url);
        SmartLog.d("json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.GET_VIDEO),
                createMyReqErrorListener(listener, RequestType.GET_VIDEO));
    }

    public static JsonObjectRequest getSongs(RequestFactoryListener listener) {
        String url = Constants.API_GET_SONGS;

        JSONObject jsonObject = new JSONObject();

        SmartLog.i("getSongs", "get " + url);
        SmartLog.d("json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.GET_SONGS),
                createMyReqErrorListener(listener, RequestType.GET_SONGS));
    }

    public static JsonObjectRequest getSong(RequestFactoryListener listener, int id) {
        String url = Constants.API_GET_SONGS + id + Constants.VIEW;

        JSONObject jsonObject = new JSONObject();

        SmartLog.i("getSong", "get " + url);
        SmartLog.d("json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.GET_SONG),
                createMyReqErrorListener(listener, RequestType.GET_SONG));
    }

    public static JsonObjectRequest getSimilarVideos(String hash, RequestFactoryListener listener) {
        String url = Constants.API_GET_VIDEO + hash + Constants.SIMILAR;

        JSONObject jsonObject = new JSONObject();

        SmartLog.i("getSimilarVideos", "get " + url);
        SmartLog.d("json", String.valueOf(jsonObject));

        return new JsonObjectRequest(Request.Method.GET, url, jsonObject,
                createMyReqSuccessListener(listener, RequestType.GET_SIMILAR_VIDEOS),
                createMyReqErrorListener(listener, RequestType.GET_SIMILAR_VIDEOS));
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
            final RequestFactoryListener listener, final RequestType requestType) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onErrorResponse(error, requestType);
            }
        };
    }
}
