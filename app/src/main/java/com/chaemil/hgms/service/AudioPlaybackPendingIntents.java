package com.chaemil.hgms.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.chaemil.hgms.fragment.AudioPlayerFragment;
import com.chaemil.hgms.receiver.AudioPlaybackReceiver;

import java.util.ArrayList;

/**
 * Created by chaemil on 3.6.16.
 */
public class AudioPlaybackPendingIntents {

    public static ArrayList<PendingIntent> generate(Context context) {
        Intent open = new Intent(AudioPlaybackReceiver.NOTIFY_OPEN);
        PendingIntent pOpen = PendingIntent.getBroadcast(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pause = new Intent(AudioPlaybackReceiver.NOTIFY_PLAY_PAUSE);
        PendingIntent pPause = PendingIntent.getBroadcast(context, 0, pause, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent ff = new Intent(AudioPlaybackReceiver.NOTIFY_FF);
        PendingIntent pFf = PendingIntent.getBroadcast(context, 0, ff, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent rew = new Intent(AudioPlaybackReceiver.NOTIFY_REW);
        PendingIntent pRew = PendingIntent.getBroadcast(context, 0, rew, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent delete = new Intent(AudioPlaybackReceiver.NOTIFY_DELETE);
        PendingIntent pDelete = PendingIntent.getBroadcast(context, 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);

        ArrayList intents = new ArrayList();
        intents.add(pOpen);
        intents.add(pPause);
        intents.add(pFf);
        intents.add(pRew);
        intents.add(pDelete);

        return intents;
    }

}
