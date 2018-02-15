package com.chaemil.hgms.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

/**
 * Created by Michal Mlejnek on 15/02/2018.
 */

public class TVUtils {
    public static boolean isTV(Context context) {
        return(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEVISION)
                || context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK));
    }
}
