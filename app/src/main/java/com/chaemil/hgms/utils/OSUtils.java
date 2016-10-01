package com.chaemil.hgms.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

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
}
