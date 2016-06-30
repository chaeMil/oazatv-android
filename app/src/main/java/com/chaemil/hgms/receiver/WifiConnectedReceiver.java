package com.chaemil.hgms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.activity.MainActivity;

/**
 * Created by chaemil on 8.2.16.
 */
public class WifiConnectedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();

        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Log.d("WifiReceiver", "Have Wifi Connection");

            /*Intent downloadService = new Intent(context, DownloadService.class);
            context.startService(downloadService);*/

        }

        else {
            Log.d("WifiReceiver", "Don't have Wifi Connection");

            /*if (((OazaApp) context.getApplicationContext()).getDownloadService() != null) {
                if (SharedPrefUtils.getInstance(context).loadDownloadOnlyOnWifi()) {
                    ((OazaApp) context.getApplicationContext()).getDownloadService().killCurrentDownload();
                }
            }*/

        }

        MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
        if (mainActivity != null) {
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                mainActivity.hideStatusMessage();
                mainActivity.setupLiveRequestTimer();
            }
            if (netInfo != null && !netInfo.isConnected()) {
                mainActivity.noConnectionMessage();
            }
        }

    }
}
