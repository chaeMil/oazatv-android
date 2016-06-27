package com.chaemil.hgms.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.chaemil.hgms.R;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.receiver.DownloadServiceReceiver;
import com.chaemil.hgms.receiver.DownloadServiceReceiverListener;
import com.chaemil.hgms.utils.NetworkUtils;
import com.chaemil.hgms.utils.SharedPrefUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaemil on 8.1.16.
 */
public class DownloadService extends Service implements ProgressCallback,
        FutureCallback<File>,DownloadServiceReceiverListener {
    private static final int NOTIFICATION_ID = 5000;
    public static final int WAITING = 0;
    public static final int DOWNLOADING = 1;
    public static final int FINISHED = 2;

    public static final String DOWNLOAD_COMPLETE = "downloadComplete";
    public static final String DOWNLOAD_STARTED = "downloadStarted";
    public static final String OPEN_DOWNLOADS = "openDownloads";
    public static final String KILL_DOWNLOAD = "killDownload";

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    private Intent openDownloads;
    private PendingIntent pOpenDownloads;
    private Intent killDownload;
    private PendingIntent pKillDownload;
    private static List<Video> downloadQueue = new ArrayList<>();
    private Video currentDownload;
    private long percentDownloaded;
    private boolean isDownloadingNow;
    private int currentDownloadState = WAITING;
    private Handler notificationHandler;
    private Future<File> currentIonDownload;
    private DownloadServiceReceiver receiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (getFirstVideoToDownload() != null) {
            startDownload();
        } else {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    private void setupReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.DOWNLOAD_COMPLETE);
        filter.addAction(DownloadService.DOWNLOAD_STARTED);
        filter.addAction(DownloadService.OPEN_DOWNLOADS);
        filter.addAction(DownloadService.KILL_DOWNLOAD);

        receiver = new DownloadServiceReceiver(this);
        registerReceiver(receiver, filter);
    }

    private static void updateDownloadQueue() {
        downloadQueue =  Video.getDownloadQueue();
    }

    public boolean isDownloadingNow() {
        return isDownloadingNow;
    }

    private Video getFirstVideoToDownload() {
        updateDownloadQueue();
        if (downloadQueue.size() > 0) {
            return downloadQueue.get(0);
        } else {
            return null;
        }
    }

    private boolean canStartDownload() {
        SharedPrefUtils prefUtils = SharedPrefUtils.getInstance(this);

        boolean downloadOnWifi = prefUtils.loadDownloadOnWifi();
        if (downloadOnWifi && NetworkUtils.isConnectedWithWifi(this)) {
            return true;
        }

        if (!downloadOnWifi && NetworkUtils.isConnected(this)) {
            return true;
        }

        if (!NetworkUtils.isConnected(this)) {
            return false;
        }

        return false;
    }

    private void startDownload() {
        currentDownload = getFirstVideoToDownload();

        Intent i = new Intent(DOWNLOAD_STARTED);
        sendBroadcast(i);

        if (!isDownloadingNow() && canStartDownload()) {
            if (currentDownload != null) {
                isDownloadingNow = true;
                currentDownloadState = WAITING;

                createNotification(currentDownload.getName());

                downloadThumb(currentDownload);
                currentIonDownload = downloadAudio(currentDownload);
            }
        }
    }

    private Future<File> downloadThumb(Video video) {
        return Ion.with(this)
                .load(video.getThumbFile())
                .write(new File(getExternalFilesDir(null), video.getHash() + ".jpg"))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File result) {
                        if (e == null && result != null) {
                            currentDownloadState += 1;
                        }
                    }
                });
    }

    private Future<File> downloadAudio(Video video) {
        return Ion.with(this)
                .load(video.getAudioFile())
                .progress(this)
                .write(new File(getExternalFilesDir(null), video.getHash() + ".mp3"))
                .setCallback(this);
    }

    @Override
    public void onProgress(long downloaded, long total) {
        percentDownloaded = (long) ((float) downloaded / total * 100);

        if (notificationHandler == null) {
            notificationHandler = new Handler(Looper.getMainLooper());
            notificationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateNotificationPercent(percentDownloaded);
                    if (notificationHandler != null) {
                        notificationHandler.postDelayed(this, 500);
                    }
                }
            }, 500);
        }

    }

    @Override
    public void onCompleted(Exception e, File result) {
        if (e != null) {
            e.printStackTrace();
            if (e instanceof SocketException) {
                Video corruptedDownload = Video.findByServerId(currentDownload.getServerId());
                if (corruptedDownload != null) {
                    corruptedDownload.setDownloaded(false);
                }
            }
        }

        if (result != null) {
            SmartLog.Log(SmartLog.LogLevel.DEBUG, "fileDownloaded", result.getAbsolutePath());

            Video downloadedAudio = Video.findByServerId(currentDownload.getServerId());

            if (downloadedAudio != null) {
                currentDownloadState += 1;
                videoDownloaded(currentDownload.getId());
            }
        }

        if (currentDownloadState == FINISHED) {

            currentIonDownload = null;

            if (getFirstVideoToDownload() != null) {
                startDownload();
            } else {
                updateNotificationComplete();
                stopSelf();
            }
        }
    }

    public void updateNotificationPercent(long percentDownloaded) {
        builder.setProgress(100, (int) percentDownloaded, false);
        notificationManager.notify(5000, builder.build());
    }

    private void updateNotificationComplete() {
        cancelNotification();

        builder = new NotificationCompat.Builder(this);
        builder.setProgress(0, 0, false)
                .setSmallIcon(R.drawable.ic_done)
                .setContentIntent(PendingIntent.getActivity(this, 0, openDownloads,
                        PendingIntent.FLAG_CANCEL_CURRENT))
                .setContentText(getString(R.string.download_completed));
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void updateNotificationCanceled() {
        builder = new NotificationCompat.Builder(this);
        builder.setProgress(0, 0, false)
                .setContentText(getString(R.string.download_canceled));
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void cancelNotification() {
        stopForeground(true);
        //notificationManager.cancel(NOTIFICATION_ID);
    }

    public void createNotification(String title) {
        openDownloads = new Intent(OPEN_DOWNLOADS);
        pOpenDownloads = PendingIntent.getBroadcast(this, 0, openDownloads,
                PendingIntent.FLAG_UPDATE_CURRENT);
        killDownload = new Intent(KILL_DOWNLOAD);
        pKillDownload = PendingIntent.getBroadcast(this, 0, killDownload,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(title)
                .setContentText(getResources().getString(R.string.downloading_audio))
                .setProgress(100, 0, false)
                .setAutoCancel(true)
                .setContentIntent(pOpenDownloads)
                .addAction(R.drawable.ic_close, getString(R.string.cancel_download), pKillDownload)
                .setSmallIcon(R.drawable.download);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        startForeground(NOTIFICATION_ID, builder.build());
    }

    public void videoDownloaded(Long id) {
        Video video = Video.findById(Video.class, id);
        video.setInDownloadQueue(false);
        video.setDownloaded(true);
        video.save();
    }

    public void killCurrentDownload() {
        if (currentIonDownload != null) {
            currentIonDownload.cancel();
        }

        startDownload();
    }

    @Override
    public void notifyDownloadFinished() {

    }

    @Override
    public void notifyDownloadStarted() {

    }

    @Override
    public void notifyDownloadKilled() {
        killCurrentDownload();
    }
}
