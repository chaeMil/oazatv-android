package com.chaemil.hgms.utils;

import java.util.Locale;

/**
 * Created by chaemil on 16.4.16.
 */
public class LocalUtils {

    public static String getLocale() {
        switch (Locale.getDefault().getLanguage()) {

            case Constants.SK:
                return Constants.CS;
            case Constants.CS:
                return Constants.CS;
            case Constants.EN:
                return Constants.EN;
            default:
                return Constants.EN;
        }
    }
}
