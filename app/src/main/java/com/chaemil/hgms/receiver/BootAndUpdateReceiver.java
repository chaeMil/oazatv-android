package com.chaemil.hgms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.service.DownloadService;
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

            Intent startServiceIntent = new Intent(context, DownloadService.class);
            context.startService(startServiceIntent);

            Intent locationIntentService = ((OazaApp) context.getApplicationContext()).getDownloadServiceIntent();
            ServiceConnection downloadServiceConnection = ((OazaApp) context.getApplicationContext()).getDownloadServiceConnection();

            context.getApplicationContext()
                    .bindService(locationIntentService, downloadServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }
}