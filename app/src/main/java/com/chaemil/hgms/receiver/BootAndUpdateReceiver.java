package com.chaemil.hgms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.chaemil.hgms.service.TrackerService;
import com.chaemil.hgms.service.TrackerService;
import com.chaemil.hgms.utils.NetworkUtils;
import com.chaemil.hgms.utils.SmartLog;


/**
 * Created by chaemil on 6.10.15.
 */
public class BootAndUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "BootAndUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ||
                intent.getAction().equals("android.intent.action.MY_PACKAGE_REPLACED")) {

            SmartLog.Log(SmartLog.LogLevel.DEBUG, TAG, "booting");

            context.startService(new Intent(context, TrackerService.class));
        }
    }
}