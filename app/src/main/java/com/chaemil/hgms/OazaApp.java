package com.chaemil.hgms;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.multidex.MultiDex;

import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.DownloadService;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.SmartLog;
import com.orm.SugarContext;

/**
 * Created by chaemil on 3.12.15.
 */
public class OazaApp extends Application {

    private Intent downloadServiceIntent;
    private DownloadService downloadService;

    @Override
    public void onCreate() {
        super.onCreate();
        downloadServiceIntent = new Intent(this, DownloadService.class);
        startService(downloadServiceIntent);
        bindService(downloadServiceIntent, downloadServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        MyRequestService.init(this);
        SugarContext.init(this);
    }


    public void addToDownloadQueue(Video video) {
        Video savedVideo = video;

        try {
            savedVideo = Video.findByServerId(video.getServerId());
        } catch (Exception e) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
        }

        if (savedVideo != null) {
            savedVideo.setInDownloadQueue(true);
            savedVideo.save();
        }

        if (downloadService != null) {
            downloadService.notifyQueueUpdated();
        }
    }

    public void restartBackgroundService() {
        downloadServiceIntent = new Intent(this, DownloadService.class);
        stopService(downloadServiceIntent);
        startService(downloadServiceIntent);
    }

    public Intent getDownloadServiceIntent() {
        return downloadServiceIntent;
    }

    public ServiceConnection getDownloadServiceConnection() {
        return downloadServiceConnection;
    }


    private ServiceConnection downloadServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            downloadServiceConnection = (ServiceConnection) ((DownloadService.MyBinder)service).getService();
            downloadService = ((DownloadService.MyBinder) service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            downloadServiceIntent = null;
        }
    };
}
