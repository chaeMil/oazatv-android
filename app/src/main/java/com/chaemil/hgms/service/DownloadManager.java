package com.chaemil.hgms.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.NetworkUtils;
import com.chaemil.hgms.utils.ShareUtils;
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
    public static final int FINISHED = 3;

    private static DownloadManager instance = null;
    private static List<Video> downloadQueue = new ArrayList<>();
    private static ConnectivityManager connManager;
    private static NetworkInfo wifi;
    private Video currentDownload;
    private Context context;
    private long percentDownloaded;
    private boolean isDownloadingNow;
    private int currentDownloadState = WAITING;

    private DownloadManager(Context context) {
        this.context = context;
    }

    public static void init(Context context) {
        instance = new DownloadManager(context);
        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        updateDownloadQueue();
    }

    public DownloadManager getInstance() {
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

        if (!isDownloadingNow() && canStartDownload()) {
            if (currentDownload != null) {
                isDownloadingNow = true;
                currentDownloadState = WAITING;

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
                downloadedAudio.setDownloaded(true);
            }
        }

        if (currentDownloadState == FINISHED) {
            startDownload();
        }
    }
}
