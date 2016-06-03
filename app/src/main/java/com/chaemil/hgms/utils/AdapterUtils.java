package com.chaemil.hgms.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.CategoriesAdapter;
import com.chaemil.hgms.model.Video;
import com.github.johnpersano.supertoasts.SuperToast;

/**
 * Created by chaemil on 28.3.16.
 */
public class AdapterUtils {

    public static void contextDialog(final Context context, final MainActivity mainActivity,
                                     final Object adapter, final Video video) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String[] menu;
        int videoStatus = Video.getDownloadStatus(((OazaApp) context.getApplicationContext()), video.getServerId());

        if (videoStatus == Video.NOT_DOWNLOADED) {
            menu = new String[] {context.getString(R.string.download_audio),
                    context.getString(R.string.stream_audio),
                    context.getString(R.string.share_video)};

            builder.setItems(menu, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0:
                            downloadAudio(context, mainActivity, adapter, video);
                            dialog.dismiss();
                            break;
                        case 1:
                            mainActivity.playNewAudio(video, true);
                            dialog.dismiss();
                            break;
                        case 2:
                            ShareUtils.shareVideoLink(mainActivity, video);
                            break;
                    }
                }
            });

        }

        if (videoStatus == Video.DOWNLOADED
                || videoStatus == Video.CURRENTLY_DOWNLOADING
                || videoStatus == Video.IN_DOWNLOAD_QUEUE) {
            menu = new String[] {context.getString(R.string.play_downloaded_audio),
                    context.getString(R.string.share_video)};

            builder.setItems(menu, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0:
                            mainActivity.playNewAudio(video, true);
                            dialog.dismiss();
                            break;
                        case 1:
                            ShareUtils.shareVideoLink(mainActivity, video);
                    }
                }
            });

        }


        builder.create().show();
    }

    public static void downloadAudio(Context context, MainActivity mainActivity, Object arrayAdapter, Video video) {
        ((OazaApp) mainActivity.getApplication()).addToDownloadQueue(video);
        mainActivity.startDownloadService();
        if (arrayAdapter instanceof ArrayAdapter) {
            ((ArrayAdapter) arrayAdapter).notifyDataSetChanged();
        }
        if (arrayAdapter instanceof CategoriesAdapter) {
            ((CategoriesAdapter) arrayAdapter).notifyDataSetChanged();
        }
        mainActivity.getMainFragment().getDownloadedFragment().notifyDatasetChanged();
        SuperToast.create(context, context.getString(R.string.added_to_download_queue), SuperToast.Duration.MEDIUM).show();
    }
}
