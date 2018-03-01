package com.chaemil.hgms.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.chaemil.hgms.ui.mobile.activity.BaseActivity;

import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;

/**
 * Created by Michal Mlejnek on 18/12/2017.
 */

public class IntentUtils {

    public static void openBrowser(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }

    public static void openMaps(Context context, String address) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + address));
        context.startActivity(intent);
    }

    public static void shareText(Context context, String sharePopUpTitle, String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, sharePopUpTitle));
    }

    public static void shareLinkWithText(Context context, String sharePopUpTitle, String link, String text) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, text);
        intent.putExtra(Intent.EXTRA_TEXT, link);
        context.startActivity(Intent.createChooser(intent, sharePopUpTitle));
    }
}
