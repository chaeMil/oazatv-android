package com.chaemil.hgms.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by chaemil on 4.4.16.
 */
public class NetworkUtils {

    public static boolean isConnected(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
    }
}
