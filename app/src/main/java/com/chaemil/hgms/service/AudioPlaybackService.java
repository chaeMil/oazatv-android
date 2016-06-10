package com.chaemil.hgms.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.view.View;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.fragment.AudioPlayerFragment;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.receiver.AudioPlaybackReceiver;
import com.chaemil.hgms.utils.SmartLog;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by chaemil on 28.5.16.
 */
public class AudioPlaybackService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, RequestFactoryListener {

    public static final String AUDIO = "current_audio";
    public static final String DOWNLOADED = "downloaded";
    private static final int NOTIFICATION_ID = 1111;

    private MediaPlayer player;
    private int audioPos;
    private WifiManager.WifiLock wifiLock;
    private Video currentAudio;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private int duration;
    private final AudioPlaybackBind audioPlaybackBind = new AudioPlaybackBind();
    private AudioPlaybackReceiver audioPlaybackReceiver;
    private boolean downloaded;
    private int notificationID;
    private static AudioPlaybackService instance = null;

    public static AudioPlaybackService getInstance() {
        return instance;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return audioPlaybackBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onDestroy() {
        stop();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init(intent);
        return START_STICKY;
    }

    private void init(Intent bindIntent) {
        instance = this;

        ((OazaApp) getApplication()).playbackService = this;

        notificationID = NOTIFICATION_ID;

        audioPos = 0;
        player = new MediaPlayer();

        initMusicPlayer();
        setupReceiver();
        if (bindIntent != null) {
            if (bindIntent.getParcelableExtra(AUDIO) != null) {
                currentAudio = bindIntent.getParcelableExtra(AUDIO);
                downloaded = bindIntent.getBooleanExtra(DOWNLOADED, false);
                playNewAudio();
            }
        }
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

    private void setupReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioPlayerFragment.NOTIFY_PLAY_PAUSE);
        filter.addAction(AudioPlayerFragment.NOTIFY_OPEN);
        filter.addAction(AudioPlayerFragment.NOTIFY_FF);
        filter.addAction(AudioPlayerFragment.NOTIFY_REW);
        filter.addAction(AudioPlayerFragment.NOTIFY_DELETE);

        audioPlaybackReceiver = new AudioPlaybackReceiver(this, ((OazaApp) getApplication()));
        registerReceiver(audioPlaybackReceiver, filter);
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

    public boolean getIsPlayingDownloaded() {
        return downloaded;
    }

    public MediaPlayer getAudioPlayer() {
        return player;
    }

    private void postVideoView() {
        JsonObjectRequest postView = RequestFactory.postVideoView(this, currentAudio.getHash());
        MyRequestService.getRequestQueue().add(postView);
    }

    public void playPauseAudio() {
        if (player != null) {
            if (player.isPlaying()) {
                pauseAudio();
            } else {
                playAudio();
            }
        }
    }

    public void pauseAudio() {
        if (player != null) {
            player.pause();

            if (wifiLock.isHeld()) {
                wifiLock.release();
            }

            notificationBuilder.mActions.get(1).icon = R.drawable.play;
            notificationManager.notify(notificationID,
                    notificationBuilder.build());

            /*playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
            miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play));*/
        }
    }

    public void playAudio() {
        player.start();

        notificationBuilder.mActions.get(1).icon = R.drawable.pause;
        notificationManager.notify(notificationID,
                notificationBuilder.build());

        wifiLock.acquire();

        /*playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

        if (seekBar != null) {
            seekBar.postDelayed(onEverySecond, 1000);
        }*/
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

        if (getApplication() != null
                && getApplication().getSystemService(Context.NOTIFICATION_SERVICE) != null) {

            ArrayList<PendingIntent> intents = AudioPlaybackPendingIntents.generate(getApplication());

            notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplication())
                    .setContentTitle(currentAudio.getName())
                    .setContentText(currentAudio.getDate())
                    .setSmallIcon(R.drawable.white_logo)
                    .setContentIntent(intents.get(0))
                    .setDeleteIntent(intents.get(4))
                    .setAutoCancel(true)
                    .addAction(R.drawable.rew, "", intents.get(3))
                    .addAction(R.drawable.pause, "", intents.get(1))
                    .addAction(R.drawable.ff, "", intents.get(2))
                    .addAction(R.drawable.ic_close, "", intents.get(4))
                    .setStyle(new NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2, 3)
                    );

            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk >= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            notificationManager.notify(notificationID, notificationBuilder.build());
            startForeground(notificationID, notificationBuilder.build());

            Ion.with(getApplication())
                    .load(currentAudio.getThumbFile())
                    .asBitmap()
                    .setCallback(new FutureCallback<Bitmap>() {
                        @Override
                        public void onCompleted(Exception e, Bitmap result) {
                            notificationBuilder.setLargeIcon(result);
                            notificationManager.notify(notificationID, notificationBuilder.build());
                        }
                    });
        }
    }

    public void stop() {
        ((OazaApp) getApplication()).playbackService = null;

        saveCurrentAudioTime();

        if (player != null) {
            player.stop();
            player = null;
        }

        if (audioPlaybackReceiver != null) {
            try {
                unregisterReceiver(audioPlaybackReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        stopForeground(true);
        stopSelf();
    }

    public class AudioPlaybackBind extends Binder {

        public AudioPlaybackService getService() {
            return getInstance();
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
