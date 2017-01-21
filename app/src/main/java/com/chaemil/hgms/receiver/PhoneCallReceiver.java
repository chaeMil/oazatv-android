package com.chaemil.hgms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * Created by chaemil on 1.10.16.
 */

public class PhoneCallReceiver extends BroadcastReceiver {

    public static final String INCOMING_CALL = "incoming_call";

    public PhoneCallReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            Intent incomingCall = new Intent();
            incomingCall.setAction(INCOMING_CALL);
            context.sendBroadcast(incomingCall);

        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
                TelephonyManager.EXTRA_STATE_IDLE)
                || intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
                TelephonyManager.EXTRA_STATE_OFFHOOK)) {
        }
    }
}
