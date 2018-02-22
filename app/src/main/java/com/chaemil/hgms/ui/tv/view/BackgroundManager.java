package com.chaemil.hgms.ui.tv.view;

/**
 * Created by macbook on 22/02/2018.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.koushikdutta.ion.Ion;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Marcus Gabilheri (gabilher)
 * @since 7/21/16
 */

/**
 * NOTE: >> DO NOT USE << images with transparency on then. The BackgroundManager freaks out and a really weird
 * stuff happens with the cards.
 */
public class BackgroundManager {

    private static final String TAG = BackgroundManager.class.getSimpleName();
    private static final int BACKGROUND_UPDATE_DELAY = 200;

    private WeakReference<Activity> mActivityWeakReference;
    private android.support.v17.leanback.app.BackgroundManager mBackgroundManager;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private String mBackgroundURI;
    private Timer mBackgroundTimer;

    public static BackgroundManager instance;

    /**
     * @param activity
     *      The activity to which this WindowManager is attached
     */
    public BackgroundManager(Activity activity) {
        mActivityWeakReference = new WeakReference<>(activity);
        mBackgroundManager = android.support.v17.leanback.app.BackgroundManager.getInstance(activity);
        mBackgroundManager.attach(activity.getWindow());
    }

    public void loadImage(String imageUrl) {
        mBackgroundURI = imageUrl;
        startBackgroundTimer();
    }

    public void setBackground(Drawable drawable) {
        if (mBackgroundManager != null) {
            if (!mBackgroundManager.isAttached()) {
                mBackgroundManager.attach(mActivityWeakReference.get().getWindow());
            }
            mBackgroundManager.setDrawable(drawable);
        }
    }

    private class UpdateBackgroundTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(() -> {
                if (mBackgroundURI != null) {
                    updateBackground();
                }
            });
        }
    }

    /**
     * Cancels an ongoing background change
     */
    public void cancelBackgroundChange() {
        mBackgroundURI = null;
        cancelTimer();
    }

    /**
     * Stops the timer
     */
    private void cancelTimer() {
        if (mBackgroundTimer != null) {
            mBackgroundTimer.cancel();
        }
    }

    /**
     * Starts the background change timer
     */
    private void startBackgroundTimer() {
        cancelTimer();
        mBackgroundTimer = new Timer();
        /* set delay time to reduce too much background image loading process */
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    /**
     * Updates the background with the last known URI
     */
    public void updateBackground() {
        if (mActivityWeakReference.get() != null) {
            Ion.with(mActivityWeakReference.get())
                    .load(mBackgroundURI)
                    .asBitmap()
                    .setCallback((e, result) -> {
                        if (result != null) {
                            Drawable background =
                                    new BitmapDrawable(mActivityWeakReference.get().getResources(), result);
                            setBackground(background);
                        }
                    });
        }
    }

}