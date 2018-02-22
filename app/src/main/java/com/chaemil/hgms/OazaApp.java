package com.chaemil.hgms;

import android.content.ContextWrapper;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.chaemil.hgms.ui.mobile.activity.MainActivity;
import com.chaemil.hgms.ui.universal.activity.SplashActivity;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.AudioPlaybackService;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.ServiceUtils;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.koushikdutta.ion.Ion;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.orm.SugarApp;
import com.pixplicity.easyprefs.library.Prefs;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by chaemil on 3.12.15.
 */
public class OazaApp extends SugarApp {

    public static final boolean DEVELOPMENT = BuildConfig.DEBUG;
    public static final boolean TRACKER = true;

    private MainActivity mainActivity;
    public SplashActivity splashActivity;
    public boolean appVisible = false;
    private Tracker mTracker;
    public AudioPlaybackService playbackService;

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());
        AnalyticsService.init(this);
        MultiDex.install(this);
        RequestService.init(this);
        initLogger();
        initIonNetworking();
        initPrefs();
        initCalligraphy();
        initAudioPlaybackService();
    }

    private void initIonNetworking() {
        Ion.getDefault(this).getConscryptMiddleware().enable(false);
    }

    private void initCalligraphy() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(getString(R.string.default_font))
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    private void initPrefs() {
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

    private void initLogger() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .methodCount(0)
                .tag(getString(R.string.app_name))
                .showThreadInfo(false)
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
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
