package com.chaemil.hgms.receiver;

/**
 * Created by chaemil on 27.6.16.
 */
public interface DownloadServiceReceiverListener {
    void notifyDownloadFinished();

    void notifyDownloadStarted();

    void notifyDownloadKilled();
}
