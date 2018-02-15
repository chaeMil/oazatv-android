package com.chaemil.hgms.ui.mobile.activity;

import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.ui.mobile.fragment.ArchiveFragment;
import com.chaemil.hgms.ui.mobile.fragment.AudioPlayerFragment;
import com.chaemil.hgms.ui.mobile.fragment.CategoriesFragment;
import com.chaemil.hgms.ui.mobile.fragment.CategoryFragment;
import com.chaemil.hgms.ui.mobile.fragment.DownloadedFragment;
import com.chaemil.hgms.ui.mobile.fragment.HomeFragment;
import com.chaemil.hgms.ui.mobile.fragment.MainFragment;
import com.chaemil.hgms.ui.mobile.fragment.PhotoAlbumFragment;
import com.chaemil.hgms.ui.mobile.fragment.SongsFragment;
import com.chaemil.hgms.ui.mobile.fragment.VideoPlayerFragment;
import com.chaemil.hgms.model.LiveStream;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.receiver.AudioPlaybackReceiver;
import com.chaemil.hgms.service.AudioPlaybackService;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.DimensUtils;
import com.chaemil.hgms.utils.NetworkUtils;
import com.chaemil.hgms.utils.SharedPrefUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.chaemil.hgms.ui.view.LockableBottomSheetBehaviour;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.gson.JsonObject;
import com.iamhabib.ratingrequestlibrary.RatingRequest;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chaemil on 2.12.15.
 */
public class MainActivity extends BaseActivity {

    public static final String EXPAND_PANEL = "expand_panel";
    private VideoPlayerFragment videoPlayerFragment;
    private AudioPlayerFragment audioPlayerFragment;
    private MainFragment mainFragment;
    private RelativeLayout mainRelativeLayout;
    private ConnectivityManager connManager;
    private NetworkInfo wifi;
    private SharedPrefUtils sharedPreferences;
    private RelativeLayout playerWrapper;
    private RelativeLayout statusMessageWrapper;
    private TextView statusMessageText;
    private Timer liveRequestTimer;
    private LiveStream liveStream;
    private TextView liveStreamMessageWatch;
    private boolean deepLink;
    public Intent playAudioIntent;
    private BottomSheetBehavior mBottomSheetBehavior;
    private RelativeLayout mainFragmentWrapper;
    private RelativeLayout playerBgBlack;
    private HomeFragment homeFragment;
    private CategoriesFragment categoriesFragment;
    private ArchiveFragment archiveFragment;
    private DownloadedFragment downloadedFragment;
    private PhotoAlbumFragment photoAlbumFragment;
    private SongsFragment songsFragment;
    private BroadcastReceiver networkStateReceiver;
    public boolean categoryVisible = false;
    public boolean songVisible = false;
    private MaterialDialog contextDialog;
    private BroadcastReceiver audioDownloadedDeletedReceiver;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private CategoryFragment categoryFragment;

    @Override
    protected void onResume() {
        super.onResume();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

        adjustLayout();
        parseDeepLink();

        setupNetworkStateReceiver();
        setupAudioDownloadedDeletedReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterAudioDownloadedDeletedReceiver();
        unregisterNetworkStateReceiver();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        reconnectToPlaybackService();
        adjustLayout();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        sharedPreferences = SharedPrefUtils.getInstance(this);

        ((OazaApp) getApplication()).setMainActivity(this);

        getUI();
        setupUI(savedInstanceState);
        setupPlaybackReceiver();
        setupLiveRequestTimer();
        createFragments();
        ratingRequest();
        initTracker();

        if (getIntent().getBooleanExtra(EXPAND_PANEL, false)) {
            expandPanel();
        }
    }

    private void ratingRequest() {
        RatingRequest.with(this)
                .scheduleAfter(5)
                .agreeButtonText(getString(R.string.sure))
                .laterButtonText(getString(R.string.later))
                .doneButtonText(getString(R.string.already_done))
                .backgroundResource(R.color.colorPrimary)
                .message(getString(R.string.rating_request_message))
                .listener(new RatingRequest.ClickListener() {
                    @Override
                    public void onAgreeButtonClick() {

                    }

                    @Override
                    public void onDoneButtonClick() {

                    }

                    @Override
                    public void onLaterButtonClick() {

                    }
                })
                .cancelable(false)
                .delay(5 * 1000)
                .register();
    }

    private void createFragments() {
        homeFragment = new HomeFragment();
        categoriesFragment = new CategoriesFragment();
        archiveFragment = new ArchiveFragment();
        downloadedFragment = new DownloadedFragment();
        photoAlbumFragment = new PhotoAlbumFragment();
        songsFragment = new SongsFragment();
    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }

    public CategoriesFragment getCategoriesFragment() {
        return categoriesFragment;
    }

    public ArchiveFragment getArchiveFragment() {
        return archiveFragment;
    }

    public DownloadedFragment getDownloadedFragment() {
        return downloadedFragment;
    }

    public PhotoAlbumFragment getPhotoAlbumFragment() {
        return photoAlbumFragment;
    }

    public SongsFragment getSongsFragment() {
        return songsFragment;
    }

    public void setCategoryFragment(CategoryFragment categoryFragment) {
        this.categoryFragment = categoryFragment;
    }

    public CategoryFragment getCategoryFragment() {
        return categoryFragment;
    }

    private void parseDeepLink() {
        Intent intent = getIntent();
        Uri data = intent.getData();
        deepLink = true;

        if (data != null) {
            SmartLog.e("data", data.toString());
            String path = data.getPath();
            String[] pathArray = path.split("/");

            if (path.contains("/video/watch")) {
                String videoHash = pathArray[pathArray.length - 1];
                Request getVideo = RequestFactory.getVideo(this, videoHash);
                RequestService.getRequestQueue().add(getVideo);
            }

            if (path.contains("/album/view")) {
                String albumHash = pathArray[pathArray.length - 1];
                Request getAlbum = RequestFactory.getPhotoAlbum(this, albumHash);
                RequestService.getRequestQueue().add(getAlbum);
            }
        }
    }

    private void setupPlaybackReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioPlaybackReceiver.NOTIFY_PLAY_PAUSE);
        filter.addAction(AudioPlaybackReceiver.NOTIFY_OPEN);
        filter.addAction(AudioPlaybackReceiver.NOTIFY_FF);
        filter.addAction(AudioPlaybackReceiver.NOTIFY_REW);
        filter.addAction(AudioPlaybackReceiver.NOTIFY_DELETE);
    }

    public void setupLiveRequestTimer() {
        if (liveRequestTimer != null) {
            liveRequestTimer.cancel();
        }
        if (NetworkUtils.isConnected(this)) {
            liveRequestTimer = new Timer();
            liveRequestTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    getLiveStream();
                }
            }, 0, 30 * 1000);
        } else {
            noConnectionMessage();
        }
    }

    public void noConnectionMessage() {
        showStatusMessage(getString(R.string.offline_status_message),
                getResources().getColor(R.color.md_red_800), false);
    }

    private void getLiveStream() {
        JsonObjectRequest request = RequestFactory.getLiveStream(MainActivity.this);
        RequestService.getRequestQueue().add(request);
    }

    @Override
    protected void onDestroy() {
        ((OazaApp) getApplication()).setMainActivity(null);
        exit();
        super.onDestroy();
    }

    public void bringToFront() {
        Intent i = new Intent(getBaseContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
        getBaseContext().startActivity(i);
    }

    private void getUI() {
        mainRelativeLayout = (RelativeLayout) findViewById(R.id.main_relative_layout);
        mainFragmentWrapper = (RelativeLayout) findViewById(R.id.main_layout);
        playerWrapper = (RelativeLayout) findViewById(R.id.player_wrapper);
        statusMessageWrapper = (RelativeLayout) findViewById(R.id.status_message_wrapper);
        statusMessageText = (TextView) findViewById(R.id.status_message_text);
        liveStreamMessageWatch = (TextView) findViewById(R.id.watch);
        mBottomSheetBehavior = BottomSheetBehavior.from(playerWrapper);
        playerBgBlack = (RelativeLayout) findViewById(R.id.player_bg_black);
    }

    private void setupBottomSheet() {
        mBottomSheetBehavior.setPeekHeight(DimensUtils.getActionBarHeight(this));
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if (mBottomSheetBehavior instanceof LockableBottomSheetBehaviour) {
                            ((LockableBottomSheetBehaviour) mBottomSheetBehavior).setLocked(false);
                        }
                        adjustLayout();
                        setFullscreen(false);
                        if (getVideoPlayerFragment() != null) {
                            getVideoPlayerFragment().cancelFullscreenPlayer();
                        }

                        layoutParams.bottomMargin = DimensUtils.getActionBarHeight(MainActivity.this);
                        mainFragmentWrapper.setLayoutParams(layoutParams);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        if (mBottomSheetBehavior instanceof LockableBottomSheetBehaviour) {
                            ((LockableBottomSheetBehaviour) mBottomSheetBehavior).setLocked(true);
                        }
                        adjustLayout();
                        if (getVideoPlayerFragment() != null) {
                            getVideoPlayerFragment().adjustFullscreen();
                            getVideoPlayerFragment().adjustTabletLayout();
                        }
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        layoutParams.bottomMargin = 0;
                        mainFragmentWrapper.setLayoutParams(layoutParams);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        if (mBottomSheetBehavior instanceof LockableBottomSheetBehaviour) {
                            ((LockableBottomSheetBehaviour) mBottomSheetBehavior).setLocked(false);
                        }
                        if (fullscreen) {
                            expandPanel();
                        }
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (videoPlayerFragment != null) {
                    videoPlayerFragment.switchMiniPlayer(slideOffset);
                }
                if (audioPlayerFragment != null) {
                    audioPlayerFragment.switchMiniPlayer(slideOffset);
                }
                playerBgBlack.setAlpha(slideOffset);
            }
        });
    }

    private void setupUI(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            setupBottomSheet();
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            hidePanel();

            mainFragment = new MainFragment();

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.main_fragment, mainFragment);
            transaction.commit();
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.white_logo);
            setTaskDescription(new ActivityManager
                    .TaskDescription(getString(R.string.app_name), bm,
                    getResources().getColor(R.color.colorPrimary)));
        }

        changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        if (!NetworkUtils.isConnected(this)) {
            showStatusMessage(getString(R.string.offline_status_message),
                    getResources().getColor(R.color.md_red_800), false);
        }

        statusMessageWrapper.setOnClickListener(setupMessageClick());
    }

    private View.OnClickListener setupMessageClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (liveStream != null && liveStream.getOnAir()) {
                    Intent youtubePlayer = new Intent(MainActivity.this, YoutubePlayer.class);
                    youtubePlayer.putExtra(YoutubePlayer.LIVESTREAM, liveStream);
                    startActivity(youtubePlayer);
                }
            }
        };
    }

    public void playVideo(final Video video, final boolean quality) {
        audioPlayerFragment = null;

        videoPlayerFragment = getVideoPlayerFragment();
        if (videoPlayerFragment != null) {
            boolean currentQuality = videoPlayerFragment.isInQualityMode;
            Video currentVideo = videoPlayerFragment.getCurrentVideo();

            if (currentVideo != null
                    && currentVideo.getServerId() == video.getServerId()
                    && currentQuality == quality) {
                expandPanel();
                return;
            }
        }
        videoPlayerFragment = null;
        videoPlayerFragment = new VideoPlayerFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.player_fragment, videoPlayerFragment, VideoPlayerFragment.TAG);
        transaction.commit();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                expandPanel();
                getVideoPlayerFragment().playNewVideo(video, quality);
            }
        }, 600);
    }

    public void playNewVideo(final Video inputVideo) {
        String url = Constants.API_GET_VIDEO + inputVideo.getHash();

        Ion.with(this).load(url)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        if (e != null) {
                            SuperToast.create(MainActivity.this,
                                    getString(R.string.problem_occured_when_playing_video),
                                    SuperToast.Duration.MEDIUM).show();
                            return;
                        }

                        stopAudioPlaybackService();

                        JSONObject json = null;
                        Video updatedVideo = null;
                        Video video = inputVideo;
                        int savedVideoTime = 0;

                        try {
                            if (result != null) {
                                json = new JSONObject(result.getResult()
                                        .getAsJsonObject(Constants.JSON_VIDEO).toString());
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                        if (json != null) {
                            updatedVideo = ResponseFactory.parseVideo(json);
                        }

                        if (updatedVideo != null) {
                            savedVideoTime = video.getCurrentTime();
                            video.delete();
                            video = null;
                            video = updatedVideo;
                            video.setCurrentTime(savedVideoTime);
                            video.save();
                        }

                        if (wifi.isConnected()) {
                            playVideo(video, true);
                        } else {
                            if (sharedPreferences.loadStreamOnWifi()) {
                                SuperToast.create(MainActivity.this,
                                        getString(R.string.cannot_play_without_wifi),
                                        SuperToast.Duration.MEDIUM).show();
                            } else if (sharedPreferences.loadStreamAudio()) {
                                playNewAudio(video);
                            } else {
                                playVideo(video, false);
                            }
                        }
                    }
                });
    }

    private void reconnectToPlaybackService() {
        AudioPlaybackService service = ((OazaApp) getApplication()).playbackService;
        if (service != null) {
            if (isPanelHidden()) {
                collapsePanel();
            }

            if (getAudioPlayerFragment() == null) {
                audioPlayerFragment = new AudioPlayerFragment();
                audioPlayerFragment.init(true);
            }

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.player_fragment, getAudioPlayerFragment(), AudioPlayerFragment.TAG);
            transaction.commit();

            adjustLayout();
        }
    }

    private void stopAudioPlaybackService() {
        if(((OazaApp) getApplication()).playbackService != null) {
            ((OazaApp) getApplication()).playbackService.playbackStop();
        }
    }

    private void startAudioPlaybackService(Video audio, boolean downloaded) {
        if(((OazaApp) getApplication()).playbackService != null) {
            if (playAudioIntent != null) {
                Video currentlyPlayingAudio = playAudioIntent.getParcelableExtra(AudioPlaybackService.AUDIO);
                if (!currentlyPlayingAudio.equals(audio)) {
                    stopAudioPlaybackService();
                } else {
                    return;
                }
            }
        }

        playAudioIntent = new Intent(this, AudioPlaybackService.class);
        playAudioIntent.putExtra(AudioPlaybackService.AUDIO, audio);
        playAudioIntent.putExtra(AudioPlaybackService.DOWNLOADED, downloaded);
        startService(playAudioIntent);
    }

    public void playNewAudio(final Video audio) {

        boolean downloaded = audio.isAudioDownloaded(this);

        if (!downloaded && sharedPreferences.loadStreamOnWifi() && !wifi.isConnected()) {
            SuperToast.create(this,
                    getString(R.string.cannot_play_without_wifi),
                    SuperToast.Duration.MEDIUM).show();
        } else {

            AudioPlaybackService playbackService = ((OazaApp) getApplication()).playbackService;
            Video currentAudio = null;
            if (playbackService != null) {
                currentAudio = playbackService.getCurrentAudio();
            }

            if (playbackService == null || currentAudio.getServerId() != audio.getServerId()) {

                stopService(new Intent(this, AudioPlaybackService.class));
                startAudioPlaybackService(audio, downloaded);

                videoPlayerFragment = null;
                audioPlayerFragment = new AudioPlayerFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.player_fragment, audioPlayerFragment, AudioPlayerFragment.TAG);
                transaction.commit();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        expandPanel();
                        getAudioPlayerFragment().playNewAudio(MainActivity.this);
                    }
                }, 600);
            }  else {
                expandPanel();
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adjustLayout();

    }

    private void adjustLayout() {

        if (mBottomSheetBehavior != null) {
            setupBottomSheet();
        }

        if (getMainFragment() != null) {
            if (getArchiveFragment() != null) {
                getArchiveFragment().adjustLayout();
            }
            if (getDownloadedFragment() != null) {
                getDownloadedFragment().adjustLayout();
            }
            if (getCategoriesFragment() != null) {
                getCategoriesFragment().adjustLayout();
            }
            if (getHomeFragment() != null) {
                getHomeFragment().adjustLayout();
            }
            if (getSongsFragment() != null) {
                getSongsFragment().adjustLayout();

                if (getSongsFragment().getSongFragment() != null) {
                    getSongsFragment().getSongFragment().adjustLayout();
                }
            }

            if (getPhotoAlbumFragment() != null) {
                getPhotoAlbumFragment().adjustLayout();
            }
        }

        if (getAudioPlayerFragment() != null) {
            getAudioPlayerFragment().reconnectToService(this);
            if (isPanelExpanded()) {
                getAudioPlayerFragment().switchMiniPlayer(1);
            } else {
                getAudioPlayerFragment().switchMiniPlayer(0);
            }
        } else if (getVideoPlayerFragment() != null) {
            if (getVideoPlayerFragment().getCurrentVideo() == null) {
                hidePanel();
            }
        }
    }

    public void expandPanel() {
        mBottomSheetBehavior.setHideable(false);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void collapsePanel() {
        mBottomSheetBehavior.setHideable(false);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void hidePanel() {
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onBackPressed() {

        int fragmentsCount = getSupportFragmentManager().getBackStackEntryCount();

        if (fullscreen) {
            setFullscreen(false);
            if (getVideoPlayerFragment() != null) {
                getVideoPlayerFragment().cancelFullscreenPlayer();
            }
            return;
        }

        if (getMainFragment() != null
                && getMainFragment().getSearchView() != null
                && getMainFragment().getSearchView().isSearchOpen()) {
            getMainFragment().getSearchView().closeSearch();
            return;
        }

        if (isPanelExpanded()) {
            if (getVideoPlayerFragment() != null) {
                if (getVideoPlayerFragment().isInFullscreenMode) {
                    getVideoPlayerFragment().cancelFullscreenPlayer();
                    return;
                } else {
                    collapsePanel();
                    return;
                }
            } else {
                collapsePanel();
                return;
            }

        }

        if (getMainFragment() != null
                && getMainFragment().getPhotoAlbumFragment() != null
                && getMainFragment().getPhotoalbumWrapper().getVisibility() == View.VISIBLE) {

            ViewPager photosViewPager = getMainFragment().getPhotoAlbumFragment().getPhotosViewPager();
            GridView grid = getMainFragment().getPhotoAlbumFragment().getGrid();

            if (photosViewPager.getVisibility() == View.VISIBLE) {
                int currentPhoto = photosViewPager.getCurrentItem();
                grid.smoothScrollToPosition(currentPhoto);

                getMainFragment().getPhotoAlbumFragment().hidePhotos();
            } else {
                getMainFragment().closeAlbum();
            }
            return;
        }

        if (getMainFragment() != null
                && getMainFragment().getSettingsCard() != null
                && getMainFragment().getSettingsCard().getVisibility() == View.VISIBLE) {

            getMainFragment().hideSettings();
            return;
        }

        switch (getMainFragment().getPager().getCurrentItem()) {

            case 1:
                if (categoryVisible) {
                    categoriesFragment.goBack();
                    categoryVisible = false;
                    return;
                }
                break;
            case 3:
                if (songVisible) {
                    songsFragment.goBack();
                    songVisible = false;
                    return;
                }
                break;
        }

        exit();
    }

    private void exit() {
        if (videoPlayerFragment != null) {
            videoPlayerFragment.exit();
        }
        if (songsFragment != null) {
            songsFragment.exit();
        }
        if (photoAlbumFragment != null) {
            photoAlbumFragment.exit();
        }
        if (homeFragment != null) {
            homeFragment.exit();
        }
        if (categoriesFragment != null) {
            categoriesFragment.exit();
        }
        downloadedFragment = null;
        homeFragment = null;
        photoAlbumFragment = null;
        songsFragment = null;
        videoPlayerFragment = null;
        audioPlayerFragment = null;
        categoriesFragment = null;
        archiveFragment = null;
        mainFragment = null;
        connManager = null;
        System.gc();
        finish();
    }

    public MainFragment getMainFragment() {
        if (mainFragment == null) {
            return (MainFragment) getFragmentManager().findFragmentById(R.id.main_fragment);
        }
        return mainFragment;
    }

    public VideoPlayerFragment getVideoPlayerFragment() {
        if (videoPlayerFragment == null) {
            return (VideoPlayerFragment) getFragmentManager()
                    .findFragmentByTag(VideoPlayerFragment.TAG);
        }
        return videoPlayerFragment;
    }

    public AudioPlayerFragment getAudioPlayerFragment() {
        if (audioPlayerFragment == null) {
            return (AudioPlayerFragment) getFragmentManager()
                    .findFragmentByTag(AudioPlayerFragment.TAG);
        }
        return audioPlayerFragment;
    }

    public RelativeLayout getMainRelativeLayout() {
        return mainRelativeLayout;
    }

    public boolean isPanelExpanded() {
        if (mBottomSheetBehavior != null) {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                return true;
            }
        }
        return false;
    }

    public boolean isPanelHidden() {
        if (mBottomSheetBehavior != null) {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                return true;
            }
        }
        return false;
    }

    public void showStatusMessage(String text, int backgroundColor, boolean liveStream) {
        statusMessageWrapper.setVisibility(View.VISIBLE);
        statusMessageWrapper.setBackgroundColor(backgroundColor);
        statusMessageText.setText(text);
        changeStatusBarColor(backgroundColor);

        if (liveStream) {
            liveStreamMessageWatch.setVisibility(View.VISIBLE);
        } else {
            liveStreamMessageWatch.setVisibility(View.GONE);
        }
    }

    public void hideStatusMessage() {
        statusMessageWrapper.setVisibility(View.GONE);
        changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_LIVESTREAM:
                liveStream = ResponseFactory.parseLiveStream(response);
                if (liveStream != null && liveStream.getOnAir()) {
                    showStatusMessage(getString(R.string.app_name) + " " + getString(R.string.now_on_air),
                            getResources().getColor(R.color.md_green_700), true);
                } else {
                    hideStatusMessage();
                }
                break;

            case GET_VIDEO:
                if (deepLink) {
                    Video video = ResponseFactory.parseVideoOnly(response);
                    if (video != null) {
                        playNewVideo(video);
                        deepLink = false;
                    }
                }
                break;
            case GET_PHOTO_ALBUM:
                if (deepLink) {
                    PhotoAlbum photoAlbum = ResponseFactory.parseAlbumOnly(response);
                    if (photoAlbum != null) {
                        getMainFragment().openAlbum(photoAlbum);
                        deepLink = false;
                    }
                }
                break;
        }
    }


    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        switch (requestType) {
            case GET_LIVESTREAM:
                SmartLog.e("error", exception.toString());
                break;
            default:
                super.onErrorResponse(exception, requestType);
        }
    }

    public void setVideoPlayerFragment(VideoPlayerFragment videoPlayerFragment) {
        this.videoPlayerFragment = videoPlayerFragment;
    }

    public void setAudioPlayerFragment(AudioPlayerFragment audioPlayerFragment) {
        this.audioPlayerFragment = audioPlayerFragment;
    }

    public void nextTab() {
        ViewPager pager = getMainFragment().getPager();
        int currentItem = pager.getCurrentItem();
        pager.setCurrentItem(currentItem + 1);
    }

    public void prevTab() {
        ViewPager pager = getMainFragment().getPager();
        int currentItem = pager.getCurrentItem();
        pager.setCurrentItem(currentItem - 1);
    }

    public void setContextDialog(MaterialDialog contextDialog) {
        this.contextDialog = contextDialog;
    }

    public void dismissContextDialog() {
        if (contextDialog != null)  {
            contextDialog.dismiss();
        }
    }

    public boolean isSomethingPlaying() {
        if (getVideoPlayerFragment() != null) {
            return getVideoPlayerFragment().isCurrentlyPlaying();
        }
        if (getAudioPlayerFragment() != null) {
            return getAudioPlayerFragment().isCurrentlyPlaying();
        }
        return false;
    }

    public void setupAudioDownloadedDeletedReceiver() {
        unregisterAudioDownloadedDeletedReceiver();

        audioDownloadedDeletedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getArchiveFragment() != null) {
                    getArchiveFragment().notifyAudioDeleted();
                }
                if (getHomeFragment() != null) {
                    getHomeFragment().notifyAudioDeleted();
                }
                if (getCategoryFragment() != null) {
                    getCategoryFragment().notifyAudioDeleted();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadedFragment.DOWNLOAD_MANAGER_ONCHANGE);
        intentFilter.addAction(Video.NOTIFY_AUDIO_DELETE);

        registerReceiver(audioDownloadedDeletedReceiver, intentFilter);
    }

    private void setupNetworkStateReceiver() {
        unregisterNetworkStateReceiver();

        final IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filters.addAction("android.net.wifi.STATE_CHANGE");
        networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                boolean isConnected = wifi != null && wifi.isConnectedOrConnecting() ||
                        mobile != null && mobile.isConnectedOrConnecting();

                if (isConnected) {
                    hideStatusMessage();
                    setupLiveRequestTimer();
                } else {
                    noConnectionMessage();
                }
            }
        };
        registerReceiver(networkStateReceiver, filters);
    }

    private void unregisterAudioDownloadedDeletedReceiver() {
        if (audioDownloadedDeletedReceiver != null) {
            try {
                unregisterReceiver(audioDownloadedDeletedReceiver);
                audioDownloadedDeletedReceiver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void unregisterNetworkStateReceiver() {
        if (networkStateReceiver != null) {
            try {
                unregisterReceiver(networkStateReceiver);
                networkStateReceiver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
