package com.chaemil.hgms.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import java.util.Locale;

/**
 * Created by chaemil on 1.10.16.
 */

public class OSUtils {

    public static boolean isRunningNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isRunningMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean hasPhoneFeature(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public static void openPlayStore(Context context) {
        final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    //fix for NotificationCompat.MediaStyle
    // http://stackoverflow.com/questions/34851943/couldnt-expand-remoteviews-mediasessioncompat-and-notificationcompat-mediastyl
    public static boolean isHuawei() {
        return (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1
                || android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP)
            && Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("huawei");
    }
}
