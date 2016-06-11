package com.chaemil.hgms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.fragment.AudioPlayerFragment;
import com.chaemil.hgms.service.AudioPlaybackService;
import com.chaemil.hgms.service.DownloadService;
import com.chaemil.hgms.utils.SmartLog;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by chaemil on 3.6.16.
 */
public class AudioPlaybackReceiver extends BroadcastReceiver {


    private final AudioPlaybackService service;
    private final OazaApp app;

    public static final String NOTIFY_PLAY_PAUSE = "notify_play_pause";
    public static final String NOTIFY_REW = "notify_rew";
    public static final String NOTIFY_FF = "notify_ff";
    public static final String NOTIFY_OPEN = "notify_open";
    public static final String NOTIFY_DELETE = "notify_delete";

    public AudioPlaybackReceiver(AudioPlaybackService service, OazaApp app) {
        this.service = service;
        this.app = app;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        SmartLog.Log(SmartLog.LogLevel.DEBUG, "onReceive", intent.getAction());

        switch (intent.getAction()) {

            case NOTIFY_PLAY_PAUSE:
                if (service != null) {
                    service.playPauseAudio();
                }
                break;
            case NOTIFY_OPEN:

                /*Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                context.sendBroadcast(it);

                mainActivity.bringToFront();
                mainActivity.expandPanel();*/

                break;
            case NOTIFY_FF:
                if (service != null) {
                    service.seekFF();
                }
                break;
            case NOTIFY_REW:
                if (service != null) {
                    service.seekREW();
                }
                break;
            case NOTIFY_DELETE:
                if (app != null && app.getMainActivity() != null) {
                    app.getMainActivity().hidePanel();
                    app.getMainActivity().playAudioIntent = null;
                    app.stopService(new Intent(app, AudioPlaybackService.class));
                }
                service.stop();
                break;

        }


    }
}
