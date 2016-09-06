package com.chaemil.hgms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.service.TrackerService;

/**
 * Created by chaemil on 8.2.16.
 */
public class WifiConnectedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();

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

        if (OazaApp.TRACKER) {
            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                context.stopService(new Intent(context, TrackerService.class));
                context.startActivity(new Intent(context, TrackerService.class));
            }
        }
    }
}
