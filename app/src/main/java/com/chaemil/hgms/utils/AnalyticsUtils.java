package com.chaemil.hgms.utils;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import com.chaemil.hgms.BuildConfig;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.ion.Ion;

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
        return getDeviceName() + " Android " + release + " SDK: " + sdkVersion;
    }

    public static String getAppVersion() {
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;

        return "oazatv-android v:" + versionName + " build: " + versionCode;
    }

    public static int getAppVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    public static boolean isDisplayOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isScreenOn();
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model.toUpperCase();
        } else {
            return manufacturer + " " + model;
        }
    }

}
