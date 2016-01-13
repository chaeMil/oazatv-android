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
import com.github.johnpersano.supertoasts.SuperToast;
import com.orm.SugarContext;

/**
 * Created by chaemil on 3.12.15.
 */
public class OazaApp extends Application {

    private boolean downloadingNow = false;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        MyRequestService.init(this);
        SugarContext.init(this);
    }

    public boolean isDownloadingNow() {
        return downloadingNow;
    }

    public void setDownloadingNow(boolean downloadingNow) {
        this.downloadingNow = downloadingNow;
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

        SuperToast.create(this, getString(R.string.added_to_queue), SuperToast.Duration.SHORT);
    }
}
