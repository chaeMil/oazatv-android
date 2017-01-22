package com.chaemil.hgms.receiver;

import android.content.Context;
import android.content.Intent;

import java.util.Date;

/**
 * Created by chaemil on 22.1.17.
 */

public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        ctx.sendBroadcast(new Intent(AudioPlaybackReceiver.NOTIFY_PAUSE));
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        ctx.sendBroadcast(new Intent(AudioPlaybackReceiver.NOTIFY_PAUSE));
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
    }

}