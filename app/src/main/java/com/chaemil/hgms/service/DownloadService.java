package com.chaemil.hgms.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
    private List<Video> downloadQueue;
    private boolean downloadingNow = false;
    private Video currentDownload;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        downloadQueue = getDownloadQueue();
        if (!downloadingNow) {
            if (getDownloadQueueSize(downloadQueue) > 0) {
                currentDownload = getFirstToDownload(downloadQueue);
                startDownload();
            }
        }


        return super.onStartCommand(intent, flags, startId);
    }

    private void startDownload() {
        Ion.with(getApplication())
                .load(currentDownload.getThumbFile())
                .write(new File(getExternalFilesDir(null), currentDownload.getHash() + ".jpg"));

        Ion.with(getApplication())
                .load(currentDownload.getAudioFile())
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        SmartLog.Log(SmartLog.LogLevel.DEBUG, "download", String.valueOf(downloaded + " / " + total));
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

                        videoDownloaded(currentDownload.getId());
                    }
                });
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
