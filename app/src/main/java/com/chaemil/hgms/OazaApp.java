package com.chaemil.hgms;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.activity.SplashActivity;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.AudioPlaybackService;
import com.chaemil.hgms.service.DownloadService;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.Constants;
import com.crashlytics.android.Crashlytics;
import com.github.pedrovgs.lynx.LynxShakeDetector;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.orm.SugarApp;

import io.fabric.sdk.android.Fabric;

/**
 * Created by chaemil on 3.12.15.
 */
public class OazaApp extends SugarApp {

    public static final boolean DEVELOPMENT = true;

    private boolean downloadingNow = false;
    private DownloadService downloadService;
    private MainActivity mainActivity;
    public SplashActivity splashActivity;
    public boolean appVisible = false;
    private Tracker mTracker;
    public AudioPlaybackService playbackService;

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEVELOPMENT) {
            LynxShakeDetector lynxShakeDetector = new LynxShakeDetector(this);
            lynxShakeDetector.init();
        }

        Fabric.with(this, new Crashlytics());
        AnalyticsService.init(this);
        MultiDex.install(this);
        MyRequestService.init(this);

        if (isMyServiceRunning(AudioPlaybackService.class)) {
            if (AudioPlaybackService.getInstance() != null) {
                playbackService = AudioPlaybackService.getInstance();
            }
        }
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

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
