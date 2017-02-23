package com.chaemil.hgms.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.ArchiveAdapter;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.GAUtils;
import com.chaemil.hgms.utils.OSUtils;
import com.chaemil.hgms.utils.ShareUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.chaemil.hgms.utils.StringUtils;
import com.chaemil.hgms.utils.ViewUtils;
import com.chaemil.hgms.utils.subtitles.Caption;
import com.chaemil.hgms.utils.subtitles.FormatASS;
import com.chaemil.hgms.utils.subtitles.TimedTextObject;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.ion.Ion;

import net.soulwolf.widget.ratiolayout.widget.RatioFrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import link.fls.BoundLayout;
import ru.rambler.libs.swipe_layout.SwipeLayout;

/**
 * Created by chaemil on 2.12.15.
 */
public class VideoPlayerFragment extends BaseFragment implements View.OnClickListener,
        RequestFactoryListener, EasyVideoCallback {

    public static final String TAG = "player_fragment";
    private static final String CURRENT_TIME = "current_time";
    private static final int PERIODICAL_SAVE_TIME = 10 * 1000;
    private static final String HIGH_QUALITY = "high_quality";
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
    private LinearLayout shareWrapper;
    private WebView description;
    private TextView tags;
    private RelativeLayout playerBgWrapper;
    private TimerTask timerTask;
    private Timer timer;
    private SwipeLayout miniPlayerSwipe;
    private MainActivity mainActivity;
    private ImageView qualitySwitch;
    private EasyVideoPlayer player;
    private Video savedVideo;
    private boolean dismiss;
    private RelativeLayout toolbarsWrapper;
    private boolean qualityToggle = false;
    private TextView subtitles;
    private TimedTextObject srt;
    private TextView dateText;
    private NestedScrollView infoWrapper;
    private RelativeLayout videoWrapper;
    private RatioFrameLayout playerRatioWrapper;
    private WebView descriptionTablet;
    private NestedScrollView tabletRightScroll;
    private BoundLayout actionsBoundWrapper;
    private BoundLayout tagsBoundWrapper;
    private LinearLayout infoLinearLayout;
    private ImageView downloadedView;
    private TextView downloadedText;
    private LinearLayout downloadedWrapper;
    private BroadcastReceiver downloadChangeReceiver;
    private RecyclerView similarVideosView;
    private RecyclerView similarVideosTabletView;
    private ArchiveAdapter similarVideosAdapter;
    private ArchiveAdapter similarVideosTabletAdapter;
    private CardView descriptionCard;
    private CardView descriptionTabletCard;
    private TextView viewsText;
    private ArrayList<Video> similarVideos = new ArrayList<>();
    private Handler subtitleDisplayHandler = new Handler();
    private Runnable subtitleProcessor = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying()) {
                int currentPos = player.getCurrentPosition();
                Collection<Caption> subtitles = srt.captions.values();
                for (Caption caption : subtitles) {
                    if (currentPos >= caption.start.mseconds
                            && currentPos <= caption.end.mseconds) {
                        onTimedText(caption);
                        break;
                    } else if (currentPos > caption.end.mseconds) {
                        onTimedText(null);
                    }
                }
            }
            subtitleDisplayHandler.postDelayed(this, 100);
            adjustSubtitlesPosition();
        }
    };

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mainActivity = (MainActivity) getActivity();
    }


    @Override
    public void onPause() {
        super.onPause();

        saveCurrentVideoTime();

        if (OSUtils.isRunningNougat() && getActivity().isInMultiWindowMode()
                || OSUtils.isRunningChromeOS(getActivity())) {
            return;
        } else {
            unregisterDownloadChangeReceiver();
            cancelFullscreenPlayer();
            player.pause();
            stopTimer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        adjustOrientation(false);

        createDownloadChangeReceiver();

        if (OSUtils.isRunningNougat() && getActivity().isInMultiWindowMode()
                || OSUtils.isRunningChromeOS(getActivity())) {
            return;
        } else {
            if (currentVideo != null) {
                player.seekTo(currentVideo.getCurrentTime());

                AnalyticsService.getInstance()
                        .setPage(AnalyticsService.Pages.VIDEOPLAYER_FRAGMENT + "audioHash: "
                                + currentVideo.getHash());
                GAUtils.sendGAScreen(
                        ((OazaApp) getActivity().getApplication()),
                        "VideoPlayer",
                        currentVideo.getName());
            }

            player.showControls();

            miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play_dark));

            setupTimer();
        }
    }

    private void createDownloadChangeReceiver() {
        unregisterDownloadChangeReceiver();

        if (downloadChangeReceiver == null) {
            downloadChangeReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateDownloadedButton();
                }
            };
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadedFragment.DOWNLOAD_MANAGER_ONCHANGE);
        filter.addAction(Video.NOTIFY_AUDIO_DELETE);

        getActivity().registerReceiver(downloadChangeReceiver, filter);
    }

    private void unregisterDownloadChangeReceiver() {
        if (downloadChangeReceiver != null) {
            try {
                getActivity().unregisterReceiver(downloadChangeReceiver);
                downloadChangeReceiver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adjustFullscreen();
        adjustTabletLayout();
    }

    public void adjustFullscreen() {
        if (!isTablet(getActivity())) {
            switch (getScreenOrientation(getActivity())) {
                case Configuration.ORIENTATION_PORTRAIT:
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    if (mainActivity.isPanelExpanded()) {
                        requestFullscreenPlayer();
                    }
                    break;
            }
        }
    }

    private void adjustOrientation(boolean exitingFullscreen) {
        if (!isTablet(getActivity())) {
            if (isInFullscreenMode) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                if (exitingFullscreen) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    delay(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {
                                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                            }
                        }
                    }, 2000);
                } else {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                }
            }
        }
    }

    public void exit() {
        mainActivity = null;
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
        shareWrapper = (LinearLayout) rootView.findViewById(R.id.share_wrapper);
        description = (WebView) rootView.findViewById(R.id.description);
        tags = (TextView) rootView.findViewById(R.id.tags);
        playerBgWrapper = (RelativeLayout) rootView.findViewById(R.id.player_bg_wrapper);
        miniPlayerSwipe = (SwipeLayout) rootView.findViewById(R.id.mini_player_swipe);
        qualitySwitch = (ImageView) rootView.findViewById(R.id.quality_switch);
        player = (EasyVideoPlayer) rootView.findViewById(R.id.player);
        toolbarsWrapper = (RelativeLayout) rootView.findViewById(R.id.toolbars_wrapper);
        subtitles = (TextView) rootView.findViewById(R.id.subtitles);
        dateText = (TextView) rootView.findViewById(R.id.date_text);
        infoWrapper = (NestedScrollView) rootView.findViewById(R.id.info_wrapper);
        videoWrapper = (RelativeLayout) rootView.findViewById(R.id.video_wrapper);
        playerRatioWrapper = (RatioFrameLayout) rootView.findViewById(R.id.player_ratio_wrapper);
        tabletRightScroll = (NestedScrollView) rootView.findViewById(R.id.tablet_right_scroll);
        descriptionTablet = (WebView) rootView.findViewById(R.id.description_tablet);
        actionsBoundWrapper = (BoundLayout) rootView.findViewById(R.id.actions_bound_wrapper);
        tagsBoundWrapper = (BoundLayout) rootView.findViewById(R.id.tags_bound_wrapper);
        infoLinearLayout = (LinearLayout) rootView.findViewById(R.id.info_linear_layout);
        downloadedView = (ImageView) rootView.findViewById(R.id.downloaded);
        downloadedText = (TextView) rootView.findViewById(R.id.downloaded_text);
        downloadedWrapper = (LinearLayout) rootView.findViewById(R.id.downloaded_wrapper);
        similarVideosView = (RecyclerView) rootView.findViewById(R.id.similar_videos);
        similarVideosTabletView = (RecyclerView) rootView.findViewById(R.id.similar_videos_tablet);
        descriptionCard = (CardView) rootView.findViewById(R.id.description_card);
        descriptionTabletCard = (CardView) rootView.findViewById(R.id.description_tablet_card);
        viewsText = (TextView) rootView.findViewById(R.id.views_text);
    }

    private void setupUI() {
        miniPlayerPause.setOnClickListener(this);
        back.setOnClickListener(this);
        shareWrapper.setOnClickListener(this);
        playerBgWrapper.setOnClickListener(this);
        miniPlayer.setOnClickListener(this);
        playerToolbar.setOnClickListener(this);
        qualitySwitch.setOnClickListener(this);
        miniPlayerImageView.setOnClickListener(this);
        miniPlayerText.setOnClickListener(this);
        downloadedWrapper.setOnClickListener(this);

        miniPlayerSwipe.setOnSwipeListener(createSwipeListener());

        setupSimilarVideosLayout();

        delay(new Runnable() {
            @Override
            public void run() {
                switchMiniPlayer(1);
            }
        }, 750);
    }

    private void setupSimilarVideosLayout() {
        similarVideosAdapter = new ArchiveAdapter(getActivity(), similarVideos, R.layout.archive_item);
        similarVideosView.setLayoutManager(new LinearLayoutManager(getActivity()));
        similarVideosView.setNestedScrollingEnabled(false);
        similarVideosView.setAdapter(similarVideosAdapter);

        similarVideosTabletAdapter = new ArchiveAdapter(getActivity(), similarVideos, R.layout.archive_item);
        similarVideosTabletView.setAdapter(similarVideosTabletAdapter);
        similarVideosTabletView.setNestedScrollingEnabled(false);
        similarVideosTabletView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    public Video getCurrentVideo() {
        return currentVideo;
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
        if (currentVideo != null && currentVideo.getVideoFile() != null) {
            qualityToggle = true;
            player.disableControls();
            player.setSource(Uri.parse(currentVideo.getVideoFile()));
            qualitySwitch.setImageDrawable(getResources().getDrawable(R.drawable.ic_quality_white));
            isInQualityMode = true;
        } else {
            mainActivity.hidePanel();
            SuperToast.create(getActivity(),
                    getString(R.string.error_when_playing_video),
                    SuperToast.Duration.SHORT).show();
        }
    }

    private void setLowQuality() {
        if (currentVideo != null && currentVideo.getVideoFileLowRes() != null) {
            qualityToggle = true;
            player.disableControls();
            player.setSource(Uri.parse(currentVideo.getVideoFileLowRes()));
            qualitySwitch.setImageDrawable(getResources().getDrawable(R.drawable.ic_quality_alpha));
            isInQualityMode = false;
        } else {
            mainActivity.hidePanel();
            SuperToast.create(getActivity(),
                    getString(R.string.error_when_playing_video),
                    SuperToast.Duration.SHORT).show();
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
        player.release();

        MainActivity mainActivity = ((MainActivity) getActivity());
        mainActivity.hidePanel();
        mainActivity.getFragmentManager().beginTransaction().remove(this).commit();
        mainActivity.setVideoPlayerFragment(null);
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
            case R.id.share_wrapper:
                ShareUtils.shareVideoLink(getActivity(), currentVideo);
                break;
            case R.id.quality_switch:
                toggleQuality();
                break;
            case R.id.downloaded_wrapper:
                unregisterDownloadChangeReceiver();
                AdapterUtils.downloadAudio(getActivity(), getCurrentVideo());
                delay(new Runnable() {
                    @Override
                    public void run() {
                        createDownloadChangeReceiver();
                    }
                }, 2000);
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

        ((MainActivity) getActivity()).getMainRelativeLayout().setFitsSystemWindows(false);

        miniPlayer.setVisibility(View.GONE);
        playerToolbar.setVisibility(View.GONE);
        infoWrapper.setVisibility(View.GONE);
        toolbarsWrapper.setVisibility(View.GONE);
        videoWrapper.setLayoutParams(new RelativeLayout
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        videoWrapper.setFitsSystemWindows(false);
        player.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        playerRatioWrapper.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        player.hideControls();

        isInFullscreenMode = true;

        adjustOrientation(false);
        adjustTabletLayout();
    }

    public void cancelFullscreenPlayer() {
        if (getActivity() != null) {
            ((BaseActivity) getActivity()).setFullscreen(false);

            getActivity().getWindow().getDecorView()
                    .setBackgroundColor(getResources().getColor(R.color.fragment_bg));

            ((MainActivity) getActivity()).getMainRelativeLayout().setFitsSystemWindows(true);
        }

        player.showControls();
        miniPlayer.setVisibility(View.VISIBLE);
        playerToolbar.setVisibility(View.VISIBLE);
        infoWrapper.setVisibility(View.VISIBLE);
        toolbarsWrapper.setVisibility(View.VISIBLE);
        videoWrapper.setLayoutParams(new RelativeLayout
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        videoWrapper.setFitsSystemWindows(true);
        player.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        playerRatioWrapper.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        isInFullscreenMode = false;

        adjustOrientation(true);
        adjustTabletLayout();
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
            if (player != null && currentVideo != null && !qualityToggle) {
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

    private void adjustSubtitlesPosition() {
        if (player.isControlsShown()) {
            int height = (int) getResources().getDimension(R.dimen.video_player_controls_height);
            ViewUtils.setMargins(getActivity(), subtitles, 0, 0, 0, height);
        } else {
            ViewUtils.setMargins(getActivity(), subtitles, 0, 0, 0, 8);
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

    public void playNewVideo(final Video video, boolean quality) {

        isInQualityMode = quality;
        savedVideo = null;

        try {
            savedVideo = Video.findByServerId(video.getServerId());
        } catch (Exception e) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
        }

        if (savedVideo != null) {
            this.currentVideo = savedVideo;
        } else {
            this.currentVideo = video;
        }

        Ion.with(getActivity())
                .load(currentVideo.getThumbFile())
                .intoImageView(miniPlayerImageView);

        setupPlayerUI(video);
        updateDownloadedButton();
        setupDescriptionView();
        setupTagsView();

        if (isInQualityMode) {
            setHighQuality();
        } else {
            setLowQuality();
        }

        setupPlayer();
        player.start();

        mainActivity.expandPanel();

        adjustTabletLayout();

        delay(new Runnable() {
            @Override
            public void run() {
                adjustFullscreen();
            }
        }, 750);

        getSimilarVideos(video);

        AnalyticsService
                .getInstance()
                .setPage(AnalyticsService.Pages.VIDEOPLAYER_FRAGMENT + "videoHash: " + currentVideo.getHash());
        postVideoView();

        postGA();
    }

    private void getSimilarVideos(Video video) {
        Request getSimilarVideos = RequestFactory.getSimilarVideos(video.getHash(), this);
        RequestService.getRequestQueue().add(getSimilarVideos);
    }

    private void setupPlayerUI(Video video) {
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause_dark));

        miniPlayerText.setText(video.getName());
        playerTitle.setText(video.getName());
        dateText.setText(StringUtils.formatDate(video.getDate(), getActivity()));
        viewsText.setText(video.getViews() + " " + getActivity().getString(R.string.views));
    }

    private void setupTagsView() {
        if (!currentVideo.getTags().equals("")) {
            String tagsString = "";
            for (String tag : currentVideo.getTags().split(",")) {
                tagsString += "#" + tag.replace(" ","") + " ";
            }
            tags.setText(tagsString);
        } else {
            tags.setVisibility(View.GONE);
        }
    }

    private void setupDescriptionView() {
        if (!currentVideo.getDescription().equals("")) {
            description.loadData(currentVideo.getDescription(), "text/html; charset=utf-8", "UTF-8");
            descriptionTablet.loadData(currentVideo.getDescription(), "text/html; charset=utf-8", "UTF-8");
        } else {
            description.setVisibility(View.GONE);
            descriptionTablet.setVisibility(View.GONE);
        }
    }

    public void updateDownloadedButton() {
        if (getCurrentVideo().isAudioDownloaded(getActivity())) {
            downloadedText.setText(getString(R.string.downloaded));
            downloadedView.setImageDrawable(getResources().getDrawable(R.drawable.ic_downloaded));
        } else {
            downloadedText.setText(getString(R.string.download_audio));
            downloadedView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_cloud_download));
        }
    }

    public void adjustTabletLayout() {
        if (isTablet(getActivity())) {
            if (isInFullscreenMode) {
                tabletRightScroll.setVisibility(View.GONE);
            } else {
                switch (getScreenOrientation(getActivity())) {
                    case Configuration.ORIENTATION_LANDSCAPE:
                        adjustTabletLayoutLandscape();
                        break;
                    case Configuration.ORIENTATION_PORTRAIT:
                        adjustTabletLayoutPortrait();
                        break;
                }
            }
        } else {
            tabletRightScroll.setVisibility(View.GONE);
            if (isDescriptionPresent()) {
                description.setVisibility(View.VISIBLE);
                descriptionCard.setVisibility(View.VISIBLE);
            } else {
                description.setVisibility(View.GONE);
                descriptionCard.setVisibility(View.GONE);
            }
        }
    }

    private boolean isDescriptionPresent() {
        return currentVideo.getDescription() != null
                && currentVideo.getDescription().trim().length() > 0;
    }

    private void adjustTabletLayoutPortrait() {
        tabletRightScroll.setVisibility(View.GONE);
        similarVideosView.setVisibility(View.VISIBLE);
        if (isDescriptionPresent()) {
            description.setVisibility(View.VISIBLE);
            descriptionCard.setVisibility(View.VISIBLE);
        } else {
            description.setVisibility(View.GONE);
            descriptionCard.setVisibility(View.GONE);
        }
    }

    private void adjustTabletLayoutLandscape() {
        tabletRightScroll.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = new RelativeLayout
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.LEFT_OF, R.id.tablet_right_scroll);
        params.addRule(RelativeLayout.BELOW, R.id.toolbars_wrapper);
        videoWrapper.setLayoutParams(params);
        description.setVisibility(View.GONE);
        if (isDescriptionPresent()) {
            descriptionTablet.setVisibility(View.VISIBLE);
            descriptionTabletCard.setVisibility(View.VISIBLE);
        } else {
            descriptionTablet.setVisibility(View.GONE);
            descriptionTabletCard.setVisibility(View.GONE);
        }
        similarVideosView.setVisibility(View.GONE);
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        switch(requestType) {
            case POST_VIDEO_VIEW:
                SmartLog.Log(SmartLog.LogLevel.DEBUG, "postedVideoView", "ok");
                break;
            case GET_SIMILAR_VIDEOS:
                if (response != null) {
                    SmartLog.Log(SmartLog.LogLevel.DEBUG, "getSimilarVideos", response.toString());
                    try {
                        similarVideos.addAll(ResponseFactory.parseVideos(response.getJSONArray(Constants.JSON_VIDEOS)));
                        similarVideosAdapter.notifyDataSetChanged();
                        similarVideosTabletAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
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
        adjustSubtitlesPosition();
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onPaused");
        saveCurrentVideoTime();
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play_dark));
        adjustSubtitlesPosition();
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

        if (qualityToggle) {
            player.seekTo(currentVideo.getCurrentTime());
            qualityToggle = false;
            player.enableControls(true);
        }

        if (currentVideo.getSubtitlesFile() != null) {
            setupSubtitles();
        } else {
            subtitles.setVisibility(View.GONE);
        }
    }

    private void setupSubtitles() {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... unused) {
                try {
                    downloadSubtitlesFile();
                    InputStream stream = new FileInputStream(getExternalFile());
                    FormatASS formatASS = new FormatASS();
                    srt = formatASS.parseFile("subtitles.ass", stream);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "error in downloading subs");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (null != srt) {
                    subtitles.setText("");
                    subtitleDisplayHandler.post(subtitleProcessor);
                }
                super.onPostExecute(result);
            }
        }.execute();
    }

    private void downloadSubtitlesFile() throws IOException {
        int count;
        URL url = new URL(currentVideo.getSubtitlesFile());
        InputStream is = url.openStream();
        File f = getExternalFile();
        FileOutputStream fos = new FileOutputStream(f);
        byte data[] = new byte[1024];
        while ((count = is.read(data)) != -1) {
            fos.write(data, 0, count);
        }
        is.close();
        fos.close();
    }

    public File getExternalFile() {
        File srt = null;
        try {
            srt = new File(getActivity().getExternalCacheDir().getPath()
                    + "/" + currentVideo.getHash() + ".srt");
            srt.createNewFile();
            return srt;
        } catch (Exception e) {
            Log.e(TAG, "exception in file creation");
        }
        return null;
    }

    public void onTimedText(Caption text) {
        if (text == null || text.toString().equals("")) {
            subtitles.setVisibility(View.INVISIBLE);
            return;
        }
        subtitles.setText(Html.fromHtml(text.content));
        subtitles.setVisibility(View.VISIBLE);
    }

    public boolean isCurrentlyPlaying() {
        if (player != null) {
            return player.isPlaying();
        }
        return false;
    }

    @Override
    public void onBuffering(int percent) {
        SmartLog.Log(SmartLog.LogLevel.DEBUG, "player", "onBuffering");
        if (player.isPlaying()) {

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
