package com.chaemil.hgms.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.chaemil.hgms.R;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.SmartLog;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;
import java.util.List;

/**
 * Created by chaemil on 8.1.16.
 */
public class DownloadService extends Service {
    private static final int NOTIFICATION_ID = 5000;
    private List<Video> downloadQueue;
    private boolean downloadingNow = false;
    private Video currentDownload;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private long percentDownloaded;
    private Thread notificationThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class MyBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();

        return START_STICKY;
    }

    private void init() {

        notificationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(downloadingNow) {
                    updateNotificationPercent(percentDownloaded, 100);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        downloadQueue = getDownloadQueue();
        if (!downloadingNow) {
            if (getDownloadQueueSize(downloadQueue) > 0) {
                currentDownload = getFirstToDownload(downloadQueue);
                startDownload();
            }
        }
    }

    public void notifyQueueUpdated() {
        init();
    }

    private void startDownload() {
        createNotification();

        notificationThread.start();

        downloadingNow = true;

        Ion.with(getApplication())
                .load(currentDownload.getThumbFile())
                .write(new File(getExternalFilesDir(null), currentDownload.getHash() + ".jpg"));

        Ion.with(getApplication())
                .load(currentDownload.getAudioFile())
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        percentDownloaded = (long) ((float) downloaded / total * 100);
                        SmartLog.Log(SmartLog.LogLevel.DEBUG, "download", String.valueOf(percentDownloaded));
                    }
                })
                .write(new File(getExternalFilesDir(null), currentDownload.getHash() + ".mp3"))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File file) {
                        if (e != null) {
                            e.printStackTrace();
                        }

                        if (file != null) {
                            SmartLog.Log(SmartLog.LogLevel.DEBUG, "fileDownloaded", file.getAbsolutePath());
                        }

                        notificationThread.interrupt();

                        videoDownloaded(currentDownload.getId());
                        updateNotificationComplete();
                        downloadingNow = false;

                        init();
                    }
                });
    }

    private void updateNotificationPercent(double downloaded, double total) {
        percentDownloaded = (long) ((float) downloaded / total * 100);
        builder.setProgress(100, (int) percentDownloaded, false);
        notificationManager.notify(5000, builder.build());
    }

    private void updateNotificationComplete() {
        builder.setProgress(0, 0, false);
        builder.setContentText(getString(R.string.download_completed));
        builder.setOngoing(false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotification() {
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(currentDownload.getName())
                .setContentText(getResources().getString(R.string.downloading_audio))
                .setProgress(100, 0, false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.download);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private List<Video> getDownloadQueue() {
        return Video.getDownloadQueue();
    }

    private void videoDownloaded(Long id) {
        Video video = Video.findById(Video.class, id);
        video.setInDownloadQueue(false);
        video.setDownloaded(true);
        video.save();
    }

    private int getDownloadQueueSize(List<Video> downloadQueue) {
        return downloadQueue.size();
    }

    private Video getFirstToDownload(List<Video> downloadQueue) {
        if (getDownloadQueueSize(downloadQueue) > 0) {
            return downloadQueue.get(0);
        } else {
            return null;
        }
    }


}
