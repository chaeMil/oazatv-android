package com.chaemil.hgms.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.ui.activity.BaseActivity;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.receiver.AudioPlaybackReceiver;
import com.chaemil.hgms.receiver.PlaybackReceiverListener;
import com.chaemil.hgms.utils.OSUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.chaemil.hgms.utils.StringUtils;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;

/**
 * Created by chaemil on 28.5.16.
 */
public class AudioPlaybackService extends Service implements
        MediaPlayer.OnPreparedListener,
        PlaybackReceiverListener,
        RequestFactoryListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener {

    public static final String AUDIO = "current_audio";
    public static final String DOWNLOADED = "downloaded";
    public static final String BUFFERING_START = "buffering_start";
    public static final String BUFFERING_END = "buffering_end";
    public static final int NOTIFICATION_ID = 1111;

    private MediaPlayer player;
    private WifiManager.WifiLock wifiLock;
    private Video currentAudio;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private AudioPlaybackReceiver audioPlaybackReceiver;
    private boolean downloaded;
    private static AudioPlaybackService instance = null;
    private IntentFilter noisyAudioIntent = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private NoisyAudioStreamReceiver noisyAudioReceiver;
    private OazaApp app;
    private boolean readPhoneState;

    public static AudioPlaybackService getInstance() {
        return instance;
    }

    @Override
    public void onDestroy() {
        playbackStop();
        unregisterPlaybackReceiver();
        unregisterNoisyAudioReceiver();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getParcelableExtra(AUDIO) != null) {
            currentAudio = intent.getParcelableExtra(AUDIO);
            downloaded = intent.getBooleanExtra(DOWNLOADED, false);
            app = (OazaApp) getApplication();
            readPhoneState = app.getMainActivity()
                    .isPermissionGranted(app, PermissionUtils.Manifest_READ_PHONE_STATE);
            init();
        } else {
            stopSelf();
        }

        return START_STICKY;
    }

    private void init() {
        instance = this;
        ((OazaApp) getApplication()).playbackService = this;

        if (app != null && app.getMainActivity() != null) {
            if (OSUtils.isRunningMarshmallow()
                    && OSUtils.hasPhoneFeature(app)
                    && !readPhoneState) {
                new MaterialDialog.Builder(app.getMainActivity())
                        .content(R.string.audioplayer_phonestate_permission)
                        .cancelable(false)
                        .positiveText(R.string.ok)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog,
                                                @NonNull DialogAction which) {
                                askPhoneStatePermissions();
                            }
                        })
                        .show();
            } else {
                phoneStatePermissionGranted();
            }
        }
    }

    private void askPhoneStatePermissions() {
        app.getMainActivity().askCompactPermissions(new String[] {
                    PermissionUtils.Manifest_READ_PHONE_STATE,
                    PermissionUtils.Manifest_PROCESS_OUTGOING_CALLS
                },
                new PermissionResult() {
                    @Override
                    public void permissionGranted() {
                        phoneStatePermissionGranted();
                    }

                    @Override
                    public void permissionDenied() {
                        phoneStatePermissionDenied();
                    }

                    @Override
                    public void permissionForeverDenied() {
                        SuperToast.create(app,
                                getString(R.string.permission_revoked_phone_state),
                                SuperToast.Duration.LONG).show();
                    }
                });
    }

    private void phoneStatePermissionGranted() {
        readPhoneState = true;
        initMusicPlayer();
        setupPlaybackReceiver();
        playNewAudio(currentAudio);
    }

    private void phoneStatePermissionDenied() {
        readPhoneState = false;
        playbackStop();
        app.getMainActivity().hidePanel();
        SuperToast.create(app,
                getString(R.string.permission_revoked),
                SuperToast.Duration.MEDIUM).show();
    }

    public void initMusicPlayer(){
        player = new MediaPlayer();

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnInfoListener(this);

        wifiLock = ((WifiManager) getApplication()
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
    }

    private void setupPlaybackReceiver() {
        unregisterPlaybackReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioPlaybackReceiver.NOTIFY_PLAY_PAUSE);
        filter.addAction(AudioPlaybackReceiver.NOTIFY_OPEN);
        filter.addAction(AudioPlaybackReceiver.NOTIFY_FF);
        filter.addAction(AudioPlaybackReceiver.NOTIFY_REW);
        filter.addAction(AudioPlaybackReceiver.NOTIFY_DELETE);
        filter.addAction(AudioPlaybackReceiver.NOTIFY_PAUSE);

        audioPlaybackReceiver = new AudioPlaybackReceiver(this, ((OazaApp) getApplication()));
        registerReceiver(audioPlaybackReceiver, filter);

        noisyAudioReceiver = new NoisyAudioStreamReceiver();
        registerReceiver(noisyAudioReceiver, noisyAudioIntent);
    }

    private void unregisterPlaybackReceiver() {
        if (audioPlaybackReceiver != null) {
            try {
                unregisterReceiver(audioPlaybackReceiver);
                audioPlaybackReceiver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void unregisterNoisyAudioReceiver() {
        if (noisyAudioReceiver != null) {
            try {
                unregisterReceiver(noisyAudioReceiver);
                noisyAudioReceiver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        RequestService.getRequestQueue().add(postView);
    }

    public void playbackPlayPauseAudio() {
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

            if (app != null
                    && app.getMainActivity() != null
                    && app.getMainActivity().getAudioPlayerFragment() != null) {
                app.getMainActivity().getAudioPlayerFragment().playPause();
            }

            saveCurrentAudioTime();

            if (wifiLock.isHeld()) {
                wifiLock.release();
            }

            notificationBuilder.mActions.get(1).icon = R.drawable.play;
            notificationBuilder.setOngoing(false);
            notificationManager.notify(NOTIFICATION_ID,
                    notificationBuilder.build());
            stopForeground(false);
        }
    }

    public void playAudio() {
        if (player != null) {
            player.start();

            if (app != null
                    && app.getMainActivity() != null
                    && app.getMainActivity().getAudioPlayerFragment() != null) {
                app.getMainActivity().getAudioPlayerFragment().playPause();
            }

            notificationBuilder.mActions.get(1).icon = R.drawable.pause;
            notificationBuilder.setOngoing(true);
            startForeground(NOTIFICATION_ID, notificationBuilder.build());

            if (!currentAudio.isAudioDownloaded(app)) {
                wifiLock.acquire();
            }
        }
    }

    public void playNewAudio(Video audio) {

        createNotification();
        if (!currentAudio.isAudioDownloaded(app)) {
            wifiLock.acquire();
        }

        Video savedAudio = null;

        try {
            savedAudio = Video.findByServerId(audio.getServerId());
        } catch (Exception e) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
        }

        if (savedAudio != null) {
            this.currentAudio = savedAudio;
        }

        try {
            if (player != null) {
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                if (downloaded) {
                    player.setDataSource(getApplication()
                            .getExternalFilesDir(null) + "/" + currentAudio.getHash() + ".mp3");
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

        postVideoView();
    }

    private void createNotification() {

        if (getApplication() != null
                && currentAudio != null
                && getApplication().getSystemService(Context.NOTIFICATION_SERVICE) != null) {

            ArrayList<PendingIntent> intents = AudioPlaybackPendingIntents.generate(getApplication());

            notificationManager = (NotificationManager) getApplication()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);

            notificationBuilder = (NotificationCompat.Builder) new NotificationCompat
                    .Builder(getApplication())
                    .setContentTitle(currentAudio.getName())
                    .setContentText(StringUtils.formatDate(currentAudio.getDate(), this))
                    .setSmallIcon(R.drawable.white_logo)
                    .setContentIntent(intents.get(0))
                    .setDeleteIntent(intents.get(4))
                    .setAutoCancel(true)
                    .setWhen(0)
                    .addAction(R.drawable.rew, "", intents.get(3))
                    .addAction(R.drawable.pause, "", intents.get(1))
                    .addAction(R.drawable.ff, "", intents.get(2));

            if (!OSUtils.isHuawei()) {
                notificationBuilder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2));
            }

            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk >= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            if (sdk >= Build.VERSION_CODES.JELLY_BEAN) {
                notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
            }

            startForeground(NOTIFICATION_ID, notificationBuilder.build());

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
        }
    }

    public void playbackStop() {
        ((OazaApp) getApplication()).playbackService = null;

        saveCurrentAudioTime();

        if (player != null) {
            player.stop();
            player = null;
        }

        unregisterPlaybackReceiver();

        stopForeground(true);
        stopSelf();
    }

    @Override
    public void playbackPauseAudio() {
        pauseAudio();
    }

    public void playbackSeekFF() {
        player.seekTo(player.getCurrentPosition() + 10000);
    }

    public void playbackSeekREW() {
        player.seekTo(player.getCurrentPosition() - 30 * 1000);
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
        createNotification();

        if (player == null) {
            player = mp;
        }
        if (player != null) {
            player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);

            player.start();
            player.seekTo(currentAudio.getCurrentTime());
        }
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

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            Intent bufferingStart = new Intent(BUFFERING_START);
            sendBroadcast(bufferingStart);
            saveCurrentAudioTime();
        }
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            Intent bufferingEnd = new Intent(BUFFERING_END);
            sendBroadcast(bufferingEnd);
            saveCurrentAudioTime();
        }
        return false;
    }

    private class NoisyAudioStreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                pauseAudio();
            }
        }
    }
}
