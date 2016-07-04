package com.chaemil.hgms.fragment;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

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
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.DimensUtils;
import com.chaemil.hgms.utils.GAUtils;
import com.chaemil.hgms.utils.ShareUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import at.markushi.ui.CircleButton;
import ru.rambler.libs.swipe_layout.SwipeLayout;

/**
 * Created by chaemil on 2.12.15.
 */
public class VideoPlayerFragment extends BaseFragment implements View.OnClickListener, View.OnTouchListener,
        MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener, RequestFactoryListener {

    public static final String TAG = "player_fragment";
    private static final String CURRENT_TIME = "current_time";
    private RelativeLayout miniPlayer;
    private ImageView miniPlayerImageView;
    private RelativeLayout playerToolbar;
    private TextView miniPlayerText;
    private TextView playerTitle;
    private VideoView videoView;
    private CircleButton playPause;
    private CircleButton rew;
    private TextView currentTime;
    private TextView totalTime;
    private int duration;
    private int currentTimeInt;
    private AppCompatSeekBar seekBar;
    private Bitmap thumb;
    private Video currentVideo;
    private CircleButton miniPlayerPause;
    private ProgressBar bufferBar;
    private int bufferFail;
    private RelativeLayout controlsWrapper;
    private RelativeLayout videoWrapper;
    private ViewGroup rootView;
    private ImageView fullscreen;
    private RelativeLayout.LayoutParams videoWrapperParamsFullscreen;
    private RelativeLayout.LayoutParams videoWrapperParamsNormal;
    public boolean isInFullscreenMode = false;
    private ImageView back;
    private ImageView share;
    private TextView description;
    private TextView tags;
    private RelativeLayout infoLayout;
    private ImageView fullscreenExit;
    private Timer hideControlsTimer;
    private int controlsTimeHide = 0;
    private Handler uiHandler;
    private RelativeLayout playerBgWrapper;
    private TimerTask timerTask;
    private Timer timer;
    private SwipeLayout miniPlayerSwipe;
    private MainActivity mainActivity;
    private ImageView info;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mainActivity = (MainActivity) getActivity();

        setupUIHandler();
    }


    @Override
    public void onPause() {
        super.onPause();

        videoView.pause();

        saveCurrentVideoTime();
        stopTimer();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (currentVideo != null) {
            videoView.seekTo(currentVideo.getCurrentTime());
        }

        playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play));

        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.VIDEOPLAYER_FRAGMENT);

        setupTimer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.video_player_fragment, container, false);

        getUI(rootView);
        activateUI(false);
        setupUI();

        return rootView;
    }

    private void setupUIHandler() {
        uiHandler = new Handler(Looper.getMainLooper());
        hideControlsTimer = new Timer();
        hideControlsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        controlsTimeHide += 1;
                        if (controlsTimeHide >= 5) {
                            toggleControls(false);
                        }
                    }
                };
                uiHandler.post(runnable);
            }
        }, 1000, 1000);
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
        if (videoView.isPlaying()) {
            GAUtils.sendGAScreen(
                    ((OazaApp) getActivity().getApplication()),
                    "VideoPlayer",
                    currentVideo.getNameCS());
        }
    }

    private void postVideoView() {
        JsonObjectRequest postView = RequestFactory.postVideoView(this, currentVideo.getHash());
        RequestService.getRequestQueue().add(postView);
    }

    private void activateUI(boolean state) {
        playPause.setEnabled(state);
        rew.setEnabled(state);
        seekBar.setEnabled(state);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            videoView.seekTo(savedInstanceState.getInt(CURRENT_TIME));
        }
    }

    private void getUI(ViewGroup rootView) {
        miniPlayer = (RelativeLayout) rootView.findViewById(R.id.mini_player);
        miniPlayerImageView = (ImageView) rootView.findViewById(R.id.mini_player_image);
        miniPlayerText = (TextView) rootView.findViewById(R.id.mini_player_text);
        playerToolbar = (RelativeLayout) rootView.findViewById(R.id.toolbar);
        playerTitle = (TextView) rootView.findViewById(R.id.player_title);
        videoView = (VideoView) rootView.findViewById(R.id.video_view);
        playPause = (CircleButton) rootView.findViewById(R.id.play_pause);
        rew = (CircleButton) rootView.findViewById(R.id.rew);
        currentTime = (TextView) rootView.findViewById(R.id.current_time);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        seekBar = (AppCompatSeekBar) rootView.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        miniPlayerPause = (CircleButton) rootView.findViewById(R.id.mini_play_pause);
        bufferBar = (ProgressBar) rootView.findViewById(R.id.buffer_bar);
        controlsWrapper = (RelativeLayout) rootView.findViewById(R.id.controls_wrapper);
        videoWrapper = (RelativeLayout) rootView.findViewById(R.id.video_wrapper);
        fullscreen = (ImageView) rootView.findViewById(R.id.fullscreen);
        fullscreenExit = (ImageView) rootView.findViewById(R.id.fullscreen_exit);
        back = (ImageView) rootView.findViewById(R.id.back);
        share = (ImageView) rootView.findViewById(R.id.share);
        description = (TextView) rootView.findViewById(R.id.description);
        tags = (TextView) rootView.findViewById(R.id.tags);
        infoLayout = (RelativeLayout) rootView.findViewById(R.id.info_layout);
        playerBgWrapper = (RelativeLayout) rootView.findViewById(R.id.player_bg_wrapper);
        miniPlayerSwipe = (SwipeLayout) rootView.findViewById(R.id.mini_player_swipe);
        info = (ImageView) rootView.findViewById(R.id.info);
    }

    private void setupUI() {
        playPause.setOnClickListener(this);
        rew.setOnClickListener(this);
        videoView.setOnTouchListener(this);
        videoView.setOnPreparedListener(this);
        miniPlayerPause.setOnClickListener(this);
        fullscreen.setOnClickListener(this);
        back.setOnClickListener(this);
        share.setOnClickListener(this);
        fullscreenExit.setOnClickListener(this);
        playerBgWrapper.setOnClickListener(this);
        miniPlayer.setOnClickListener(this);
        playerToolbar.setOnClickListener(this);
        info.setOnClickListener(this);

        miniPlayerSwipe.setOnSwipeListener(createSwipeListener());

        videoWrapperParamsNormal = (RelativeLayout.LayoutParams) videoWrapper.getLayoutParams();
        videoWrapperParamsNormal.setMargins(16, 16, 16,
                (int) getResources().getDimension(R.dimen.video_player_wrapper_bottom_margin));  // left, top, right, bottom
        videoWrapper.setLayoutParams(videoWrapperParamsNormal);

        videoWrapperParamsFullscreen = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        videoWrapperParamsFullscreen.setMargins(0, 0, 0, 0);  // left, top, right, bottom
    }

    private void showInfo() {
        infoLayout.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.SlideInDown).duration(300).playOn(infoLayout);
    }

    private void hideInfo() {
        YoYo.with(Techniques.SlideOutUp).duration(300).playOn(infoLayout);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                infoLayout.setVisibility(View.GONE);
            }
        }, 300);
    }

    private void toggleInfo() {
        if (infoLayout.getVisibility() == View.VISIBLE) {
            hideInfo();
        } else {
            showInfo();
        }
    }

    private SwipeLayout.OnSwipeListener createSwipeListener() {
        return new SwipeLayout.OnSwipeListener() {
            @Override
            public void onBeginSwipe(SwipeLayout swipeLayout, boolean moveToRight) {

            }

            @Override
            public void onSwipeClampReached(SwipeLayout swipeLayout, boolean moveToRight) {
                swipeDismissPlayer();
            }

            @Override
            public void onLeftStickyEdge(SwipeLayout swipeLayout, boolean moveToRight) {

            }

            @Override
            public void onRightStickyEdge(SwipeLayout swipeLayout, boolean moveToRight) {

            }
        };
    }

    private void swipeDismissPlayer() {
        saveCurrentVideoTime();
        videoView.pause();

        MainActivity mainActivity = ((MainActivity) getActivity());
        mainActivity.hidePanel();
        mainActivity.getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.play_pause:
                playPauseVideo();
                break;
            case R.id.rew:
                if (videoView.canSeekBackward()) {
                    videoView.seekTo(videoView.getCurrentPosition() - 30 * 1000);
                }
                break;
            case R.id.mini_play_pause:
                playPauseVideo();
                break;
            case R.id.fullscreen:
                requestFullscreenPlayer();
                hideInfo();
                break;
            case R.id.back:
                ((MainActivity) getActivity()).collapsePanel();
                break;
            case R.id.share:
                ShareUtils.shareVideoLink(getActivity(), currentVideo);
                break;
            case R.id.fullscreen_exit:
                cancelFullscreenPlayer();
                break;
            case R.id.player_bg_wrapper:
                toggleControls(true);
                resetHideControlsTimer();
                break;
            case R.id.mini_player:
            case R.id.toolbar:
                if (mainActivity != null && mainActivity.getPanelLayout() != null) {
                    switch(mainActivity.getPanelLayout().getPanelState()) {
                        case EXPANDED:
                            mainActivity.collapsePanel();
                            break;
                        case COLLAPSED:
                            mainActivity.expandPanel();
                            break;
                    }
                }
                break;
            case R.id.info:
                toggleInfo();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(v.getId()) {
            case R.id.video_view:
                toggleControls(true);
                resetHideControlsTimer();
                break;
        }

        return true;
    }

    private void resetHideControlsTimer() {
        controlsTimeHide = 0;
    }

    public void requestFullscreenPlayer() {
        ((BaseActivity) getActivity()).setFullscreen(true);

        getActivity().getWindow().getDecorView()
                .setBackgroundColor(getResources().getColor(R.color.black));

        playerToolbar.setVisibility(View.GONE);
        infoLayout.setVisibility(View.GONE);
        playerBgWrapper.setBackgroundColor(getResources().getColor(R.color.black));

        videoWrapper.setLayoutParams(videoWrapperParamsFullscreen);
        ((MainActivity) getActivity()).getMainRelativeLayout().setFitsSystemWindows(false);

        isInFullscreenMode = true;
        if (videoView.isPlaying()) {
            toggleControls(false);
        } else {
            toggleControls(true);
            fullscreenExit.setVisibility(View.VISIBLE);
        }

    }

    public void cancelFullscreenPlayer() {
        ((BaseActivity) getActivity()).setFullscreen(false);

        getActivity().getWindow().getDecorView()
                .setBackgroundColor(getResources().getColor(R.color.white));

        playerToolbar.setVisibility(View.VISIBLE);
        infoLayout.setVisibility(View.VISIBLE);
        playerBgWrapper.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDarkest));

        videoWrapper.setLayoutParams(videoWrapperParamsNormal);
        ((MainActivity) getActivity()).getMainRelativeLayout().setFitsSystemWindows(true);

        isInFullscreenMode = false;
        toggleControls(true);
    }

    public void toggleControls(boolean visible) {
        if (visible) {
            if (isInFullscreenMode) {
                if (controlsWrapper.getVisibility() != View.VISIBLE) {
                    controlsWrapper.setVisibility(View.VISIBLE);
                    fullscreenExit.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeInUp).duration(400).playOn(controlsWrapper);
                }
            } else {
                fullscreenExit.setVisibility(View.GONE);
            }
        } else {
            if (isInFullscreenMode && videoView.isPlaying()) {
                if (controlsWrapper.getVisibility() != View.GONE) {
                    YoYo.with(Techniques.FadeOutDown).duration(400).playOn(controlsWrapper);
                    YoYo.with(Techniques.FadeIn).duration(400).playOn(seekBar);
                    fullscreenExit.setVisibility(View.GONE);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            controlsWrapper.setVisibility(View.GONE);
                        }
                    }, 400);
                }
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        duration = videoView.getDuration();
        seekBar.setMax(duration);
        seekBar.postDelayed(onEverySecond, 1000);
        YoYo.with(Techniques.FadeIn).duration(350).delay(250).playOn(videoView);

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
            if (seekBar != null) {
                seekBar.setProgress(videoView.getCurrentPosition());
            }

            if (videoView.isPlaying()) {
                if (seekBar != null) {
                    seekBar.postDelayed(onEverySecond, 1000);
                }
                updateTime();
            }
        }
    };

    private void updateTime() {
        currentTimeInt = videoView.getCurrentPosition();

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

    private void playPauseVideo() {
        saveCurrentVideoTime();

        if (videoView.isPlaying()) {
            videoView.pause();
            playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
            miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
        } else {
            videoView.start();
            resetHideControlsTimer();
            playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            if (seekBar != null) {
                seekBar.postDelayed(onEverySecond, 1000);
            }
        }
    }



    public void switchMiniPlayer(float alpha) {
        if (isAdded()) {

            if (alpha == 0) {
                playerToolbar.setVisibility(View.GONE);
                miniPlayerSwipe.setVisibility(View.VISIBLE);
                miniPlayer.setVisibility(View.VISIBLE);
            } else if (alpha == 1) {
                playerToolbar.setVisibility(View.VISIBLE);
                miniPlayerSwipe.setVisibility(View.GONE);
                miniPlayer.setVisibility(View.GONE);
            } else {
                miniPlayerSwipe.setVisibility(View.VISIBLE);
                playerToolbar.setVisibility(View.VISIBLE);
                miniPlayer.setVisibility(View.VISIBLE);

                playerToolbar.setAlpha(alpha);
                miniPlayer.setAlpha(1 - alpha);
            }
        }
    }

    public void saveCurrentVideoTime() {
        if (videoView != null && currentVideo != null) {
            try {
                currentVideo.setCurrentTime(videoView.getCurrentPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentVideo.save();
        }
    }

    public void playNewVideo(final Video video) {

        saveCurrentVideoTime();

        Video savedVideo = null;

        try {
            savedVideo = Video.findByServerId(video.getServerId());
        } catch (Exception e) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
        }

        if (savedVideo != null) {
            this.currentVideo = savedVideo;

            SuperToast.create(getActivity(),
                    getString(R.string.resuming_from_saved_time),
                    SuperToast.Duration.SHORT).show();
        } else {
            this.currentVideo = video;
        }

        Ion.with(getActivity())
                .load(currentVideo.getThumbFile())
                .intoImageView(miniPlayerImageView);

        playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

        miniPlayerText.setText(video.getName());
        playerTitle.setText(video.getName());
        videoView.setAlpha(0);

        currentTime.setText("00:00:00");
        totalTime.setText("???");
        if (!currentVideo.getDescription().equals("")) {
            description.setText(currentVideo.getDescription());
        } else {
            description.setVisibility(View.GONE);
        }
        if (!currentVideo.getTags().equals("")) {
            String tagsString = "";
            for (String tag : currentVideo.getTags().split(",")) {
                tagsString += "#" + tag.replace(" ","") + " ";
            }
            tags.setText(tagsString);
        } else {
            tags.setVisibility(View.GONE);
        }

        ((MainActivity) getActivity()).expandPanel();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                activateUI(true);
                videoView.stopPlayback();
                videoView.setVideoPath(video.getVideoFileLowRes()); //TODO lowres as default for now
                videoView.start();
                videoView.seekTo(currentVideo.getCurrentTime());

            }
        }, 500);

        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.VIDEOPLAYER_FRAGMENT + "videoHash: " + currentVideo.getHash());
        postVideoView();

        postGA();
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
            videoView.seekTo(progress);
            updateTime();
            saveCurrentVideoTime();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

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
        BaseActivity.responseError(exception, getActivity());
    }
}
