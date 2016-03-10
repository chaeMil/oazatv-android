package com.chaemil.hgms.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.model.Photo;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.PermissionUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.chaemil.hgms.utils.StringUtils;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by chaemil on 21.9.15.
 */
public class BaseActivity extends AppCompatActivity implements RequestFactoryListener {

    private Photo photoToDownload;
    private DownloadManager.Request request;
    private DownloadManager manager;
    public boolean fullscreen = false;

    public void setFullscreen(boolean full) {

        View decorView = getWindow().getDecorView();

        if (full) {
            fullscreen = true;
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            fullscreen = false;
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "response", String.valueOf(response));
    }

    @Override
    public void onErrorResponse(VolleyError exception) {
        SmartLog.Log(SmartLog.LogLevel.ERROR,
                "jsonResponse",
                String.valueOf(responseError(exception, this)));

        JSONObject jsonError = responseError(exception, this);

        SmartLog.Log(SmartLog.LogLevel.ERROR, "jsonError", String.valueOf(jsonError));

        String error = null;

        if (jsonError != null) {
            try {
                if (jsonError.has(Constants.JSON_ERROR)) {
                    error = jsonError.getString(Constants.JSON_ERROR);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (error == null) {
            error = getString(R.string.something_went_wrong);
        }

        SuperToast.create(getApplicationContext(), error,
                SuperToast.Duration.SHORT,
                Style.getStyle(Style.RED)).show();
    }

    public static JSONObject responseError(VolleyError error, Context context) {

        if (error instanceof TimeoutError || error instanceof NoConnectionError) {

            if (context != null) {
                SuperToast.create(context,
                        context.getString(R.string.exception_network_timeout),
                        SuperToast.Duration.MEDIUM,
                        Style.getStyle(Style.RED)).show();
            }

        } else if (error instanceof AuthFailureError) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "AuthFailureError", error.toString());
        } else if (error instanceof ServerError) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "ServerError", error.toString());
        } else if (error instanceof NetworkError) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "NetworkError", error.toString());
        } else if (error instanceof ParseError) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "ParseError", error.toString());
        }

        JSONObject errorResponse = null;
        try {
            if (error != null) {
                if (error.networkResponse != null) {
                    if (error.networkResponse.data != null) {

                        SmartLog.Log(SmartLog.LogLevel.ERROR,
                                "error",
                                new String(error.networkResponse.data));

                        errorResponse = new JSONObject(new String(error.networkResponse.data));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return errorResponse;
    }

    public void downloadPhoto(Photo photo) {

        request = new DownloadManager.Request(Uri.parse(photo.getThumb2048()));
        request.setDescription(getString(R.string.downloading_photo));
        if (!photo.getDescription().equals("")) {
            request.setTitle(photo.getDescription());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                getString(R.string.app_name) + "_" + StringUtils.randomString(8) + ".jpg");

        manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        if (Build.VERSION.SDK_INT >= 23) {
            photoToDownload = photo;
            if (PermissionUtils.isStoragePermissionGranted(this)) {
                manager.enqueue(request);
            }
        } else {
            manager.enqueue(request);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(PermissionUtils.TAG, "Permission: " + permissions[0]+ "was " + grantResults[0]);

            if (photoToDownload != null) {
                manager.enqueue(request);
                photoToDownload = null;
            }
        } else {
            SuperToast.create(this, getString(R.string.permission_revoked), SuperToast.Duration.MEDIUM).show();
        }
    }
}
