package com.chaemil.hgms.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.fragment.video_player.PlayerFragment;
import com.chaemil.hgms.fragment.video_player.QualitySwitchListener;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.GAUtils;
import com.chaemil.hgms.utils.ShareUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import ru.rambler.libs.swipe_layout.SwipeLayout;

/**
 * Created by chaemil on 2.12.15.
 */
public class VideoPlayerFragment extends BaseFragment implements View.OnClickListener,
        RequestFactoryListener, EasyVideoCallback, QualitySwitchListener {

    public static final String TAG = "player_fragment";
    private static final String CURRENT_TIME = "current_time";
    private RelativeLayout miniPlayer;
    private ImageView miniPlayerImageView;
    private RelativeLayout playerToolbar;
    private TextView miniPlayerText;
    private TextView playerTitle;
    private ImageView miniPlayerPause;
    private ViewGroup rootView;
    public boolean isInFullscreenMode = false;
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
    private boolean dismiss;
    private PlayerFragment playerFragment;
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

        stopTimer();
    }

    @Override
    public void onResume() {
        super.onResume();

        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.VIDEOPLAYER_FRAGMENT);

        setupTimer();
    }

    private PlayerFragment createPlayerFragment() {
        playerFragment = new PlayerFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.video_player, playerFragment);
        ft.commit();

        return playerFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.video_player_fragment, container, false);
        playerFragment = createPlayerFragment();

        getUI(rootView);
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
        if (playerFragment.isPlaying()) {
            GAUtils.sendGAScreen(
                    ((OazaApp) getActivity().getApplication()),
                    "VideoPlayer",
                    playerFragment.getVideo().getName());
        }
    }

    private void postVideoView() {
        JsonObjectRequest postView = RequestFactory.postVideoView(this, playerFragment.getVideo().getHash());
        RequestService.getRequestQueue().add(postView);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            playerFragment.seekTo(savedInstanceState.getInt(CURRENT_TIME));
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
        dismiss = true;
        playerFragment.release();

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
                ShareUtils.shareVideoLink(getActivity(), playerFragment.getVideo());
                break;
            case R.id.info:
                toggleInfo();
                break;
            case R.id.quality_switch:
                playerFragment.toggleQuality();
                break;
            case R.id.mini_player_image:
            case R.id.mini_player_text:
                mainActivity.expandPanel();
                break;
        }
    }

    private void playPauseVideo() {
        playerFragment.saveCurrentVideoTime();

        if (playerFragment != null) {
            if (playerFragment.getPlayer() != null) {
                if (playerFragment.isPlaying()) {
                    playerFragment.pause();
                } else {
                    playerFragment.start();
                }
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

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        switch(requestType) {
            case POST_VIDEO_VIEW:
                SmartLog.Log(SmartLog.LogLevel.DEBUG, "postedVideoView", "ok");
                break;
        }
    }

    public ImageView getMiniPlayerPause() {
        return miniPlayerPause;
    }

    public boolean getDismiss() {
        return dismiss;
    }

    public PlayerFragment getPlayerFragment() {
        return playerFragment;
    }

    public void playNewVideo(Video video) {
        if (playerFragment != null) {
            playerFragment.playNewVideo(this, video);

            miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause_dark));

            miniPlayerText.setText(video.getName());
            playerTitle.setText(video.getName());

            if (!video.getDescription().equals("")) {
                description.setText(video.getDescription());
            } else {
                description.setVisibility(View.GONE);
            }

            Ion.with(getActivity())
                    .load(video.getThumbFile())
                    .intoImageView(miniPlayerImageView);

            if (!video.getTags().equals("")) {
                String tagsString = "";
                for (String tag : video.getTags().split(",")) {
                    tagsString += "#" + tag.replace(" ","") + " ";
                }
                tags.setText(tagsString);
            } else {
                tags.setVisibility(View.GONE);
            }

            postVideoView();
            postGA();

            AnalyticsService
                    .getInstance()
                    .setPage(AnalyticsService.Pages.VIDEOPLAYER_FRAGMENT + "videoHash: " + video.getHash());
        }
    }

    @Override
    public void onStarted(EasyVideoPlayer player) {

    }

    @Override
    public void onPaused(EasyVideoPlayer player) {

    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {

    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {

    }

    @Override
    public void onBuffering(int percent) {

    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {

    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {

    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {

    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {

    }

    @Override
    public void onHighQualitySet() {
        qualitySwitch.setImageDrawable(getResources().getDrawable(R.drawable.ic_quality_white));
    }

    @Override
    public void onLowQualitySet() {
        qualitySwitch.setImageDrawable(getResources().getDrawable(R.drawable.ic_quality_alpha));
    }

    public void hideToolbar() {
        toolbarsWrapper.setVisibility(View.GONE);
    }

    public void showToolbar() {
        toolbarsWrapper.setVisibility(View.VISIBLE);
    }
}
