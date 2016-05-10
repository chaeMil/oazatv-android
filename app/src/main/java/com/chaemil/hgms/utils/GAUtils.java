package com.chaemil.hgms.utils;

import com.chaemil.hgms.BuildConfig;
import com.chaemil.hgms.OazaApp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by chaemil on 6.5.16.
 */
public class GAUtils {

    public static Tracker getTracker(OazaApp app) {
        return app.getDefaultTracker();
    }

    private static void setTrackerParams(Tracker tracker) {
        tracker.setAppVersion(String.valueOf(BuildConfig.VERSION_CODE));
        tracker.setLanguage(LocalUtils.getLocale());
    }

    public static void sendGAScreen(OazaApp app, String name) {
        Tracker mTracker = getTracker(app);
        setTrackerParams(mTracker);
        mTracker.setScreenName(name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void sendGAScreen(OazaApp app, String name, String title) {
        Tracker mTracker = getTracker(app);
        setTrackerParams(mTracker);
        mTracker.setScreenName(name);
        mTracker.setTitle(title);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

}
