package com.chaemil.hgms.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.receiver.AudioPlaybackReceiver;
import com.github.clans.fab.FloatingActionButton;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.ion.Ion;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Query;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.downloadmanager.notifications.NotificationVisibility;

import java.util.ArrayList;

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

    public static void contextDialog(final Context context,
                                     final Video video, boolean continueWatching) {

        MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();

        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.video_context_menu, true)
                .build();

        mainActivity.setContextDialog(dialog);
        final boolean isAudioDownloaded = video.isAudioDownloaded(context);

        View dialogView = dialog.getCustomView();
        if (dialogView != null) {
            dialogView.setPadding(0, 0, 0, 0);

            ImageView thumb = (ImageView) dialogView.findViewById(R.id.thumb);
            FancyButton downloadAudio = (FancyButton) dialogView.findViewById(R.id.download_audio);
            FancyButton streamAudio = (FancyButton) dialogView.findViewById(R.id.stream_audio);
            FancyButton shareVideo = (FancyButton) dialogView.findViewById(R.id.share_video);
            FancyButton hideVideo = (FancyButton) dialogView.findViewById(R.id.hide_video);
            TextView name = (TextView) dialogView.findViewById(R.id.name);
            TextView date = (TextView) dialogView.findViewById(R.id.date);
            TextView views = (TextView) dialogView.findViewById(R.id.views);
            TextView cc = (TextView) dialog.findViewById(R.id.cc);
            TextView language = (TextView) dialog.findViewById(R.id.language);
            TextView time = (TextView) dialog.findViewById(R.id.video_time);
            ProgressBar viewProgress = (ProgressBar) dialog.findViewById(R.id.view_progress);
            FloatingActionButton playFab = (FloatingActionButton) dialog.findViewById(R.id.play_fab);
            FloatingActionButton deleteFab = (FloatingActionButton) dialog.findViewById(R.id.delete_fab);

            Ion.with(context).load(video.getThumbFile()).intoImageView(thumb);

            name.setText(video.getName());
            date.setText(StringUtils.formatDate(video.getDate(), context));
            views.setText(video.getViews() + " " + context.getString(R.string.views));

            time.setText(StringUtils.getDurationString(video.getDuration()));
            cc.setVisibility(video.getSubtitlesFile() != null ? View.VISIBLE : View.GONE);
            language.setVisibility(video.getVideoLanguage(context) != null ? View.VISIBLE : View.GONE);
            language.setText(video.getVideoLanguage(context));
            viewProgress.setMax(video.getDuration());
            viewProgress.setProgress(video.getCurrentTime() / 1000);
            deleteFab.setVisibility(video.isAudioDownloaded(context) ? View.VISIBLE : View.GONE);

            downloadAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                    downloadAudio(context, mainActivity, video);
                    mainActivity.dismissContextDialog();
                }
            });

            streamAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                    mainActivity.playNewAudio(video);
                    mainActivity.dismissContextDialog();
                }
            });

            shareVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                    ShareUtils.shareVideoLink(mainActivity, video);
                    mainActivity.dismissContextDialog();
                }
            });

            hideVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                    SharedPrefUtils.getInstance(context).addHiddenVideo(context, video.getHash());
                    mainActivity.getHomeFragment().refreshContinueWatching();
                    mainActivity.dismissContextDialog();
                }
            });

            playFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                    mainActivity.playNewVideo(video);
                    mainActivity.dismissContextDialog();
                }
            });

            deleteFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDeleteDialog(context, video).show();
                }
            });

            if (isAudioDownloaded) {
                downloadAudio.setVisibility(View.GONE);
                downloadAudio.setEnabled(false);
            }
            if (!continueWatching) {
                hideVideo.setVisibility(View.GONE);
                hideVideo.setEnabled(false);
            }

            dialog.show();
        }
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

    public static void deleteAudio(Context context, Video audio,
                                   DialogInterface dialog) {
        audio.deleteDownloadedAudio(context);

        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public static MaterialDialog createDeleteDialog(final Context context, final Video video) {

        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(video.getName())
                .theme(Theme.LIGHT)
                .content(context.getString(R.string.delete_downloaded_audio) + "?")
                .positiveText(context.getString(R.string.yes))
                .negativeText(context.getString(R.string.no))
                .positiveColor(context.getResources().getColor(R.color.colorPrimary))
                .negativeColor(context.getResources().getColor(R.color.colorPrimary))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        DownloadManager downloadManager = DownloadManagerBuilder.from(context).build();
                        Cursor cursor = downloadManager.query(new Query().setFilterByExtraData(String.valueOf(video.getServerId())));
                        ArrayList<Integer> idsToDelete = new ArrayList<>();
                        try {
                            while (cursor.moveToNext()) {
                                long videoId = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_EXTRA_DATA));
                                if (videoId == video.getServerId()) {
                                    idsToDelete.add(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BATCH_ID)));
                                }
                            }
                        } finally {
                            cursor.close();
                            for (Integer integer : idsToDelete) {
                                downloadManager.removeBatches(integer);
                            }
                        }

                        AdapterUtils.deleteAudio(context, video, dialog);
                        MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                        mainActivity.dismissContextDialog();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).build();

        return dialog;
    }
}
