package com.chaemil.hgms.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.SmartLog;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by chaemil on 21.9.15.
 */
public class BaseActivity extends AppCompatActivity implements RequestFactoryListener {

    public void setFullscreen(boolean full) {
        if (full) {

            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);

        } else {

            this.getWindow()
                .getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

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

            SuperToast.create(context,
                    context.getString(R.string.exception_network_timeout),
                    SuperToast.Duration.MEDIUM,
                    Style.getStyle(Style.RED)).show();

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
}
