package com.chaemil.hgms.model;

import com.novoda.downloadmanager.lib.DownloadManager;

/**
 * Created by chaemil on 30.6.16.
 */
public class Download {
    private final int downloadStatus;
    private final long batchId;
    private final long videoId;
    private final String filename;

    public Download(long videoId, String filename, int downloadStatus, long batchId) {
        this.videoId = videoId;
        this.filename = filename;
        this.downloadStatus = downloadStatus;
        this.batchId = batchId;
    }

    public String getFilename() {
        return filename;
    }

    public long getVideoServerId() {
        return videoId;
    }

    public int getDownloadStatusText() {
        return downloadStatus;
    }

    public long getBatchId() {
        return batchId;
    }

    public boolean isPaused() {
        return downloadStatus == DownloadManager.STATUS_PAUSED;
    }

}
