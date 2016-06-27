package com.chaemil.hgms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.service.DownloadService;
import com.chaemil.hgms.utils.SmartLog;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by chaemil on 3.6.16.
 */
public class DownloadServiceReceiver extends BroadcastReceiver {

    private final DownloadServiceReceiverListener listener;

    public DownloadServiceReceiver(DownloadServiceReceiverListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        SmartLog.Log(SmartLog.LogLevel.DEBUG, "onReceive", intent.getAction());

        switch (intent.getAction()) {

            case DownloadService.DOWNLOAD_COMPLETE:
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listener.notifyDownloadFinished();
                    }
                }, 1000);
                break;
            case DownloadService.DOWNLOAD_STARTED:
                listener.notifyDownloadStarted();
                break;
            case DownloadService.OPEN_DOWNLOADS:
                if (listener instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) listener;
                    mainActivity.bringToFront();
                    if (mainActivity.getPanelLayout().getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                        mainActivity.collapsePanel();
                    }
                    if (mainActivity.getMainFragment().isAlbumOpened()) {
                        mainActivity.getMainFragment().closeAlbum();
                    }
                    mainActivity.getMainFragment().hideSettings();
                    mainActivity.getMainFragment().getSearchView().closeSearch();
                    mainActivity.getMainFragment().getPager().setCurrentItem(3);
                }
                break;
            case DownloadService.KILL_DOWNLOAD:
                listener.notifyDownloadKilled();
                break;

        }


    }
}
