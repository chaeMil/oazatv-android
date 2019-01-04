package com.chaemil.hgms.model;

import android.app.DownloadManager;

import com.orm.SugarRecord;

/**
 * Created by chaemil on 30.6.16.
 */
public class Download extends SugarRecord {
    private int downloadStatus;
    private String batchId;
    private long videoId;
    private String filename;

    public Download() {
    }

    public Download(long videoId, String filename, int downloadStatus, String batchId) {
        this.videoId = videoId;
        this.filename = filename;
        this.downloadStatus = downloadStatus;
        this.batchId = batchId;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public long getVideoId() {
        return videoId;
    }

    public void setVideoId(long videoId) {
        this.videoId = videoId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
