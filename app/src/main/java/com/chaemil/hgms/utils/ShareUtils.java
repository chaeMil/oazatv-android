package com.chaemil.hgms.utils;

import android.content.Context;
import android.content.Intent;

/**
 * Created by chaemil on 16.4.16.
 */
public class ShareUtils {

    public static void shareLink(Context context, String link, String subject, String shareMessage) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        share.putExtra(Intent.EXTRA_SUBJECT, subject);
        share.putExtra(Intent.EXTRA_TEXT, link);

        context.startActivity(Intent.createChooser(share, shareMessage));
    }
}
