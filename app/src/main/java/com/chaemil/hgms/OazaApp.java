package com.chaemil.hgms;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.activity.SplashActivity;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.DownloadService;
import com.chaemil.hgms.service.MyRequestService;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import io.fabric.sdk.android.Fabric;

/**
 * Created by chaemil on 3.12.15.
 */
public class OazaApp extends Application {

    private boolean downloadingNow = false;
    private DownloadService downloadService;
    private MainActivity mainActivity;
    public SplashActivity splashActivity;
    public boolean appVisible = false;
    private Tracker mTracker;
    private ServiceConnection observerService;
    private Intent observerIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());
        AnalyticsService.init(this);
        MultiDex.install(this);
        MyRequestService.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d("", "onTerminate");
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker("UA-46402880-6");
        }
        return mTracker;
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

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

}
