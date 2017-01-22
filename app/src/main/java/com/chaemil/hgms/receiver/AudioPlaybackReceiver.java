package com.chaemil.hgms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.service.AudioPlaybackService;
import com.chaemil.hgms.utils.SmartLog;

import java.util.Objects;

/**
 * Created by chaemil on 3.6.16.
 */
public class AudioPlaybackReceiver extends BroadcastReceiver {

    private final PlaybackReceiverListener listener;
    private final OazaApp app;

    public static final String NOTIFY_PLAY_PAUSE = "notify_play_pause";
    public static final String NOTIFY_PAUSE = "notify_pause";
    public static final String NOTIFY_REW = "notify_rew";
    public static final String NOTIFY_FF = "notify_ff";
    public static final String NOTIFY_OPEN = "notify_open";
    public static final String NOTIFY_DELETE = "notify_delete";

    public AudioPlaybackReceiver(PlaybackReceiverListener listener, OazaApp app) {
        this.listener = listener;
        this.app = app;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        SmartLog.Log(SmartLog.LogLevel.DEBUG, "onReceive", intent.getAction());
        switch (intent.getAction()) {

            case NOTIFY_PLAY_PAUSE:
                if (listener != null) {
                    listener.playbackPlayPauseAudio();
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
                    listener.playbackSeekFF();
                }
                break;
            case NOTIFY_REW:
                if (listener != null) {
                    listener.playbackSeekREW();
                }
                break;
            case NOTIFY_DELETE:
                if (app != null && app.getMainActivity() != null) {
                    app.getMainActivity().hidePanel();
                    app.getMainActivity().playAudioIntent = null;
                    app.stopService(new Intent(app, AudioPlaybackService.class));
                    app.getMainActivity().setAudioPlayerFragment(null);
                }
                listener.playbackStop();
                break;
            case AudioPlaybackService.BUFFERING_START:
                if (app != null
                        && app.getMainActivity() != null
                        && app.getMainActivity().getAudioPlayerFragment() != null) {
                    app.getMainActivity().getAudioPlayerFragment().bufferingStart();
                }
                break;
            case AudioPlaybackService.BUFFERING_END:
                if (app != null
                        && app.getMainActivity() != null
                        && app.getMainActivity().getAudioPlayerFragment() != null) {
                    app.getMainActivity().getAudioPlayerFragment().bufferingEnd();
                }
            case AudioPlaybackReceiver.NOTIFY_PAUSE:
                if (listener != null) {
                    listener.playbackPauseAudio();
                }
                break;
        }
    }
}
