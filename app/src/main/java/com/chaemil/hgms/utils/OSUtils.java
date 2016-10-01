package com.chaemil.hgms.utils;

import android.os.Build;

/**
 * Created by chaemil on 1.10.16.
 */

public class OSUtils {

    public static boolean isRunningNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

}
