package com.chaemil.hgms.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Video;
import com.github.johnpersano.supertoasts.SuperToast;

/**
 * Created by chaemil on 28.3.16.
 */
public class AdapterUtils {

    public static void contextDialog(final Context context, final MainActivity mainActivity, final ArrayAdapter arrayAdapter, final Video video) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String[] menu;

        if (Video.getDownloadStatus(((OazaApp) context.getApplicationContext()), video.getServerId()) == Video.NOT_DOWNLOADED) {
            menu = new String[] {context.getString(R.string.download_audio),
                    context.getString(R.string.stream_audio)};

            builder.setItems(menu, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0:
                            downloadAudio(context, mainActivity, arrayAdapter, video);
                            dialog.dismiss();
                            break;
                        case 1:
                            mainActivity.playNewAudio(video);
                            dialog.dismiss();
                            break;
                    }
                }
            });

        }

        if (Video.getDownloadStatus(((OazaApp) context.getApplicationContext()), video.getServerId()) == Video.DOWNLOADED) {
            menu = new String[] {context.getString(R.string.play_downloaded_audio)};

            builder.setItems(menu, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0:
                            mainActivity.playNewAudio(video);
                            dialog.dismiss();
                            break;
                    }
                }
            });

        }


        builder.create().show();
    }

    public static void downloadAudio(Context context, MainActivity mainActivity, ArrayAdapter arrayAdapter, Video video) {
        ((OazaApp) mainActivity.getApplication()).addToDownloadQueue(video);
        mainActivity.startDownloadService();
        arrayAdapter.notifyDataSetChanged();
        mainActivity.getMainFragment().getDownloadedFragment().notifyDatasetChanged();
        SuperToast.create(context, context.getString(R.string.added_to_download_queue), SuperToast.Duration.MEDIUM).show();
    }
}
