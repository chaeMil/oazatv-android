package com.chaemil.hgms.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.model.Video;

/**
 * Created by chaemil on 8.1.16.
 */
public class DownloadService extends Service {
    private static DownloadService instance = null;
    private final DownloadServiceBind downloadServiceBind = new DownloadServiceBind();
    private Context context;

    public static DownloadService getInstance(Context context) {
        if (instance == null) {
            init(context);
        }
        return instance;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return downloadServiceBind;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static void init(Context context) {
        if (instance == null && context != null) {
            instance = new DownloadService();
            instance.setContext(context);
            instance.createDownloadManager();

            ((OazaApp) context).downloadService = instance;

        }
    }

    private void createDownloadManager() {
        if (context != null) {
            DownloadManager.init(context);
        }
    }

    public void downloadVideo(Video video) {
        DownloadManager.getInstance().addVideoToQueue(video);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public class DownloadServiceBind extends Binder {
        public DownloadService getService(Context context) {
            return getInstance(context);
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
