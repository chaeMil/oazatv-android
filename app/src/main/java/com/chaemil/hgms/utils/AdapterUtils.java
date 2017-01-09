package com.chaemil.hgms.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.receiver.AudioPlaybackReceiver;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.ion.Ion;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.downloadmanager.notifications.NotificationVisibility;

import mehdi.sakout.fancybuttons.FancyButton;
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
                                     final Video video, boolean continueWatching) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        final MaterialDialog dialog = builder
                .customView(R.layout.video_context_menu, true)
                .build();

        boolean isAudioDownloaded = video.isAudioDownloaded(context);

        View dialogView = dialog.getCustomView();
        if (dialogView != null) {
            dialogView.setPadding(0, 0, 0, 0);

            ImageView thumb = (ImageView) dialog.findViewById(R.id.thumb);
            FancyButton downloadAudio = (FancyButton) dialog.findViewById(R.id.download_audio);
            FancyButton streamAudio = (FancyButton) dialog.findViewById(R.id.stream_audio);
            FancyButton shareVideo = (FancyButton) dialog.findViewById(R.id.share_video);
            FancyButton hideVideo = (FancyButton) dialog.findViewById(R.id.hide_video);
            TextView name = (TextView) dialog.findViewById(R.id.name);
            TextView date = (TextView) dialog.findViewById(R.id.date);
            TextView views = (TextView) dialog.findViewById(R.id.views);

            Ion.with(context).load(video.getThumbFile()).intoImageView(thumb);

            name.setText(video.getName());
            date.setText(StringUtils.formatDate(video.getDate(), context));
            views.setText(video.getViews() + " " + context.getString(R.string.views));

            downloadAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    downloadAudio(context, mainActivity, video);
                    dialog.dismiss();
                }
            });

            streamAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainActivity.playNewAudio(video);
                    dialog.dismiss();
                }
            });

            shareVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShareUtils.shareVideoLink(mainActivity, video);
                    dialog.dismiss();
                }
            });

            hideVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPrefUtils.getInstance(context).addHiddenVideo(context, video.getHash());
                    mainActivity.getHomeFragment().refreshContinueWatching();
                    dialog.dismiss();
                }
            });

            if (isAudioDownloaded) {
                downloadAudio.setVisibility(View.INVISIBLE);
                downloadAudio.setEnabled(false);
            }
            if (!continueWatching) {
                hideVideo.setVisibility(View.INVISIBLE);
                hideVideo.setEnabled(false);
            }
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

                    @Override
                    public void permissionForeverDenied() {
                        SuperToast.create(context,
                                context.getString(R.string.permission_revoked_download_photos_and_audio),
                                SuperToast.Duration.LONG).show();
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
