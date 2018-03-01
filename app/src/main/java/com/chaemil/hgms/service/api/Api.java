package com.chaemil.hgms.service.api;

import android.content.Context;

import com.chaemil.hgms.BuildConfig;
import com.koushikdutta.ion.Ion;

/**
 * Created by Michal Mlejnek on 15/02/2018.
 */

public class Api {
    public static final String SERVER = "https://oaza.tv/";
    public static final String API_VERSION = "api/v2/";
    public static final String HOMEPAGE = SERVER + API_VERSION;
    public static final String ARCHIVE = SERVER + API_VERSION + "archive/";
    public static final String CATEGORIES = SERVER + API_VERSION + "categories/";
    public static final String LIVE_STREAM = SERVER + API_VERSION + "live/";
    public static final String SEARCH = SERVER + API_VERSION + "search/";

    public static void getVideosFromArchive(Context context, int page, JsonFutureCallback callback) {
        Ion.with(context)
                .load(ARCHIVE + String.valueOf(page))
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }

    public static void getHomePage(Context context, JsonFutureCallback callback) {
        Ion.with(context)
                .load(HOMEPAGE + "?appVersionCode=" + BuildConfig.VERSION_CODE)
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }

    public static void getCategories(Context context, JsonFutureCallback callback) {
        Ion.with(context)
                .load(CATEGORIES)
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }

    public static void getVideosFromCategory(Context context, int id, int page, int perPage, JsonFutureCallback callback) {
        Ion.with(context)
                .load(CATEGORIES + "?categoryId=" + id + "&page=" + page + "&perPage=" + perPage)
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }

    public static void getLiveStream(Context context, JsonFutureCallback callback) {
        Ion.with(context)
                .load(LIVE_STREAM)
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }

    public static void getSearch(Context context, String input, JsonFutureCallback callback) {
        Ion.with(context)
                .load(SEARCH + input)
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }
}
