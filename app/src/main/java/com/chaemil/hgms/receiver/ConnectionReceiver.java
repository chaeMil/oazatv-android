package com.chaemil.hgms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.chaemil.hgms.service.TrackerService;

/**
 * Created by chaemil on 9.12.16.
 */

public class ConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(info != null && info.isConnected()) {
            if (TrackerService.shouldTrack(context)) {
                context.startService(new Intent(context, TrackerService.class));
            }
        }
    }
}
