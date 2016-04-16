package com.chaemil.hgms.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.chaemil.hgms.BuildConfig;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.ion.Ion;

import org.w3c.dom.Document;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by chaemil on 16.4.16.
 */
public class AnalyticsUtils {

    private static String TAG = "AnalyticsUtils";

    public static String getDeviceUniqueID(Context context){
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static Future<String> getPublicIpAddress(Context context) {
        return Ion.with(context).load("https://api.ipify.org?format=json").asString();
    }

    public static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android " + release + " SDK: " + sdkVersion;
    }

    public static String getAppVersion() {
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;

        return "oazatv-android v:" + versionName + " build: " + versionCode;
    }

}
