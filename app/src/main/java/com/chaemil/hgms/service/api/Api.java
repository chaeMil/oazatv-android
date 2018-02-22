package com.chaemil.hgms.service.api;

import android.content.Context;

import com.koushikdutta.ion.Ion;

/**
 * Created by Michal Mlejnek on 15/02/2018.
 */

public class Api {
    public static final String SERVER = "https://oaza.tv/";
    public static final String API_VERSION = "api/v2/";
    public static final String ARCHIVE = SERVER + API_VERSION + "archive/";

    public static void getVideosFromArchive(Context context, int page, JsonFutureCallback callback) {
        Ion.with(context)
                .load(ARCHIVE + String.valueOf(page))
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }
}
