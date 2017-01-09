package com.chaemil.hgms.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class SharedPrefUtils {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static SharedPrefUtils sharedPrefUtils;

    public static final String APPLICATION_PREFERENCES = "com.chaemil.hgms.preferences";
    public static final String PREFERENCES_DOWNLOAD_ON_WIFI = "download_on_wifi";
    public static final String PREFERENCES_STREAM_ON_WIFI = "stream_on_wifi";
    public static final String PREFERENCES_STREAM_AUDIO = "stream_audio";
    public static final String PREFERENCE_USER_ID = "user_id";
    public static final String HIDDEN_VIDEOS = "hidden_videos";
    public static final String PREFERENCES_FIRST_LAUNCH = "first_launch_2016-06-30";

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

    public void createUserId() {
        editor.putString(PREFERENCE_USER_ID, StringUtils.randomString(8));
        editor.commit();
    }

    public String loadUserId() {
        return sharedPreferences.getString(PREFERENCE_USER_ID, null);
    }

    public void saveFirstLaunch(boolean value) {
        editor.putBoolean(PREFERENCES_FIRST_LAUNCH, value);
        editor.commit();
    }

    public boolean loadFirstLaunch() {
        return sharedPreferences.getBoolean(PREFERENCES_FIRST_LAUNCH, true);
    }

    public void deleteUserData() {
        editor.remove(PREFERENCES_FIRST_LAUNCH);
        editor.remove(PREFERENCES_STREAM_AUDIO);
        editor.remove(PREFERENCES_STREAM_ON_WIFI);
        editor.remove(PREFERENCES_DOWNLOAD_ON_WIFI);
        editor.commit();
    }

    public Set<String> getHiddenVideos() {
        return sharedPreferences.getStringSet(HIDDEN_VIDEOS, new HashSet<String>());
    }

    public void addHiddenVideo(Context context, String hash) {
        Set<String> hiddenVideos = getInstance(context).getHiddenVideos();
        hiddenVideos.add(hash);
        editor.putStringSet(HIDDEN_VIDEOS, hiddenVideos);
        editor.commit();
    }
}
