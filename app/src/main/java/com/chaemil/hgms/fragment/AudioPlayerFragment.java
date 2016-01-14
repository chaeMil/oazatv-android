package com.chaemil.hgms.fragment;

/**
 * Created by chaemil on 5.1.16.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.BitmapUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.johnpersano.supertoasts.SuperToast;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import at.markushi.ui.CircleButton;

/**
 * Created by chaemil on 2.12.15.
 */
public class AudioPlayerFragment extends Fragment implements View.OnClickListener,
        MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String TAG = "audio_player_fragment";
    private static final String IMAGES_ALREADY_BLURRED = "images_already_blurred";
    private static final String BG_DRAWABLE = "bg_drawable";
    private static final String CURRENT_TIME = "current_time";
    private static final int NOTIFICATION_ID = 1111;
    public static final String NOTIFY_PLAY_PAUSE = "notify_play_pause";
    public static final String NOTIFY_REW = "notify_rew";
    public static final String NOTIFY_FF = "notify_ff";
    public static final String NOTIFY_OPEN = "notify_open";
    private ImageView playerBg;
    private RelativeLayout miniPlayer;
    private ImageView miniPlayerImageView;
    private RelativeLayout playerToolbar;
    private boolean imagesAlreadyBlurred = false;
    private BitmapDrawable bgDrawable;
    private TextView miniPlayerText;
    private TextView playerTitle;
    private CircleButton playPause;
    private CircleButton rew;
    private CircleButton ff;
    private TextView currentTime;
    private TextView totalTime;
    private int duration;
    private int currentTimeInt;
    private AppCompatSeekBar seekBar;
    private Bitmap thumb;
    private Video currentAudio;
    private CircleButton miniPlayerPause;
    private ProgressBar bufferBar;
    private int bufferFail;
    private ViewGroup rootView;
    private MediaPlayer audioPlayer;
    private ImageView audioThumb;
    private WifiManager.WifiLock wifiLock;
    private AudioManager audioManager;
    private FragmentActivity mainActivity;
    private boolean downloaded;
    private NotificationManager notificationManager;
    private Notification notification;
    private NotificationCompat.Builder notificationBuilder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        audioPlayer = new MediaPlayer();

        wifiLock = ((WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        mainActivity = getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            SuperToast.create(getActivity(),
                    getString(R.string.audio_focus_not_granted),
                    SuperToast.Duration.SHORT).show();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releasePlayer();

        saveCurrentVideoTime();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.audio_player_fragment, container, false);

        if (savedInstanceState != null) {
            imagesAlreadyBlurred = savedInstanceState.getBoolean(IMAGES_ALREADY_BLURRED);

            bgDrawable = new BitmapDrawable(getResources(),
                    (Bitmap) savedInstanceState.getParcelable(BG_DRAWABLE));
        }

        getUI(rootView);
        setupUI();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IMAGES_ALREADY_BLURRED, imagesAlreadyBlurred);
        if (bgDrawable != null) {
            outState.putParcelable(BG_DRAWABLE, BitmapUtils.drawableToBitmap(bgDrawable));
        }
        outState.putInt(CURRENT_TIME, audioPlayer.getCurrentPosition());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            audioPlayer.seekTo(savedInstanceState.getInt(CURRENT_TIME));
        }
    }

    private void getUI(ViewGroup rootView) {
        miniPlayer = (RelativeLayout) rootView.findViewById(R.id.mini_player);
        playerBg = (ImageView) rootView.findViewById(R.id.player_bg);
        miniPlayerImageView = (ImageView) rootView.findViewById(R.id.mini_player_image);
        miniPlayerText = (TextView) rootView.findViewById(R.id.mini_player_text);
        playerToolbar = (RelativeLayout) rootView.findViewById(R.id.toolbar);
        playerTitle = (TextView) rootView.findViewById(R.id.player_title);
        audioThumb = (ImageView) rootView.findViewById(R.id.audio_thumb);
        playPause = (CircleButton) rootView.findViewById(R.id.play_pause);
        rew = (CircleButton) rootView.findViewById(R.id.rew);
        ff = (CircleButton) rootView.findViewById(R.id.ff);
        currentTime = (TextView) rootView.findViewById(R.id.current_time);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        seekBar = (AppCompatSeekBar) rootView.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        miniPlayerPause = (CircleButton) rootView.findViewById(R.id.mini_play_pause);
        bufferBar = (ProgressBar) rootView.findViewById(R.id.buffer_bar);
    }

    private void setupUI() {
        playPause.setOnClickListener(this);
        rew.setOnClickListener(this);
        ff.setOnClickListener(this);
        miniPlayerPause.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.play_pause:
                playPauseAudio();
                break;
            case R.id.rew:
                seekREW();
                break;
            case R.id.ff:
                seekFF();
                break;
            case R.id.mini_play_pause:
                playPauseAudio();
                break;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        audioPlayer.start();
        audioPlayer.seekTo(currentAudio.getCurrentTime());
        duration = audioPlayer.getDuration();
        seekBar.setMax(duration);
        seekBar.postDelayed(onEverySecond, 1000);
        YoYo.with(Techniques.FadeIn).duration(350).delay(250).playOn(audioThumb);

        mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {

                float temp = ((float) mp.getCurrentPosition() / (float) mp.getDuration()) * 100;
                if (Math.abs(percent - temp) < 1) {
                    bufferFail++;
                    if (bufferFail == 15) {
                        SmartLog.Log(SmartLog.LogLevel.WARN, "bufferFail", "buffering failed");
                    }
                }
            }
        });

        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    bufferBar.setVisibility(View.VISIBLE);
                    saveCurrentVideoTime();
                }
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    bufferBar.setVisibility(View.GONE);
                    saveCurrentVideoTime();
                }
                return false;
            }
        });
    }

    private Runnable onEverySecond = new Runnable() {
        @Override
        public void run(){
            try {
                if (seekBar != null && audioPlayer != null) {
                    seekBar.setProgress(audioPlayer.getCurrentPosition());
                }

                if (audioPlayer != null && audioPlayer.isPlaying()) {
                    if (seekBar != null) {
                        seekBar.postDelayed(onEverySecond, 1000);
                    }
                    updateTime();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void updateTime() {
        currentTimeInt = audioPlayer.getCurrentPosition();

        int dSeconds = (duration / 1000) % 60 ;
        int dMinutes = ((duration / (1000*60)) % 60);
        int dHours   = ((duration / (1000*60*60)) % 24);

        int cSeconds = (currentTimeInt / 1000) % 60 ;
        int cMinutes = ((currentTimeInt / (1000*60)) % 60);
        int cHours   = ((currentTimeInt / (1000*60*60)) % 24);

        if(dHours == 0){
            currentTime.setText(String.format("%02d:%02d", cMinutes, cSeconds));
            totalTime.setText(String.format("%02d:%02d", dMinutes, dSeconds));
        } else{
            currentTime.setText(String.format("%02d:%02d:%02d", cHours, cMinutes, cSeconds));
            totalTime.setText(String.format("%02d:%02d:%02d", dHours, dMinutes, dSeconds));
        }
    }

    public void playPauseAudio() {
        saveCurrentVideoTime();

        if (audioPlayer.isPlaying()) {
            pauseAudio();
        } else {
            playAudio();
        }


    }

    public void seekFF() {
        audioPlayer.seekTo(audioPlayer.getCurrentPosition() + 10000);
    }

    public void seekREW() {
        audioPlayer.seekTo(audioPlayer.getCurrentPosition() - 10000);
    }

    public void pauseAudio() {
        audioPlayer.pause();

        if (wifiLock.isHeld()) {
            wifiLock.release();
        }

        playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
    }

    public void playAudio() {
        audioPlayer.start();

        wifiLock.acquire();

        playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

        if (seekBar != null) {
            seekBar.postDelayed(onEverySecond, 1000);
        }
    }

    public void switchMiniPlayer(boolean show) {
        if (isAdded()) {
            if (show) {
                playerToolbar.setVisibility(View.GONE);
                miniPlayer.setVisibility(View.VISIBLE);
            } else {
                playerToolbar.setVisibility(View.VISIBLE);
                miniPlayer.setVisibility(View.GONE);
            }
        }
    }

    private void createNotification() {

        notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        Intent open = new Intent(NOTIFY_OPEN);
        PendingIntent pOpen = PendingIntent.getBroadcast(mainActivity, 0, open, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pause = new Intent(NOTIFY_PLAY_PAUSE);
        PendingIntent pPause = PendingIntent.getBroadcast(mainActivity, 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent ff = new Intent(NOTIFY_FF);
        PendingIntent pFf = PendingIntent.getBroadcast(mainActivity, 0, ff, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent rew = new Intent(NOTIFY_REW);
        PendingIntent pRew = PendingIntent.getBroadcast(mainActivity, 0, rew, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getActivity())
                .setContentTitle(currentAudio.getName())
                .setContentText(currentAudio.getDate())
                .setSmallIcon(R.drawable.white_logo)
                .setLargeIcon(thumb)
                .setContentIntent(pOpen)
                .setOngoing(true)
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
    }

    public void saveCurrentVideoTime() {
        if (audioPlayer != null && currentAudio != null) {
            try {
                currentAudio.setCurrentTime(audioPlayer.getCurrentPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentAudio.save();
        }
    }

    public void playNewAudio(final Video audio, final boolean downloaded) {

        saveCurrentVideoTime();

        this.downloaded = downloaded;

        Video savedAudio = null;

        try {
            savedAudio = Video.findByServerId(audio.getServerId());
        } catch (Exception e) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
        }

        if (savedAudio != null) {
            this.currentAudio = savedAudio;

            SuperToast.create(getActivity(),
                    getString(R.string.resuming_from_saved_time),
                    SuperToast.Duration.SHORT).show();
        } else {
            this.currentAudio = audio;
        }

        Picasso.with(getActivity()).load(currentAudio.getThumbFile()).into(audioThumb);
        Picasso.with(getActivity()).load(currentAudio.getThumbFile()).centerCrop().resize(320, 320).into(miniPlayerImageView);

        playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

        String downloadedString = "";
        if (downloaded) {
            downloadedString = "[" + getString(R.string.downloaded) + "] ";
        }

        miniPlayerText.setText(downloadedString + audio.getName());
        playerTitle.setText(downloadedString + audio.getName());

        currentTime.setText("00:00:00");
        totalTime.setText("???");

        ((MainActivity) getActivity()).expandPanel();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                imagesAlreadyBlurred = false;
                bgDrawable = null;

                resizeAndBlurBg();

                createNotification();

                wifiLock.acquire();

                try {
                    audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    if (downloaded) {
                        audioPlayer.setDataSource(getContext().getExternalFilesDir(null) + "/" + currentAudio.getHash() + ".mp3");
                    } else {
                        audioPlayer.setDataSource(currentAudio.getAudioFile());
                    }
                    audioPlayer.setWakeMode(getActivity(), PowerManager.PARTIAL_WAKE_LOCK);
                    audioPlayer.prepareAsync();
                    audioPlayer.setOnPreparedListener(AudioPlayerFragment.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 500);

    }

    public void releasePlayer() {
        if (audioPlayer != null) {
            audioPlayer.release();
        }
        if (wifiLock != null) {
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
        }
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private void resizeAndBlurBg() {
        new ComputeImage().execute(null);
    }

    public RelativeLayout getPlayerToolbar() {
        return playerToolbar;
    }

    public RelativeLayout getMiniPlayer() {
        return miniPlayer;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            audioPlayer.seekTo(progress);
            updateTime();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                try {
                    if (audioPlayer == null) {
                        playNewAudio(currentAudio, downloaded);
                    } else if (!audioPlayer.isPlaying()) {
                        playAudio();
                    }
                    audioPlayer.setVolume(1.0f, 1.0f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (audioPlayer != null) {
                    try {
                        if (audioPlayer.isPlaying()) {
                            pauseAudio();
                        }
                        audioPlayer.release();
                        audioPlayer = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (audioPlayer != null) {
                    try {
                        if (audioPlayer.isPlaying()) {
                            pauseAudio();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (audioPlayer != null) {
                    try {
                        if (audioPlayer.isPlaying()) {
                            audioPlayer.setVolume(0.1f, 0.1f);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private class ComputeImage extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                thumb = BitmapUtils.getBitmapFromURL(currentAudio.getThumbFile());
                if (thumb == null) {
                    thumb = BitmapUtils.drawableToBitmap(getResources().getDrawable(R.drawable.placeholder));
                }
            } catch (Exception e) {
                SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
            }
        }

        @Override
        protected Object doInBackground(Object[] params) {

            if (!imagesAlreadyBlurred && thumb != null) {
                SmartLog.Log(SmartLog.LogLevel.DEBUG, "resizeAndBlurBg", "blurring bg image");
                Bitmap originalBitmap = thumb;
                Bitmap blurredPlayerBitmap = BitmapUtils.blur(getContext(), originalBitmap, 25);
                Bitmap resizedBitmap = BitmapUtils.resizeImageForImageView(blurredPlayerBitmap, 255);
                bgDrawable = new BitmapDrawable(getResources(), resizedBitmap);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            YoYo.with(Techniques.FadeOut).duration(400).playOn(playerBg);
            YoYo.with(Techniques.FadeOut).duration(400).playOn(miniPlayerImageView);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playerBg.setImageDrawable(bgDrawable);

                    YoYo.with(Techniques.FadeIn).duration(400).playOn(playerBg);
                    YoYo.with(Techniques.FadeIn).duration(400).playOn(miniPlayerImageView);
                }
            }, 400);

            imagesAlreadyBlurred = true;
        }
    }
}

