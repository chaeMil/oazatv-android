package com.chaemil.hgms.service;

import android.content.Context;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.utils.AnalyticsUtils;
import com.chaemil.hgms.utils.NetworkUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chaemil on 16.4.16.
 */
public class AnalyticsService implements RequestFactoryListener {

    private static AnalyticsService analyticsService;
    private static final String TAG = "AnalyticsService";

    private Context context;
    private String page = "";
    private String ip = "";

    public AnalyticsService(final Context context) {
        this.context = context;

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                postKeepAlive(page);
            }
        }, 0, 10 * 1000);
    }

    public static AnalyticsService getInstance() {
        if (analyticsService != null) {
            return analyticsService;
        } else {
            throw new IllegalStateException("AnalyticsService not initialized");
        }
    }

    public static void init(Context context) {
        analyticsService = new AnalyticsService(context);

        Future<String> jsonIp = AnalyticsUtils.getPublicIpAddress(context);
        jsonIp.setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                try {
                    if (result != null) {
                        JSONObject jsonArray = new JSONObject(result);
                        analyticsService.setIp(jsonArray.getString("ip"));
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public void setPage(String page) {
        this.page = page;
    }

    private void postKeepAlive(String page) {
        if (NetworkUtils.isConnected(context) && AnalyticsUtils.isDisplayOn(context)) {
            JsonObjectRequest keepAlive = RequestFactory.postAnalyticsAlive(this, context, ip, page);
            MyRequestService.getRequestQueue().add(keepAlive);
        }
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        try {
            SmartLog.Log(SmartLog.LogLevel.DEBUG, TAG, response.toString());
        } catch (Exception e) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, TAG, e.toString());
        }
    }

    @Override
    public void onErrorResponse(VolleyError exception) {
        SmartLog.Log(SmartLog.LogLevel.ERROR, TAG, exception.toString());
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public static class Pages {
        public static final String HOME_FRAGMENT = "HomeFragment";
        public static final String ARCHIVE_FRAGMENT = "ArchiveFragment";
        public static final String DOWNLOADED_FRAGMENT = "DownloadedFragment";
        public static final String PHOTOALBUM_FRAGMENT = "PhotoalbumFragment";
        public static final String VIDEOPLAYER_FRAGMENT = "VideoPlayerFragment";
        public static final String LIVESTREAM = "live-stream";
        public static final String AUDIOPLAYER_FRAGMENT = "AudioPlayerFragment";
    }
}
