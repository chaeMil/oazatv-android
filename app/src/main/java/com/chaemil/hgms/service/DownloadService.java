package com.chaemil.hgms.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.model.Video;

import java.util.List;

/**
 * Created by chaemil on 8.1.16.
 */
public class DownloadService extends Service {
    public static final String NAME = "DownloadService";
    private static final int NOTIFICATION_ID = 5000;
    public static final String DOWNLOAD_COMPLETE = "downloadComplete";
    public static final String DOWNLOAD_STARTED = "downloadStarted";
    public static final String OPEN_DOWNLOADS = "openDownloads";
    public static final String KILL_DOWNLOAD = "killDownload";

    private static DownloadService instance = null;
    private final DownloadServiceBind downloadServiceBind = new DownloadServiceBind();
    private Intent openDownloads;
    private PendingIntent pOpenDownloads;
    private Intent killDownload;
    private PendingIntent pKillDownload;
    private Thread notificationThread;


    public static DownloadService getInstance() {
        return instance;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return downloadServiceBind;
    }

    @Override
    public boolean onUnbind(Intent intent){


        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return START_STICKY;
    }

    private void init() {
        if (instance == null) {
            instance = this;

            ((OazaApp) getApplication()).downloadService = this;

            openDownloads = new Intent(OPEN_DOWNLOADS);
            pOpenDownloads = PendingIntent.getBroadcast(getApplicationContext(), 0, openDownloads, PendingIntent.FLAG_UPDATE_CURRENT);
            killDownload = new Intent(KILL_DOWNLOAD);
            pKillDownload = PendingIntent.getBroadcast(this, 0, killDownload, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    public class DownloadServiceBind extends Binder {
        public DownloadService getService() {
            return getInstance();
        }
    }

    /*private void updateNotificationPercent(double downloaded, double total) {
        percentDownloaded = (long) ((float) downloaded / total * 100);
        builder.setProgress(100, (int) percentDownloaded, false);
        notificationManager.notify(5000, builder.build());
    }

    private void updateNotificationComplete() {
        builder.setProgress(0, 0, false);
        builder.setContentText(getString(R.string.download_completed));
        builder.mActions.clear();
        builder.setOngoing(false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void updateNotificationCanceled() {
        builder.setProgress(0, 0, false);
        builder.setContentText(getString(R.string.download_canceled));
        builder.mActions.clear();
        builder.setOngoing(false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotification() {
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(currentDownload.getName())
                .setContentText(getResources().getString(R.string.downloading_audio))
                .setProgress(100, 0, false)
                .setOngoing(true)
                .setContentIntent(pOpenDownloads)
                .addAction(R.drawable.ic_close, getString(R.string.cancel_download), pKillDownload)
                .setSmallIcon(R.drawable.download);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void videoDownloaded(Long id) {
        Video video = Video.findById(Video.class, id);
        video.setInDownloadQueue(false);
        video.setDownloaded(true);
        video.save();
    }

    public void killCurrentDownload() {
        canceled = true;
        if (currentIonDownload != null) {
            currentIonDownload.cancel();
        }
    }

    public Video getCurrentDownload() {
        return currentDownload;
    }

    public long getCurrentDownloadProgress() {
        return percentDownloaded;
    }*/
}
