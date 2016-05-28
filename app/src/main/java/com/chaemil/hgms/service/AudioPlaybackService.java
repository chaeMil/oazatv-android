package com.chaemil.hgms.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.view.View;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.SmartLog;
import com.github.johnpersano.supertoasts.SuperToast;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by chaemil on 28.5.16.
 */
public class AudioPlaybackService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, RequestFactoryListener {

    private static final int NOTIFICATION_ID = 1111;
    public static final String AUDIO = "current_audio";
    public static final String DOWNLOADED = "downloaded";

    private MediaPlayer player;
    private int audioPos;
    private WifiManager.WifiLock wifiLock;
    private Video currentAudio;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private int duration;
    private final AudioPlaybackBind audioPlaybackBind = new AudioPlaybackBind();
    private boolean downloaded = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        initMusicPlayer();
        if (intent != null) {
            if (intent.getParcelableExtra(AUDIO) != null) {
                currentAudio = intent.getParcelableExtra(AUDIO);
                downloaded = intent.getBooleanExtra(DOWNLOADED, false);
                playNewAudio();
            }
        }

        return audioPlaybackBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void onCreate() {
        super.onCreate();
        audioPos = 0;
        player = new MediaPlayer();
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

        wifiLock = ((WifiManager) getApplication().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
    }

    public void saveCurrentAudioTime() {
        if (player != null && currentAudio != null) {
            try {
                currentAudio.setCurrentTime(player.getCurrentPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentAudio.save();
        }
    }

    public Video getCurrentAudio() {
        return currentAudio;
    }

    public MediaPlayer getAudioPlayer() {
        return player;
    }

    private void postVideoView() {
        JsonObjectRequest postView = RequestFactory.postVideoView(this, currentAudio.getHash());
        MyRequestService.getRequestQueue().add(postView);
    }

    public void playNewAudio() {

        createNotification();
        wifiLock.acquire();

        Video savedAudio = null;

        try {
            savedAudio = Video.findByServerId(currentAudio.getServerId());
        } catch (Exception e) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
        }

        if (savedAudio != null) {
            this.currentAudio = savedAudio;

            SuperToast.create(getApplication(),
                    getString(R.string.resuming_from_saved_time),
                    SuperToast.Duration.SHORT).show();
        }

        try {
            if (player != null) {
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                if (downloaded) {
                    player.setDataSource(getApplication().getExternalFilesDir(null) + "/" + currentAudio.getHash() + ".mp3");
                } else {
                    player.setDataSource(currentAudio.getAudioFile());
                }
                player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                player.prepareAsync();
                player.setOnPreparedListener(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNotification() {

        /*if (getApplication() != null
                && getApplication().getSystemService(Context.NOTIFICATION_SERVICE) != null) {

            notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);

            Intent open = new Intent(NOTIFY_OPEN);
            PendingIntent pOpen = PendingIntent.getBroadcast(mainActivity, 0, open, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent pause = new Intent(NOTIFY_PLAY_PAUSE);
            PendingIntent pPause = PendingIntent.getBroadcast(mainActivity, 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent ff = new Intent(NOTIFY_FF);
            PendingIntent pFf = PendingIntent.getBroadcast(mainActivity, 0, ff, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent rew = new Intent(NOTIFY_REW);
            PendingIntent pRew = PendingIntent.getBroadcast(mainActivity, 0, rew, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent delete = new Intent(NOTIFY_DELETE);
            PendingIntent pDelete = PendingIntent.getBroadcast(mainActivity, 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getActivity())
                    .setContentTitle(currentAudio.getName())
                    .setContentText(currentAudio.getDate())
                    .setSmallIcon(R.drawable.white_logo)
                    .setContentIntent(pOpen)
                    .setOngoing(true)
                    .setDeleteIntent(pDelete)
                    .addAction(R.drawable.rew, "", pRew)
                    .addAction(R.drawable.pause, "", pPause)
                    .addAction(R.drawable.ff, "", pFf)
                    .setStyle(new NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                    );

            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk >= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

            Ion.with(getApplication())
                    .load(currentAudio.getThumbFile())
                    .asBitmap()
                    .setCallback(new FutureCallback<Bitmap>() {
                        @Override
                        public void onCompleted(Exception e, Bitmap result) {
                            notificationBuilder.setLargeIcon(result);
                            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                        }
                    });
        }*/
    }

    public class AudioPlaybackBind extends Binder {
        public AudioPlaybackService getService() {
            return AudioPlaybackService.this;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
        player.seekTo(currentAudio.getCurrentTime());
        duration = player.getDuration();
        //seekBar.setMax(duration);
        //seekBar.postDelayed(onEverySecond, 1000);
        //YoYo.with(Techniques.FadeIn).duration(350).delay(250).playOn(audioThumb);

        mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {

                float temp = ((float) mp.getCurrentPosition() / (float) mp.getDuration()) * 100;
                if (Math.abs(percent - temp) < 1) {
                    /*bufferFail++;
                    if (bufferFail == 15) {
                        SmartLog.Log(SmartLog.LogLevel.WARN, "bufferFail", "buffering failed");
                    }*/
                }
            }
        });

        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    //bufferBar.setVisibility(View.VISIBLE);
                    saveCurrentAudioTime();
                }
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    //bufferBar.setVisibility(View.GONE);
                    saveCurrentAudioTime();
                }
                return false;
            }
        });
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        switch(requestType) {
            case POST_VIDEO_VIEW:
                SmartLog.Log(SmartLog.LogLevel.DEBUG, "postedVideoView", "ok");
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        BaseActivity.responseError(exception, getApplication());
    }
}
