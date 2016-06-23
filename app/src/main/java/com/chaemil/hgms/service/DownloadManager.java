package com.chaemil.hgms.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;

import com.chaemil.hgms.R;
import com.chaemil.hgms.model.Video;
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
 * Created by chaemil on 23.6.16.
 */
public class DownloadManager implements ProgressCallback, FutureCallback<File> {

    public static final int WAITING = 0;
    public static final int DOWNLOADING = 1;
    public static final int FINISHED = 2;

    private static final int NOTIFICATION_ID = 5000;

    public static final String DOWNLOAD_COMPLETE = "downloadComplete";
    public static final String DOWNLOAD_STARTED = "downloadStarted";
    public static final String OPEN_DOWNLOADS = "openDownloads";
    public static final String KILL_DOWNLOAD = "killDownload";

    private static DownloadManager instance = null;
    private static List<Video> downloadQueue = new ArrayList<>();
    private static ConnectivityManager connManager;
    private static NetworkInfo wifi;
    private Video currentDownload;
    private Context context;
    private long percentDownloaded;
    private boolean isDownloadingNow;
    private int currentDownloadState = WAITING;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private Intent openDownloads;
    private PendingIntent pOpenDownloads;
    private Intent killDownload;
    private PendingIntent pKillDownload;
    private Handler notificationHandler;

    private DownloadManager(Context context) {
        this.context = context;
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new DownloadManager(context);
            connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            updateDownloadQueue();
        }
    }

    public static DownloadManager getInstance() {
        return instance;
    }

    private static void updateDownloadQueue() {
        downloadQueue =  Video.getDownloadQueue();
    }

    public boolean isDownloadingNow() {
        return isDownloadingNow;
    }

    public void addVideoToQueue(Video video) {
        if (!downloadQueue.contains(video)) {
            downloadQueue.add(video);
        }

        startDownload();
    }

    public void removeVideoFromQueue(Video video) {
        if (downloadQueue.contains(video)) {
            downloadQueue.remove(video);
        }
    }

    private Video getFirstVideoToDownload() {
        if (downloadQueue.size() > 0) {
            return downloadQueue.get(0);
        } else {
            return null;
        }
    }

    private boolean canStartDownload() {
        SharedPrefUtils prefUtils = SharedPrefUtils.getInstance(context);

        boolean downloadOnWifi = prefUtils.loadDownloadOnWifi();
        if (downloadOnWifi && wifi.isConnected() && NetworkUtils.isConnected(context)) {
            return true;
        }

        if (!downloadOnWifi && NetworkUtils.isConnected(context)) {
            return true;
        }

        if (!NetworkUtils.isConnected(context)) {
            return false;
        }

        return false;
    }

    private void startDownload() {
        currentDownload = getFirstVideoToDownload();

        Intent i = new Intent(DOWNLOAD_STARTED);
        context.sendBroadcast(i);

        if (!isDownloadingNow() && canStartDownload()) {
            if (currentDownload != null) {
                isDownloadingNow = true;
                currentDownloadState = WAITING;

                createNotification();

                downloadThumb(currentDownload);
                downloadAudio(currentDownload);
            }
        }
    }

    private Future<File> downloadThumb(Video video) {
        return Ion.with(context)
                .load(video.getThumbFile())
                .write(new File(context.getExternalFilesDir(null), video.getHash() + ".jpg"))
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
        return Ion.with(context)
                .load(video.getAudioFile())
                .progress(this)
                .write(new File(context.getExternalFilesDir(null), video.getHash() + ".mp3"))
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
                videoDownloaded(currentDownload);
            }
        }

        if (currentDownloadState == FINISHED) {
            startDownload();
        }
    }

    private void createNotification() {
        openDownloads = new Intent(OPEN_DOWNLOADS);
        pOpenDownloads = PendingIntent.getBroadcast(context, 0, openDownloads, PendingIntent.FLAG_UPDATE_CURRENT);
        killDownload = new Intent(KILL_DOWNLOAD);
        pKillDownload = PendingIntent.getBroadcast(context, 0, killDownload, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(currentDownload.getName())
                .setContentText(context.getResources().getString(R.string.downloading_audio))
                .setProgress(100, 0, false)
                .setOngoing(true)
                .setContentIntent(pOpenDownloads)
                .addAction(R.drawable.ic_close, context.getString(R.string.cancel_download), pKillDownload)
                .setSmallIcon(R.drawable.download);

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void updateNotificationPercent(double percentDownloaded) {
        notificationBuilder.setProgress(100, (int) percentDownloaded, false);
        notificationManager.notify(5000, notificationBuilder.build());
    }

    private void updateNotificationComplete() {
        notificationBuilder.setProgress(0, 0, false);
        notificationBuilder.setContentText(context.getString(R.string.download_completed));
        notificationBuilder.mActions.clear();
        notificationBuilder.setOngoing(false);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void updateNotificationCanceled() {
        notificationBuilder.setProgress(0, 0, false);
        notificationBuilder.setContentText(context.getString(R.string.download_canceled));
        notificationBuilder.mActions.clear();
        notificationBuilder.setOngoing(false);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void videoDownloaded(Video video) {
        Intent i = new Intent(DOWNLOAD_COMPLETE);
        context.sendBroadcast(i);

        notificationHandler = null;

        video.setInDownloadQueue(false);
        video.setDownloaded(true);
        video.save();

        updateNotificationComplete();
    }
}
