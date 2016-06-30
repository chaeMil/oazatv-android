package com.chaemil.hgms.model;

import com.novoda.downloadmanager.lib.DownloadManager;

/**
 * Created by chaemil on 30.6.16.
 */
public class Download {
    private final int downloadStatus;
    private final long batchId;
    private final long videoId;

    public Download(long videoId, int downloadStatus, long batchId) {
        this.videoId = videoId;
        this.downloadStatus = downloadStatus;
        this.batchId = batchId;
    }

    public long getVideoId() {
        return videoId;
    }

    public String getDownloadStatusText() {
        switch (downloadStatus) {
            case DownloadManager.STATUS_RUNNING:
                return "Downloading";
            case DownloadManager.STATUS_SUCCESSFUL:
                return "Complete";
            case DownloadManager.STATUS_FAILED:
                return "Failed";
            case DownloadManager.STATUS_PENDING:
                return "Queued";
            case DownloadManager.STATUS_PAUSED:
                return "Paused";
            case DownloadManager.STATUS_DELETING:
                return "Deleting";
            default:
                return "WTH";
        }
    }

    public long getBatchId() {
        return batchId;
    }

    public boolean isPaused() {
        return downloadStatus == DownloadManager.STATUS_PAUSED;
    }

}
