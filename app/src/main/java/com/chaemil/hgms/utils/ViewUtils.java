package com.chaemil.hgms.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by chaemil on 17.1.17.
 */

public class ViewUtils {

    public static void setMargins (Context c, View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            if (p != null) {
                p.setMargins((int) DimensUtils.pxFromDp(c, l),
                        (int) DimensUtils.pxFromDp(c, t),
                        (int) DimensUtils.pxFromDp(c, r),
                        (int) DimensUtils.pxFromDp(c, b));
                v.requestLayout();
            }
        }
    }
}
