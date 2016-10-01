package com.chaemil.hgms.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.receiver.AudioPlaybackReceiver;
import com.github.johnpersano.supertoasts.SuperToast;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.downloadmanager.notifications.NotificationVisibility;

import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;

/**
 * Created by chaemil on 28.3.16.
 */
public class AdapterUtils {

    public static int getArchiveLayout(Context context) {
        if (context.getResources().getBoolean(R.bool.isTablet)) {
            return R.layout.archive_item_big;
        } else {
            return R.layout.archive_item;
        }
    }

    public static void contextDialog(final Context context, final MainActivity mainActivity,
                                     final Video video) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);

        String[] menu;

        boolean isAudioDownloaded = video.isAudioDownloaded(context);

        if (!isAudioDownloaded) {

            menu = new String[]{context.getString(R.string.download_audio),
                    context.getString(R.string.stream_audio),
                    context.getString(R.string.share_video)};

            builder.title(video.getName())
                    .theme(Theme.LIGHT)
                    .items(menu)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            switch(which) {
                                case 0:
                                    downloadAudio(context, mainActivity, video);
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

        if (isAudioDownloaded) {

            menu = new String[] {context.getString(R.string.play_downloaded_audio),
                    context.getString(R.string.share_video)};

            builder.title(video.getName())
                    .theme(Theme.LIGHT)
                    .items(menu)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
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

        builder.show();
    }

    public static void downloadAudio(final Context context, final MainActivity mainActivity,
                                     final Video audio) {

        mainActivity.askCompactPermission(PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE,
                new PermissionResult() {
                    @Override
                    public void permissionGranted() {

                        audio.save();

                        Uri uri = Uri.parse(audio.getAudioFile());
                        Request request = new Request(uri)
                                .setExtraData(String.valueOf(audio.getServerId()))
                                .setTitle(context.getString(R.string.downloading_audio))
                                .setDescription(audio.getName())
                                .setBigPictureUrl(audio.getThumbFile())
                                .setDestinationInExternalFilesDir("", audio.getHash() + ".mp3")
                                .setNotificationVisibility(NotificationVisibility.ACTIVE_OR_COMPLETE);

                        SuperToast.create(context, context.getString(R.string.added_to_download_queue),
                                SuperToast.Duration.MEDIUM).show();

                        DownloadManager downloadManager = DownloadManagerBuilder.from(context).build();
                        downloadManager.enqueue(request);

                    }

                    @Override
                    public void permissionDenied() {
                        SuperToast.create(context, context.getString(R.string.permission_revoked),
                                SuperToast.Duration.MEDIUM).show();
                    }
                });
    }

    public static void deleteAudio(Context context, MainActivity mainActivity, Video audio,
                                   DialogInterface dialog) {
        if (mainActivity.getAudioPlayerFragment() != null) {
            Video currentlyPlayingAudio = mainActivity.getAudioPlayerFragment()
                    .getCurrentAudio();

            if (currentlyPlayingAudio.equals(audio)) {
                context.sendBroadcast(new Intent(AudioPlaybackReceiver.NOTIFY_DELETE));
            }
        }

        audio.deleteDownloadedAudio(context);


        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
