package com.chaemil.hgms.fragment;

/**
 * Created by chaemil on 5.1.16.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.AudioPlaybackService;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.BitmapUtils;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.GAUtils;
import com.chaemil.hgms.utils.OnSwipeTouchListener;
import com.chaemil.hgms.utils.ShareUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.chaemil.hgms.service.AudioPlaybackService.AudioPlaybackBind;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import at.markushi.ui.CircleButton;

/**
 * Created by chaemil on 2.12.15.
 */
public class AudioPlayerFragment extends BaseFragment implements View.OnClickListener,
        MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener,
        AudioManager.OnAudioFocusChangeListener, RequestFactoryListener {

    public static final String TAG = "audio_player_fragment";
    private static final String IMAGES_ALREADY_BLURRED = "images_already_blurred";
    private static final String BG_DRAWABLE = "bg_drawable";
    private static final String CURRENT_TIME = "current_time";
    private static final int NOTIFICATION_ID = 1111;
    public static final String NOTIFY_PLAY_PAUSE = "notify_play_pause";
    public static final String NOTIFY_REW = "notify_rew";
    public static final String NOTIFY_FF = "notify_ff";
    public static final String NOTIFY_OPEN = "notify_open";
    public static final String NOTIFY_DELETE = "notify_delete";
    private RelativeLayout miniPlayer;
    private ImageView miniPlayerImageView;
    private RelativeLayout playerToolbar;
    private TextView miniPlayerText;
    private TextView playerTitle;
    private CircleButton playPause;
    private CircleButton rew;
    private TextView currentTime;
    private TextView totalTime;
    private int duration;
    private int currentTimeInt;
    private AppCompatSeekBar seekBar;
    private CircleButton miniPlayerPause;
    private ProgressBar bufferBar;
    private int bufferFail;
    private ViewGroup rootView;
    private ImageView audioThumb;
    private MainActivity mainActivity;
    private ImageView back;
    private ImageView share;
    private TextView description;
    private TextView tags;
    private TimerTask timerTask;
    private Timer timer;
    private Intent playIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        /*int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            SuperToast.create(getActivity(),
                    getString(R.string.audio_focus_not_granted),
                    SuperToast.Duration.SHORT).show();
        }

        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.AUDIOPLAYER_FRAGMENT);
        setupTimer();*/
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releasePlayer();

        //saveCurrentAudioTime();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.audio_player_fragment, container, false);

        getUI(rootView);
        activateUI(false);
        setupUI();

        return rootView;
    }

    private void setupTimer() {
        stopTimer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        postGA();
                    }
                });
            }
        };
        resetTimer();
    }

    private void resetTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 5000, 5000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void postGA() {
        /*if (audioPlayer != null && audioPlayer.isPlaying()) {
            GAUtils.sendGAScreen(
                    ((OazaApp) getActivity().getApplication()),
                    "AudioPlayer",
                    currentAudio.getNameCS());
        }*/
    }

    private void activateUI(boolean state) {
        playPause.setEnabled(state);
        rew.setEnabled(state);
        seekBar.setEnabled(state);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*if (audioPlayer != null) {
            try {
                outState.putInt(CURRENT_TIME, audioPlayer.getCurrentPosition());
            } catch (Exception e) {
                SmartLog.Log(SmartLog.LogLevel.DEBUG, "exception", e.toString());
            }
        }*/
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        /*if (savedInstanceState != null) {
            audioPlayer.seekTo(savedInstanceState.getInt(CURRENT_TIME));
        }*/
    }

    private void getUI(ViewGroup rootView) {
        miniPlayer = (RelativeLayout) rootView.findViewById(R.id.mini_player);
        miniPlayerImageView = (ImageView) rootView.findViewById(R.id.mini_player_image);
        miniPlayerText = (TextView) rootView.findViewById(R.id.mini_player_text);
        playerToolbar = (RelativeLayout) rootView.findViewById(R.id.toolbar);
        playerTitle = (TextView) rootView.findViewById(R.id.player_title);
        audioThumb = (ImageView) rootView.findViewById(R.id.audio_thumb);
        playPause = (CircleButton) rootView.findViewById(R.id.play_pause);
        rew = (CircleButton) rootView.findViewById(R.id.rew);
        currentTime = (TextView) rootView.findViewById(R.id.current_time);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        seekBar = (AppCompatSeekBar) rootView.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        miniPlayerPause = (CircleButton) rootView.findViewById(R.id.mini_play_pause);
        bufferBar = (ProgressBar) rootView.findViewById(R.id.buffer_bar);
        back = (ImageView) rootView.findViewById(R.id.back);
        share = (ImageView) rootView.findViewById(R.id.share);
        description = (TextView) rootView.findViewById(R.id.description);
        tags = (TextView) rootView.findViewById(R.id.tags);
    }

    private void setupUI() {
        back.setOnClickListener(this);
        playPause.setOnClickListener(this);
        rew.setOnClickListener(this);
        miniPlayerPause.setOnClickListener(this);
        share.setOnClickListener(this);
        miniPlayer.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                swipeDismissPlayer(true);
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                swipeDismissPlayer(false);
            }
        });
    }

    private void swipeDismissPlayer(boolean right) {
        //saveCurrentAudioTime();

        //pauseAudio();
        releasePlayer();
        if (right) {
            YoYo.with(Techniques.SlideOutRight).duration(300).playOn(miniPlayer);
        } else {
            YoYo.with(Techniques.SlideOutLeft).duration(300).playOn(miniPlayer);
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) getActivity()).hidePanel();
            }
        }, 300);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.play_pause:
                //playPauseAudio();
                break;
            case R.id.rew:
                seekREW();
                break;
            case R.id.mini_play_pause:
                //playPauseAudio();
                break;
            case R.id.back:
                ((MainActivity) getActivity()).collapsePanel();
                break;
            case R.id.share:
                ShareUtils.shareAudioLink(getActivity(), getCurrentAudio());
                break;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        activateUI(true);
        /*audioPlayer.start();
        audioPlayer.seekTo(currentAudio.getCurrentTime());
        duration = audioPlayer.getDuration();*/
        seekBar.setMax(duration);
        seekBar.postDelayed(onEverySecond, 1000);
        YoYo.with(Techniques.FadeIn).duration(350).delay(250).playOn(audioThumb);
    }

    private Runnable onEverySecond = new Runnable() {
        @Override
        public void run(){
            /*try {
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
            }*/
        }
    };

    private void updateTime() {
        //currentTimeInt = audioPlayer.getCurrentPosition();

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

    public void seekFF() {
        //audioPlayer.seekTo(audioPlayer.getCurrentPosition() + 10000);
    }

    public void seekREW() {
        //audioPlayer.seekTo(audioPlayer.getCurrentPosition() - 30 * 1000);
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

    public Video getCurrentAudio() {
        return AudioPlaybackService.getInstance().getCurrentAudio();
    }

    public void closePlayer() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        ((MainActivity) getActivity()).hidePanel();
    }

    public void playNewAudio(Context context, final Video audio, final boolean downloaded) {
        Ion.with(context).load(getCurrentAudio().getThumbFile()).intoImageView(audioThumb);
        Ion.with(context).load(getCurrentAudio().getThumbFile()).intoImageView(miniPlayerImageView);

        playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

        String downloadedString = "";
        if (downloaded) {
            downloadedString = "[" + getString(R.string.downloaded) + "] ";
        }

        miniPlayerText.setText(downloadedString + audio.getName());
        playerTitle.setText(downloadedString + audio.getName());
        if (!getCurrentAudio().getDescription().equals("")) {
            description.setText(getCurrentAudio().getDescription());
        } else {
            description.setVisibility(View.GONE);
        }
        if (!getCurrentAudio().getTags().equals("")) {
            String tagsString = "";
            for (String tag : getCurrentAudio().getTags().split(",")) {
                tagsString += "#" + tag + " ";
            }
            tags.setText(tagsString);
        } else {
            tags.setVisibility(View.GONE);
        }

        currentTime.setText("00:00:00");
        totalTime.setText("???");

        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.AUDIOPLAYER_FRAGMENT + "audioHash: " + getCurrentAudio().getHash());

        postGA();
    }

    public void releasePlayer() {
        /*if (audioPlayer != null) {
            audioPlayer.release();
        }*/
        /*if (wifiLock != null) {
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
        }
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }*/
    }

    public RelativeLayout getPlayerToolbar() {
        return playerToolbar;
    }

    public RelativeLayout getMiniPlayer() {
        return miniPlayer;
    }

    public MediaPlayer getAudioPlayer() {
        return mainActivity.playbackService.getAudioPlayer();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        /*if (fromUser) {
            audioPlayer.seekTo(progress);
            updateTime();
            saveCurrentAudioTime();
        }*/
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        /*switch (focusChange) {
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
        }*/
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {

    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {

    }
}

