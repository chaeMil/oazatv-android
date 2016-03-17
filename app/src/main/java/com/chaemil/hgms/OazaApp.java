package com.chaemil.hgms;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.DownloadService;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.SmartLog;
import com.github.johnpersano.supertoasts.SuperToast;
import com.orm.SugarContext;

/**
 * Created by chaemil on 3.12.15.
 */
public class OazaApp extends Application {

    private boolean downloadingNow = false;
    private DownloadService downloadService;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        MyRequestService.init(this);
        SugarContext.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d("", "onTerminate");
    }

    public boolean isDownloadingNow() {
        return downloadingNow;
    }

    public void setDownloadingNow(boolean downloadingNow) {
        this.downloadingNow = downloadingNow;
    }

    public void addToDownloadQueue(Video video) {
        Video savedVideo = Video.findByServerId(video.getServerId());

        if (savedVideo == null) {
            savedVideo = video;
        }

        savedVideo.setInDownloadQueue(true);
        savedVideo.setDownloaded(false);
        savedVideo.save();
    }

    public void setDownloadService(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    public DownloadService getDownloadService() {
        return downloadService;
    }
}
