package com.chaemil.hgms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.fragment.AudioPlayerFragment;
import com.chaemil.hgms.service.DownloadManager;
import com.chaemil.hgms.service.DownloadService;
import com.chaemil.hgms.utils.SmartLog;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by chaemil on 3.6.16.
 */
public class MainActivityReceiver extends BroadcastReceiver {

    private MainActivity mainActivity;

    public MainActivityReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        SmartLog.Log(SmartLog.LogLevel.DEBUG, "onReceive", intent.getAction());

        switch (intent.getAction()) {

            case DownloadManager.DOWNLOAD_COMPLETE:
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.notifyDownloadFinished();
                    }
                }, 1000);
                break;
            case DownloadManager.DOWNLOAD_STARTED:
                mainActivity.notifyDownloadStarted();
                break;
            case DownloadManager.OPEN_DOWNLOADS:
                mainActivity.bringToFront();
                if (mainActivity.getPanelLayout().getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                    mainActivity.collapsePanel();
                }
                if (mainActivity.getMainFragment().isAlbumOpened()) {
                    mainActivity.getMainFragment().closeAlbum();
                }
                mainActivity.getMainFragment().hideSettings();
                mainActivity.getMainFragment().getSearchView().closeSearch();
                mainActivity.getMainFragment().getPager().setCurrentItem(2);
                break;
            case DownloadManager.KILL_DOWNLOAD:
                /*if (((OazaApp) context.getApplicationContext()).downloadService != null) {
                    ((OazaApp) context.getApplicationContext()).downloadService.killCurrentDownload();
                }*/
                break;

        }


    }
}
