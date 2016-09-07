package com.chaemil.hgms.fragment.video_player;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.android.volley.VolleyError;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.fragment.FullscreenFragment;
import com.chaemil.hgms.fragment.VideoPlayerFragment;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.utils.NetworkUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.ybq.android.spinkit.SpinKitView;
import com.koushikdutta.ion.Ion;

/**
 * Created by chaemil on 7.9.16.
 */
public class PlayerFragment extends FullscreenFragment implements EasyVideoCallback {

    private static final int PERIODICAL_SAVE_TIME = 5000;
    private ViewGroup rootView;
    private EasyVideoPlayer player;
    private SpinKitView buffering;
    private VideoPlayerFragment videoPlayerFragment;
    private boolean isInFullscreenMode;
    private MainActivity mainActivity;
    private Video savedVideo;
    private Video currentVideo;
    private boolean isInQualityMode;
    private EasyVideoCallback callbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.video_player, container, false);

        videoPlayerFragment = (VideoPlayerFragment) getFragmentManager()
                .findFragmentById(R.id.player_fragment);

        getUI(rootView);
        setupUI();

        return rootView;
    }

    private void setupUI() {

    }

    public void playNewVideo(EasyVideoCallback callbacks, final Video video) {
        this.callbacks = callbacks;

        savedVideo = null;

        try {
            savedVideo = Video.findByServerId(video.getServerId());
        } catch (Exception e) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
        }

        if (savedVideo != null) {
            this.currentVideo = savedVideo;

            if (this.currentVideo.getCurrentTime() > 0) {
                SuperToast.create(getActivity(),
                        getString(R.string.resuming_from_saved_time),
                        SuperToast.Duration.SHORT).show();
            }
        } else {
            this.currentVideo = video;
        }

        if (NetworkUtils.isConnectedWithWifi(getActivity())) {
            if (currentVideo.getVideoFile() != null) {
                setHighQuality();
            } else {
                setLowQuality();
            }
        }

        if (NetworkUtils.isConnected(getActivity()) && !NetworkUtils.isConnectedWithWifi(getActivity())) {
            if (currentVideo.getVideoFileLowRes() != null) {
                setLowQuality();
            } else {
                setHighQuality();
            }
        }


        setupPlayer(savedVideo);
        start();

        mainActivity.expandPanel();
    }

    public void setupPlayer(Video savedVideo) {
        player.setCallback(this);
        player.setAutoPlay(true);
        player.setThemeColorRes(R.color.colorPrimary);
        player.setHideControlsOnPlay(true);
        player.setLeftAction(EasyVideoPlayer.LEFT_ACTION_NONE);
        player.setRightAction(EasyVideoPlayer.RIGHT_ACTION_SUBMIT);
        player.setSubmitText(getString(R.string.fullscreen).toUpperCase());

        if (savedVideo != null) {
            player.setInitialPosition(savedVideo.getCurrentTime());
        }
    }

    public void toggleQuality() {
        saveCurrentVideoTime();

        if (isInQualityMode) {
            if (currentVideo.getVideoFileLowRes() != null) {
                setLowQuality();
            } else {
                SuperToast.create(getActivity(),
                        getString(R.string.quality_mode_not_available),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            if (currentVideo.getVideoFile() != null) {
                setHighQuality();
            } else {
                SuperToast.create(getActivity(),
                        getString(R.string.low_quality_mode_not_available),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setHighQuality() {
        player.setSource(Uri.parse(currentVideo.getVideoFile()));
        player.seekTo(currentVideo.getCurrentTime());
        isInQualityMode = true;
    }

    private void setLowQuality() {
        player.setSource(Uri.parse(currentVideo.getVideoFileLowRes()));
        player.seekTo(currentVideo.getCurrentTime());
        isInQualityMode = false;
    }

    private void getUI(ViewGroup rootView) {
        player = (EasyVideoPlayer) rootView.findViewById(R.id.player);
        buffering = (SpinKitView) rootView.findViewById(R.id.buffering);
    }

    private void showBuffering() {
        buffering.setVisibility(View.VISIBLE);
        if (buffering.getVisibility() != View.VISIBLE) {
            YoYo.with(Techniques.FadeIn).duration(150).playOn(buffering);
        }
    }

    private void hideBuffering() {
        YoYo.with(Techniques.FadeOut).duration(150).playOn(buffering);
        delay(new Runnable() {
            @Override
            public void run() {
                buffering.setVisibility(View.GONE);
            }
        }, 150);
    }

    @Override
    public void onStarted(EasyVideoPlayer player) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onStarted");

    }

    @Override
    public void onPaused(EasyVideoPlayer player) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onPaused");
        saveCurrentVideoTime();
    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onPreparing");
    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onPrepared");
        delay(new Runnable() {
            @Override
            public void run() {
                saveCurrentVideoTime();
                periodicalSaveTime();
            }
        }, PERIODICAL_SAVE_TIME);
    }

    @Override
    public void onBuffering(int percent) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onBuffering");
        if (player.isPlaying()) {
            hideBuffering();
        } else {
            showBuffering();
        }
    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onError");
    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onCompletion");
    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onRetry");
    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onSubmit");
        if (isInFullscreenMode) {
            isInFullscreenMode = false;
            exitFullscreen(mainActivity);
            videoPlayerFragment.showToolbar();
        } else {
            isInFullscreenMode = true;
            setFullscreen(mainActivity);
            videoPlayerFragment.hideToolbar();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onKeyDown(int keyCode) {
        super.onKeyDown(keyCode);
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public void hideControls() {
        if (player != null) {
            player.hideControls();
        }
    }

    public void release() {
        if (player != null) {
            player.release();
        }
    }

    public void seekTo(int time) {
        if (player != null) {
            player.seekTo(time);
        }
    }

    public void start() {
        if (player != null) {
            player.start();
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    public void stop() {
        if (player != null) {
            player.stop();
        }
    }

    public EasyVideoPlayer getPlayer() {
        return player;
    }

    private void periodicalSaveTime() {
        delay(new Runnable() {
            @Override
            public void run() {
                saveCurrentVideoTime();
                periodicalSaveTime();
            }
        }, PERIODICAL_SAVE_TIME);
    }

    public void saveCurrentVideoTime() {
        if (isAdded() && !videoPlayerFragment.getDismiss()) {
            if (player != null && currentVideo != null) {
                try {
                    currentVideo.setCurrentTime(player.getCurrentPosition());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                currentVideo.save();
            }
        }
    }

    public Video getVideo() {
        return currentVideo;
    }
}
