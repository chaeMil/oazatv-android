package com.chaemil.hgms;

import android.support.multidex.MultiDex;
import android.util.Log;

import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.activity.SplashActivity;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.AudioPlaybackService;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.ServiceUtils;
import com.crashlytics.android.Crashlytics;
import com.github.pedrovgs.lynx.LynxShakeDetector;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.orm.SugarApp;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by chaemil on 3.12.15.
 */
public class OazaApp extends SugarApp {

    public static final boolean DEVELOPMENT = false;
    public static final boolean TRACKER = true;

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
        RequestService.init(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(getString(R.string.default_font))
                .setFontAttrId(R.attr.fontPath)
                .build());

        initAudioPlaybackService();
    }

    private void initAudioPlaybackService() {
        if (ServiceUtils.isMyServiceRunning(this, AudioPlaybackService.class)) {
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

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}
