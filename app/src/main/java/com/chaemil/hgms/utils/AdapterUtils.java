package com.chaemil.hgms.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.model.Download;
import com.chaemil.hgms.ui.mobile.activity.MainActivity;
import com.chaemil.hgms.model.Video;
import com.github.clans.fab.FloatingActionButton;
import com.koushikdutta.ion.Ion;
import com.novoda.downloadmanager.Batch;
import com.novoda.downloadmanager.DownloadBatchIdCreator;
import com.novoda.downloadmanager.DownloadFileIdCreator;
import com.novoda.downloadmanager.DownloadManager;
import com.novoda.downloadmanager.DownloadManagerBuilder;

import mehdi.sakout.fancybuttons.FancyButton;
import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;

import static com.chaemil.hgms.ui.mobile.fragment.DownloadedFragment.DOWNLOAD_MANAGER_ONCHANGE;

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

        Video savedVideo = Video.findByServerId(video.getServerId());

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
            if (savedVideo != null) {
                viewProgress.setProgress(savedVideo.getCurrentTime() / 1000);
            }
            deleteFab.setVisibility(video.isAudioDownloaded(context) ? View.VISIBLE : View.GONE);

            downloadAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                    downloadAudio(context, video);
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

    public static void downloadAudio(final Context context, final Video audio) {
        audio.save();

        Toast.makeText(context, context.getString(R.string.added_to_download_queue),
                Toast.LENGTH_SHORT).show();

        DownloadManager downloadManager = OazaApp.downloadManager;

        Download download = new Download(audio.getServerId(), audio.getHash() + ".mp3",
                android.app.DownloadManager.STATUS_PENDING, DownloadBatchIdCreator.createSanitizedFrom(audio.getHash()).rawId());
        download.save();
        context.sendBroadcast(new Intent(DOWNLOAD_MANAGER_ONCHANGE));

        Batch batch = Batch.with(() -> context.getExternalFilesDir("").getAbsolutePath(),
                DownloadBatchIdCreator.createSanitizedFrom(audio.getHash()),
                audio.getName())
                .downloadFrom(audio.getAudioFile())
                .saveTo("", audio.getHash() + ".mp3")
                .withIdentifier(DownloadFileIdCreator.createFrom(audio.getHash())).apply()
                .build();

        downloadManager.download(batch);
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
                .onPositive((dialog1, which) -> {

                    OazaApp.downloadManager.delete(DownloadBatchIdCreator.createSanitizedFrom(video.getHash()));

                    AdapterUtils.deleteAudio(context, video, dialog1);
                    MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                    mainActivity.dismissContextDialog();
                })
                .onNegative((dialog12, which) -> dialog12.dismiss()).build();

        return dialog;
    }
}
