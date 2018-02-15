package com.chaemil.hgms.service.api;

import android.content.Context;

import com.chaemil.hgms.utils.SmartLog;
import com.chaemil.hgms.viewmodel.model.BindableString;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import java.util.HashMap;


/**
 * Created by Michal Mlejnek on 23/10/2017.
 */

public abstract class JsonFutureCallback implements FutureCallback<Response<JsonObject>> {

    private HashMap<String, BindableString> form;
    private Context context;

    public JsonFutureCallback() {
    }

    public JsonFutureCallback(Context context) {
        this.context = context;
    }

    public JsonFutureCallback(HashMap<String, BindableString> form) {
        this.form = form;
    }

    @Override
    public void onCompleted(Exception e, Response<JsonObject> response) {
        String exception = null;
        String method = "";
        String requestBody = "";
        String uri = "";
        String headers = "";
        String responseHeaders = "";
        int statusCode = -1;
        JsonObject result = new JsonObject();

        if (response != null) {
            if (response.getRequest() != null) {
                method = response.getRequest().getMethod();
                uri = response.getRequest().getUri().toString();
                if (response.getRequest().getBody() != null) {
                    if (response.getRequest().getBody().get() != null) {
                        requestBody = response.getRequest().getBody().get().toString();
                    }
                }
                if (response.getRequest().getHeaders() != null) {
                    headers = response.getRequest().getHeaders().toString();
                }
            }
            if (response.getHeaders() != null) {
                statusCode = response.getHeaders().code();
                if (response.getHeaders().getHeaders() != null) {
                    responseHeaders = response.getHeaders().getHeaders().toString();
                }
            }
            if (response.getException() != null) {
                exception = response.getException().getLocalizedMessage();
            }
            if (response.getResult() != null) {
                result = response.getResult();
            }
        }

        if (exception != null) {
            SmartLog.e("Request Exception", exception);
        }
        SmartLog.d("Request Method", method);
        SmartLog.d("Request URI", uri);
        SmartLog.d("Request Headers", headers);
        SmartLog.d("Request Body", requestBody);
        SmartLog.d("Response Status Code", statusCode);
        SmartLog.d("Response Headers", responseHeaders);
        SmartLog.d("Response Body", result);

        if (statusCode >= 400 && statusCode <= 599) {
            onError(statusCode, e, result);
        } else {
            onSuccess(statusCode, result);
        }
    }

    public abstract void onSuccess(int statusCode, JsonObject response);

    public abstract void onError(int statusCode, Exception e, JsonObject response);

    public JsonFutureCallback setContext(Context context) {
        this.context = context;
        return this;
    }

    public Context getContext() {
        return context;
    }
}
