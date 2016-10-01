package com.chaemil.hgms.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
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
import com.chaemil.hgms.utils.GAUtils;
import com.chaemil.hgms.utils.NetworkUtils;
import com.chaemil.hgms.utils.OSUtils;
import com.chaemil.hgms.utils.ShareUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.ybq.android.spinkit.SpinKitView;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import ru.rambler.libs.swipe_layout.SwipeLayout;

/**
 * Created by chaemil on 2.12.15.
 */
public class VideoPlayerFragment extends BaseFragment implements View.OnClickListener, RequestFactoryListener, EasyVideoCallback
{

    public static final String TAG = "player_fragment";
    private static final String CURRENT_TIME = "current_time";
    private static final int PERIODICAL_SAVE_TIME = 5000;
    private RelativeLayout miniPlayer;
    private ImageView miniPlayerImageView;
    private RelativeLayout playerToolbar;
    private TextView miniPlayerText;
    private TextView playerTitle;
    private Video currentVideo;
    private ImageView miniPlayerPause;
    private ViewGroup rootView;
    public boolean isInFullscreenMode = false;
    public boolean isInQualityMode = false;
    private ImageView back;
    private ImageView share;
    private TextView description;
    private TextView tags;
    private RelativeLayout infoLayout;
    private RelativeLayout playerBgWrapper;
    private TimerTask timerTask;
    private Timer timer;
    private SwipeLayout miniPlayerSwipe;
    private MainActivity mainActivity;
    private ImageView info;
    private ImageView qualitySwitch;
    private EasyVideoPlayer player;
    private Video savedVideo;
    private boolean dismiss;
    private SpinKitView buffering;
    private RelativeLayout toolbarsWrapper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mainActivity = (MainActivity) getActivity();
    }


    @Override
    public void onPause() {
        super.onPause();

        if (OSUtils.isRunningNougat() && mainActivity.isInMultiWindowMode()) {
            return;
        } else {
            cancelFullscreenPlayer();
            player.pause();
            stopTimer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (OSUtils.isRunningNougat() && mainActivity.isInMultiWindowMode()) {
            return;
        } else {
            if (currentVideo != null) {
                player.seekTo(currentVideo.getCurrentTime());
            }

            player.showControls();

            miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play_dark));
            AnalyticsService.getInstance().setPage(AnalyticsService.Pages.VIDEOPLAYER_FRAGMENT);

            setupTimer();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.video_player_fragment, container, false);

        getUI(rootView);
        setupUI();

        return rootView;
    }

    private void setupTimer() {
        stopTimer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            postGA();
                        }
                    });
                } else {
                    stopTimer();
                }
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
        if (player.isPlaying()) {
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

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            player.seekTo(savedInstanceState.getInt(CURRENT_TIME));
        }
    }

    private void getUI(ViewGroup rootView) {
        miniPlayer = (RelativeLayout) rootView.findViewById(R.id.mini_player);
        miniPlayerImageView = (ImageView) rootView.findViewById(R.id.mini_player_image);
        miniPlayerText = (TextView) rootView.findViewById(R.id.mini_player_text);
        playerToolbar = (RelativeLayout) rootView.findViewById(R.id.toolbar);
        playerTitle = (TextView) rootView.findViewById(R.id.player_title);
        miniPlayerPause = (ImageView) rootView.findViewById(R.id.mini_play_pause);
        back = (ImageView) rootView.findViewById(R.id.back);
        share = (ImageView) rootView.findViewById(R.id.share);
        description = (TextView) rootView.findViewById(R.id.description);
        tags = (TextView) rootView.findViewById(R.id.tags);
        infoLayout = (RelativeLayout) rootView.findViewById(R.id.info_layout);
        playerBgWrapper = (RelativeLayout) rootView.findViewById(R.id.player_bg_wrapper);
        miniPlayerSwipe = (SwipeLayout) rootView.findViewById(R.id.mini_player_swipe);
        info = (ImageView) rootView.findViewById(R.id.info);
        qualitySwitch = (ImageView) rootView.findViewById(R.id.quality_switch);
        player = (EasyVideoPlayer) rootView.findViewById(R.id.player);
        buffering = (SpinKitView) rootView.findViewById(R.id.buffering);
        toolbarsWrapper = (RelativeLayout) rootView.findViewById(R.id.toolbars_wrapper);
    }

    private void setupUI() {
        miniPlayerPause.setOnClickListener(this);
        back.setOnClickListener(this);
        share.setOnClickListener(this);
        playerBgWrapper.setOnClickListener(this);
        miniPlayer.setOnClickListener(this);
        playerToolbar.setOnClickListener(this);
        info.setOnClickListener(this);
        qualitySwitch.setOnClickListener(this);
        miniPlayerImageView.setOnClickListener(this);
        miniPlayerText.setOnClickListener(this);

        miniPlayerSwipe.setOnSwipeListener(createSwipeListener());

        delay(new Runnable() {
            @Override
            public void run() {
                switchMiniPlayer(1);
            }
        }, 750);
    }

    public Video getCurrentVideo() {
        return currentVideo;
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

    private void toggleQuality() {
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
        qualitySwitch.setImageDrawable(getResources().getDrawable(R.drawable.ic_quality_white));
        isInQualityMode = true;
    }

    private void setLowQuality() {
        player.setSource(Uri.parse(currentVideo.getVideoFileLowRes()));
        player.seekTo(currentVideo.getCurrentTime());
        qualitySwitch.setImageDrawable(getResources().getDrawable(R.drawable.ic_quality_alpha));
        isInQualityMode = false;
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
        dismiss = true;
        player.release();

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
            case R.id.mini_play_pause:
                playPauseVideo();
                break;
            case R.id.back:
                ((MainActivity) getActivity()).collapsePanel();
                break;
            case R.id.share:
                ShareUtils.shareVideoLink(getActivity(), currentVideo);
                break;
            case R.id.info:
                toggleInfo();
                break;
            case R.id.quality_switch:
                toggleQuality();
                break;
            case R.id.mini_player_image:
            case R.id.mini_player_text:
                mainActivity.expandPanel();
                break;
        }
    }

    public void requestFullscreenPlayer() {
        ((BaseActivity) getActivity()).setFullscreen(true);

        getActivity().getWindow().getDecorView()
                .setBackgroundColor(getResources().getColor(R.color.black));

        miniPlayer.setVisibility(View.GONE);
        playerToolbar.setVisibility(View.GONE);
        infoLayout.setVisibility(View.GONE);
        toolbarsWrapper.setVisibility(View.GONE);

        player.hideControls();

        ((MainActivity) getActivity()).getMainRelativeLayout().setFitsSystemWindows(false);

        isInFullscreenMode = true;
    }

    public void cancelFullscreenPlayer() {
        ((BaseActivity) getActivity()).setFullscreen(false);

        getActivity().getWindow().getDecorView()
                .setBackgroundColor(getResources().getColor(R.color.white));

        miniPlayer.setVisibility(View.VISIBLE);
        playerToolbar.setVisibility(View.VISIBLE);
        infoLayout.setVisibility(View.GONE);
        toolbarsWrapper.setVisibility(View.VISIBLE);

        ((MainActivity) getActivity()).getMainRelativeLayout().setFitsSystemWindows(true);

        isInFullscreenMode = false;
    }

    private void playPauseVideo() {
        saveCurrentVideoTime();

        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.start();
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
        if (isAdded() && !dismiss) {
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

    private void setupPlayer() {
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

    private void periodicalSaveTime() {
        delay(new Runnable() {
            @Override
            public void run() {
                saveCurrentVideoTime();
                periodicalSaveTime();
            }
        }, PERIODICAL_SAVE_TIME);
    }

    public void playNewVideo(final Video video) {

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

        Ion.with(getActivity())
                .load(currentVideo.getThumbFile())
                .intoImageView(miniPlayerImageView);

        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause_dark));

        miniPlayerText.setText(video.getName());
        playerTitle.setText(video.getName());

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


        setupPlayer();
        player.start();

        mainActivity.expandPanel();

        AnalyticsService
                .getInstance()
                .setPage(AnalyticsService.Pages.VIDEOPLAYER_FRAGMENT + "videoHash: " + currentVideo.getHash());
        postVideoView();

        postGA();
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

    @Override
    public void onStarted(EasyVideoPlayer player) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onStarted");
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause_dark));
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onPaused");
        saveCurrentVideoTime();
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play_dark));
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
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play_dark));
    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onRetry");
    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onSubmit");
        if (isInFullscreenMode) {
            cancelFullscreenPlayer();
        } else {
            requestFullscreenPlayer();
        }
    }
}
