package com.chaemil.hgms.receiver;

import android.app.Activity;
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

    private final ReceiverListener listener;
    private final OazaApp app;

    public static final String NOTIFY_PLAY_PAUSE = "notify_play_pause";
    public static final String NOTIFY_REW = "notify_rew";
    public static final String NOTIFY_FF = "notify_ff";
    public static final String NOTIFY_OPEN = "notify_open";
    public static final String NOTIFY_DELETE = "notify_delete";


    public AudioPlaybackReceiver(ReceiverListener listener, OazaApp app) {
        this.listener = listener;
        this.app = app;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        SmartLog.Log(SmartLog.LogLevel.DEBUG, "onReceive", intent.getAction());

        switch (intent.getAction()) {

            case NOTIFY_PLAY_PAUSE:
                if (listener != null) {
                    listener.playPauseAudio();
                }
                break;
            case NOTIFY_OPEN:

                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                context.sendBroadcast(it);

                MainActivity mainActivity = app.getMainActivity();
                if (mainActivity != null) {
                    mainActivity.bringToFront();
                    mainActivity.expandPanel();
                } else {
                    Intent activity = new Intent(app, MainActivity.class);
                    activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.putExtra(MainActivity.EXPAND_PANEL, true);
                    app.startActivity(activity);
                }

                break;
            case NOTIFY_FF:
                if (listener != null) {
                    listener.seekFF();
                }
                break;
            case NOTIFY_REW:
                if (listener != null) {
                    listener.seekREW();
                }
                break;
            case NOTIFY_DELETE:
                if (app != null && app.getMainActivity() != null) {
                    app.getMainActivity().hidePanel();
                    app.getMainActivity().playAudioIntent = null;
                    app.stopService(new Intent(app, AudioPlaybackService.class));
                }
                listener.stop();
                break;

        }


    }
}
