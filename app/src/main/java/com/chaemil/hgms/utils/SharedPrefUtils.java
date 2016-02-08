package com.chaemil.hgms.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtils {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static SharedPrefUtils sharedPrefUtils;

    public static final String APPLICATION_PREFERENCES = "com.chaemil.hgms.preferences";
    public static final String PREFERENCES_DOWNLOAD_ON_WIFI = "download_on_wifi";
    public static final String PREFERENCES_STREAM_ON_WIFI = "stream_on_wifi";
    public static final String PREFERENCES_STREAM_AUDIO = "stream_audio";

    public static SharedPrefUtils getInstance(Context context) {
        if (sharedPrefUtils == null) {
            sharedPrefUtils = new SharedPrefUtils(context);
        }
        return sharedPrefUtils;
    }

    private SharedPrefUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(
                APPLICATION_PREFERENCES,
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveDownloadOnWifi(boolean value) {
        editor.putBoolean(PREFERENCES_DOWNLOAD_ON_WIFI, value);
        editor.commit();
    }

    public boolean loadDownloadOnWifi() {
        return sharedPreferences.getBoolean(PREFERENCES_DOWNLOAD_ON_WIFI, true);
    }

    public void saveStreamOnWifi(boolean value) {
        editor.putBoolean(PREFERENCES_STREAM_ON_WIFI, value);
        editor.commit();
    }

    public boolean loadStreamOnWifi() {
        return sharedPreferences.getBoolean(PREFERENCES_STREAM_ON_WIFI, false);
    }

    public void saveStreamAudio(boolean value) {
        editor.putBoolean(PREFERENCES_STREAM_AUDIO, value);
        editor.commit();
    }

    public boolean loadStreamAudio() {
        return sharedPreferences.getBoolean(PREFERENCES_STREAM_AUDIO, true);
    }

    public void deleteUserData() {
        editor.remove(PREFERENCES_DOWNLOAD_ON_WIFI);
        editor.commit();
    }
}
