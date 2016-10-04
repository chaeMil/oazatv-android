package com.chaemil.hgms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.service.TrackerService;

/**
 * Created by chaemil on 8.2.16.
 */
public class WifiConnectedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();

        if (netInfo != null && !netInfo.isConnected()) {
            if (OazaApp.TRACKER) {
                try {
                    context.startService(new Intent(context, TrackerService.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
