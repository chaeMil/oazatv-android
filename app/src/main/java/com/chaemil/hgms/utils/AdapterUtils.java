package com.chaemil.hgms.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.ArrayAdapter;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.CategoriesAdapter;
import com.chaemil.hgms.adapter.DownloadedAdapter;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.receiver.AudioPlaybackReceiver;
import com.github.johnpersano.supertoasts.SuperToast;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.downloadmanager.lib.logger.LLog;
import com.novoda.downloadmanager.notifications.NotificationVisibility;

import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;

/**
 * Created by chaemil on 28.3.16.
 */
public class AdapterUtils {

    public static void contextDialog(final Context context, final MainActivity mainActivity,
                                     final Object adapter, final Video video) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String[] menu;
        //int videoStatus = Video.getDownloadStatus(video.getServerId());

        //if (videoStatus == Video.NOT_DOWNLOADED) {
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

        //}

        /*if (videoStatus == Video.DOWNLOADED
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

        }*/


        builder.create().show();
    }

    public static void downloadAudio(final Context context, final MainActivity mainActivity,
                                     final Object arrayAdapter,
                                     final Video audio) {

        mainActivity.askCompactPermission(PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE,
                new PermissionResult() {
                    @Override
                    public void permissionGranted() {

                        audio.save();

                        Uri uri = Uri.parse(audio.getAudioFile());
                        Request request = new Request(uri)
                                .setExtraData(String.valueOf(audio.getServerId()))
                                .setAllowedNetworkTypes(Request.NETWORK_WIFI)
                                .setTitle(context.getString(R.string.downloading_audio))
                                .setDescription(audio.getName())
                                .setBigPictureUrl(audio.getThumbFile())
                                .setDestinationInExternalFilesDir("", audio.getHash() + ".mp3")
                                .setNotificationVisibility(NotificationVisibility.ACTIVE_OR_COMPLETE);

                        if (!SharedPrefUtils.getInstance(context).loadDownloadOnlyOnWifi()) {
                            request.setAllowedNetworkTypes(Request.NETWORK_MOBILE
                                    | Request.NETWORK_WIFI
                                    | Request.NETWORK_BLUETOOTH);
                        }

                        DownloadManager downloadManager = DownloadManagerBuilder.from(context).build();
                        long requestId = downloadManager.enqueue(request);
                        LLog.d("Download enqueued with request ID: " + requestId);

                        //((OazaApp) context.getApplicationContext()).startDownloadService();

                        if (arrayAdapter instanceof ArrayAdapter) {
                            ((ArrayAdapter) arrayAdapter).notifyDataSetChanged();
                        }
                        if (arrayAdapter instanceof CategoriesAdapter) {
                            ((CategoriesAdapter) arrayAdapter).notifyDataSetChanged();
                        }

                        mainActivity.getMainFragment().getDownloadedFragment().notifyDatasetChanged();

                        SuperToast.create(context, context.getString(R.string.added_to_download_queue),
                                SuperToast.Duration.MEDIUM).show();
                    }

                    @Override
                    public void permissionDenied() {
                        SuperToast.create(context, context.getString(R.string.permission_revoked),
                                SuperToast.Duration.MEDIUM).show();
                    }
                });
    }

    public static void deleteAudio(Context context, MainActivity mainActivity, Video audio,
                                   DialogInterface dialog, DownloadedAdapter adapter) {
        if (mainActivity.getAudioPlayerFragment() != null) {
            Video currentlyPlayingAudio = mainActivity.getAudioPlayerFragment()
                    .getCurrentAudio();

            if (currentlyPlayingAudio.equals(audio)) {
                context.sendBroadcast(new Intent(AudioPlaybackReceiver.NOTIFY_DELETE));
            }
        }

        Video.deleteDownloadedAudio(context, audio);
        if (dialog != null) {
            dialog.dismiss();
        }

        adapter.remove(audio);
        mainActivity.notifyDownloadDatasetChanged();
    }
}
