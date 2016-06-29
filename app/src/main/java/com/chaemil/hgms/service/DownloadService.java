package com.chaemil.hgms.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
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
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.notifications.NotificationVisibility;

import java.io.File;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaemil on 8.1.16.
 */
public class DownloadService extends Service implements DownloadServiceReceiverListener
{
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
    private DownloadManager downloadManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        init();
        setupReceiver();

        if (getFirstVideoToDownload() != null) {
            startDownload();
        } else {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private void init() {
        downloadManager = DownloadManagerBuilder.from(this).build();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
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

                downloadThumb(currentDownload);
                downloadAudio(currentDownload);
            }
        }
    }

    private void downloadThumb(Video video) {
        Uri uri = Uri.parse(video.getThumbFile());
        Request audioDownload = new Request(uri)
                .setDestinationInExternalFilesDir(
                        getExternalFilesDir(null).getAbsolutePath(),
                        video.getHash() + ".jpg")
                .setNotificationVisibility(NotificationVisibility.ACTIVE_OR_COMPLETE)
                .setTitle(getString(R.string.download_audio))
                .setDescription(video.getName())
                .setBigPictureUrl(video.getThumbFile());

        downloadManager.enqueue(audioDownload);
    }

    private void downloadAudio(Video video) {
        Uri uri = Uri.parse(video.getAudioFile());
        Request audioDownload = new Request(uri)
                .setDestinationInExternalFilesDir(
                        getExternalFilesDir(null).getAbsolutePath(),
                        video.getHash() + ".mp3")
                .setNotificationVisibility(NotificationVisibility.ACTIVE_OR_COMPLETE)
                .setTitle(getString(R.string.download_audio))
                .setDescription(video.getName())
                .setBigPictureUrl(video.getThumbFile());

        downloadManager.enqueue(audioDownload);
    }

    /*@Override
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
                stopSelf();
            }
        }
    }*/

    public void videoDownloaded(Long id) {
        Video video = Video.findById(Video.class, id);
        video.setInDownloadQueue(false);
        video.setDownloaded(true);
        video.save();
    }

    public void deleteKilledDownload(Long id) {
        Video.deleteDownloadedAudio(this, Video.findById(Video.class, id));
    }

    public void killCurrentDownload() {
        if (currentIonDownload != null) {
            currentIonDownload.cancel();
            deleteKilledDownload(currentDownload.getId());
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
