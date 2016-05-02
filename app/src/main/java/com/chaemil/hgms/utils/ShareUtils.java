package com.chaemil.hgms.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.chaemil.hgms.R;
import com.chaemil.hgms.model.Video;

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

    public static void shareVideoLink(Activity activity, Video video) {
        ShareUtils.shareLink(activity, Constants.VIDEO_LINK + video.getHash(),
                video.getNameCS() + " | " + video.getNameEN(),
                activity.getString(R.string.share_video));
    }

    public static void shareAudioLink(Activity activity, Video audio) {
        ShareUtils.shareLink(activity, Constants.AUDIO_LINK + audio.getHash(),
                audio.getNameCS() + " | " + audio.getNameEN(),
                activity.getString(R.string.share_video));
    }
}
