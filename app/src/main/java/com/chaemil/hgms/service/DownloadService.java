package com.chaemil.hgms.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.NotificationCompat;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.fragment.AudioPlayerFragment;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.FileUtils;
import com.chaemil.hgms.utils.SharedPrefUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;
import java.util.List;

/**
 * Created by chaemil on 8.1.16.
 */
public class DownloadService extends IntentService {
    public static final String NAME = "DownloadService";
    private static final int NOTIFICATION_ID = 5000;
    public static final String DOWNLOAD_COMPLETE = "downloadComplete";
    public static final String DOWNLOAD_STARTED = "downloadStarted";
    public static final String OPEN_DOWNLOADS = "openDownloads";
    public static final String KILL_DOWNLOAD = "killDownload";
    private List<Video> downloadQueue;
    private Video currentDownload;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private long percentDownloaded;
    private Thread notificationThread;
    private Future<File> currentIonDownload;
    private boolean canceled;
    private Intent openDownloads;
    private PendingIntent pOpenDownloads;
    private Intent killDownload;
    private PendingIntent pKillDownload;

    public DownloadService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        init();
    }

    private void init() {

        openDownloads = new Intent(OPEN_DOWNLOADS);
        pOpenDownloads = PendingIntent.getBroadcast(getApplicationContext(), 0, openDownloads,
                PendingIntent.FLAG_UPDATE_CURRENT);

        killDownload = new Intent(KILL_DOWNLOAD);
        pKillDownload = PendingIntent.getBroadcast(this, 0, killDownload, PendingIntent.FLAG_UPDATE_CURRENT);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!((OazaApp) getApplication()).isDownloadingNow()) {
            notificationThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (((OazaApp) getApplication()).isDownloadingNow()) {
                        updateNotificationPercent(percentDownloaded, 100);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            downloadQueue = getDownloadQueue();

            if (getDownloadQueueSize(downloadQueue) > 0) {
                currentDownload = getFirstToDownload(downloadQueue);

                if (FileUtils.getAvailableSpaceInMB() > 100) {
                    if (SharedPrefUtils.getInstance(getApplicationContext()).loadDownloadOnWifi()) {
                        if (wifi.isConnected()) {
                            startDownload();
                        }
                    } else {
                        startDownload();
                    }
                } else {
                    SuperToast.create(getApplicationContext(),
                            getString(R.string.not_enough_space_to_download),
                            SuperToast.Duration.MEDIUM).show();
                }
            }
        }
    }

    private void startDownload() {
        createNotification();

        ((OazaApp) getApplicationContext()).setDownloadService(this);

        Intent i = new Intent(DOWNLOAD_STARTED);
        sendBroadcast(i);

        notificationThread.start();

        ((OazaApp) getApplication()).setDownloadingNow(true);

        Ion.with(getApplication())
                .load(currentDownload.getThumbFile())
                .write(new File(getExternalFilesDir(null), currentDownload.getHash() + ".jpg"));

        currentIonDownload = Ion.with(getApplication())
                .load(currentDownload.getAudioFile())
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        percentDownloaded = (long) ((float) downloaded / total * 100);
                    }
                })
                .write(new File(getExternalFilesDir(null), currentDownload.getHash() + ".mp3"))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File file) {
                        if (e != null) {
                            e.printStackTrace();
                        }

                        if (file != null) {
                            SmartLog.Log(SmartLog.LogLevel.DEBUG, "fileDownloaded", file.getAbsolutePath());
                        }

                        downloadCallback(canceled);
                    }
                });
    }

    private void downloadCallback(boolean canceled) {

        if (canceled) {

            updateNotificationCanceled();
            Video.deleteDownloadedAudio(getApplicationContext(), currentDownload);

        } else {

            notificationThread.interrupt();

            videoDownloaded(currentDownload.getId());
            updateNotificationComplete();

        }

        ((OazaApp) getApplication()).setDownloadingNow(false);

        Intent i = new Intent(DOWNLOAD_COMPLETE);
        sendBroadcast(i);

        ((OazaApp) getApplicationContext()).setDownloadService(null);

        init();

    }

    private void updateNotificationPercent(double downloaded, double total) {
        percentDownloaded = (long) ((float) downloaded / total * 100);
        builder.setProgress(100, (int) percentDownloaded, false);
        notificationManager.notify(5000, builder.build());
    }

    private void updateNotificationComplete() {
        builder.setProgress(0, 0, false);
        builder.setContentText(getString(R.string.download_completed));
        builder.setOngoing(false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void updateNotificationCanceled() {
        builder.setProgress(0, 0, false);
        builder.setContentText(getString(R.string.download_canceled));
        builder.setOngoing(false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotification() {
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(currentDownload.getName())
                .setContentText(getResources().getString(R.string.downloading_audio))
                .setProgress(100, 0, false)
                .setOngoing(true)
                .setContentIntent(pOpenDownloads)
                .addAction(R.drawable.ic_close, getString(R.string.cancel_download), pKillDownload)
                .setSmallIcon(R.drawable.download);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private List<Video> getDownloadQueue() {
        return Video.getDownloadQueue();
    }

    private void videoDownloaded(Long id) {
        Video video = Video.findById(Video.class, id);
        video.setInDownloadQueue(false);
        video.setDownloaded(true);
        video.save();
    }

    private int getDownloadQueueSize(List<Video> downloadQueue) {
        return downloadQueue.size();
    }

    private Video getFirstToDownload(List<Video> downloadQueue) {
        if (getDownloadQueueSize(downloadQueue) > 0) {
            return downloadQueue.get(0);
        } else {
            return null;
        }
    }

    public void killCurrentDownload() {
        canceled = true;
        if (currentIonDownload != null) {
            currentIonDownload.cancel();
        }
    }
}
