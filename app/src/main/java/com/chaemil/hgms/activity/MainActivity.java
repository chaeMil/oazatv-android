package com.chaemil.hgms.activity;

import android.app.ActivityManager;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.fragment.AudioPlayerFragment;
import com.chaemil.hgms.fragment.MainFragment;
import com.chaemil.hgms.fragment.VideoPlayerFragment;
import com.chaemil.hgms.model.LiveStream;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.receiver.AudioPlaybackReceiver;
import com.chaemil.hgms.receiver.DownloadServiceReceiver;
import com.chaemil.hgms.receiver.DownloadServiceReceiverListener;
import com.chaemil.hgms.receiver.PlaybackReceiverListener;
import com.chaemil.hgms.service.AudioPlaybackService;
import com.chaemil.hgms.service.RequestService;
import com.chaemil.hgms.utils.NetworkUtils;
import com.chaemil.hgms.utils.SharedPrefUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.johnpersano.supertoasts.SuperToast;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chaemil on 2.12.15.
 */
public class MainActivity extends BaseActivity implements
        SlidingUpPanelLayout.PanelSlideListener,
        PlaybackReceiverListener{

    public static final String EXPAND_PANEL = "expand_panel";
    private SlidingUpPanelLayout panelLayout;
    private VideoPlayerFragment videoPlayerFragment;
    private AudioPlayerFragment audioPlayerFragment;
    private MainFragment mainFragment;
    private RelativeLayout mainRelativeLayout;
    private View decorView;
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
    private AudioPlaybackReceiver audioPlaybackReceiver;

    @Override
    protected void onResume() {
        super.onResume();
        adjustLayout();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        reconnectToPlaybackService();
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

        if (getIntent().getBooleanExtra(EXPAND_PANEL, false)) {
            expandPanel();
        }

        parseDeepLink();
    }

    private void parseDeepLink() {
        Intent intent = getIntent();
        Uri data = intent.getData();
        deepLink = true;

        if (data != null) {
            SmartLog.Log(SmartLog.LogLevel.DEBUG, "data", data.toString());
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

        audioPlaybackReceiver = new AudioPlaybackReceiver(this, ((OazaApp) getApplication()));
        registerReceiver(audioPlaybackReceiver, filter);
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
        super.onDestroy();
        ((OazaApp) getApplication()).setMainActivity(null);
        unregisterReceiver(audioPlaybackReceiver);
    }

    public void bringToFront() {
        Intent i = new Intent(getBaseContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
        getBaseContext().startActivity(i);
    }

    private void getUI() {
        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_up_panel_layout);
        mainRelativeLayout = (RelativeLayout) findViewById(R.id.main_relative_layout);
        decorView = getWindow().getDecorView().getRootView();
        playerWrapper = (RelativeLayout) findViewById(R.id.player_wrapper);
        statusMessageWrapper = (RelativeLayout) findViewById(R.id.status_message_wrapper);
        statusMessageText = (TextView) findViewById(R.id.status_message_text);
        liveStreamMessageWatch = (TextView) findViewById(R.id.watch);
    }

    private void setupUI(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            hidePanel();

            mainFragment = new MainFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_fragment, mainFragment);
            transaction.commit();
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.white_logo);
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), bm, getResources().getColor(R.color.colorPrimary)));
        }

        changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        panelLayout.setPanelSlideListener(this);

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

    private void playVideo(final Video video) {
        audioPlayerFragment = null;
        videoPlayerFragment = new VideoPlayerFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.player_fragment, videoPlayerFragment, VideoPlayerFragment.TAG);
        transaction.commit();
        expandPanel();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getVideoPlayerFragment().playNewVideo(video);
            }
        }, 600);
    }

    public void playNewVideo(final Video video) {

        stopAudioPlaybackService();

        if (wifi.isConnected()) {
            playVideo(video);
        } else {
            if (sharedPreferences.loadStreamOnWifi()) {
                SuperToast.create(this, getString(R.string.cannot_play_without_wifi), SuperToast.Duration.MEDIUM).show();
            } else if (sharedPreferences.loadStreamAudio()) {
                playNewAudio(video, true);
            } else {
                playVideo(video);
            }
        }

    }

    private void reconnectToPlaybackService() {
        AudioPlaybackService service = ((OazaApp) getApplication()).playbackService;
        if (service != null) {
            if (getPanelLayout().getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                getPanelLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                getMainFragment().hideSplash(false);
            }

            if (getAudioPlayerFragment() == null) {
                audioPlayerFragment = new AudioPlayerFragment();
                audioPlayerFragment.init(true);
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.player_fragment, audioPlayerFragment, AudioPlayerFragment.TAG);
            transaction.commit();

            /*getAudioPlayerFragment().playNewAudio(this,
                    service.getCurrentAudio(),
                    service.getIsPlayingDownloaded());*/
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

    public void playNewAudio(final Video audio, boolean expandPanel) {

        boolean downloaded = audio.isAudioDownloaded(this);

        if (!downloaded && sharedPreferences.loadStreamOnWifi() && !wifi.isConnected()) {
            SuperToast.create(this, getString(R.string.cannot_play_without_wifi), SuperToast.Duration.MEDIUM).show();
        } else {

            if (expandPanel) {
                expandPanel();
            }

            if (((OazaApp) getApplication()).playbackService == null
                    || ((OazaApp) getApplication()).playbackService.getCurrentAudio() != audio) {

                stopService(new Intent(this, AudioPlaybackService.class));
                startAudioPlaybackService(audio, downloaded);

                videoPlayerFragment = null;
                audioPlayerFragment = new AudioPlayerFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.player_fragment, audioPlayerFragment, AudioPlayerFragment.TAG);

                transaction.commit();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getAudioPlayerFragment().playNewAudio(MainActivity.this);
                    }
                }, 600);
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adjustLayout();

    }

    private void adjustLayout() {
        if (getMainFragment() != null) {
            getMainFragment().getArchiveFragment().adjustLayout();
            getMainFragment().getDownloadedFragment().adjustLayout();
            getMainFragment().getCategoriesFragment().adjustLayout();
            getMainFragment().getHomeFragment().adjustLayout();

            if (getMainFragment().getPhotoAlbumFragment() != null) {
                getMainFragment().getPhotoAlbumFragment().adjustLayout();
            }
        }

        if (getAudioPlayerFragment() != null) {
            if (isPanelExpanded()) {
                getAudioPlayerFragment().switchMiniPlayer(1);
            } else {
                getAudioPlayerFragment().switchMiniPlayer(0);
            }
        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if (slideOffset == 0) {
            if (videoPlayerFragment != null) {
                videoPlayerFragment.switchMiniPlayer(slideOffset);
            }
            if (audioPlayerFragment != null) {
                audioPlayerFragment.switchMiniPlayer(slideOffset);
            }
        } else {
            if (videoPlayerFragment != null) {
                videoPlayerFragment.switchMiniPlayer(slideOffset);
            }
            if (audioPlayerFragment != null) {
                audioPlayerFragment.switchMiniPlayer(slideOffset);
            }
        }
    }

    @Override
    public void onPanelCollapsed(View panel) {
        adjustLayout();
        setFullscreen(false);
        if (getVideoPlayerFragment() != null) {
            getVideoPlayerFragment().cancelFullscreenPlayer();
        }
    }

    @Override
    public void onPanelExpanded(View panel) {
        adjustLayout();
    }

    @Override
    public void onPanelAnchored(View panel) {

    }

    @Override
    public void onPanelHidden(View panel) {

    }

    public void expandPanel() {
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    public void collapsePanel() {
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public void hidePanel() {
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    @Override
    public void onBackPressed() {

        if (getMainFragment() != null
                && getMainFragment().getSearchView() != null
                && getMainFragment().getSearchView().isSearchOpen()) {

            getMainFragment().getSearchView().closeSearch();

        } else if (panelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {

            if (getVideoPlayerFragment() != null) {
                if (getVideoPlayerFragment().isInFullscreenMode) {
                    getVideoPlayerFragment().cancelFullscreenPlayer();
                } else {
                    collapsePanel();
                }
            } else {
                collapsePanel();
            }

        } else if (getMainFragment() != null
                    && getMainFragment().getPhotoAlbumFragment() != null
                    && getMainFragment().getPhotoalbumWrapper().getVisibility() == View.VISIBLE) {

            ViewPager photosViewPager = getMainFragment().getPhotoAlbumFragment().getPhotosViewPager();
            GridView grid = getMainFragment().getPhotoAlbumFragment().getGrid();

            if (photosViewPager.getVisibility() == View.VISIBLE) {
                int currentPhoto = photosViewPager.getCurrentItem();
                grid.smoothScrollToPosition(currentPhoto);

                if (grid.getChildAt(currentPhoto) != null) {
                    YoYo.with(Techniques.Pulse).duration(500).playOn(grid.getChildAt(currentPhoto));
                }

                getMainFragment().getPhotoAlbumFragment().hidePhotos();
            } else {
                getMainFragment().closeAlbum();
            }

        } else if (getMainFragment() != null
                && getMainFragment().getSettingsCard() != null
                && getMainFragment().getSettingsCard().getVisibility() == View.VISIBLE) {

            getMainFragment().hideSettings();

        } else {
            finish();
        }
    }

    public MainFragment getMainFragment() {
        return mainFragment;
    }

    public VideoPlayerFragment getVideoPlayerFragment() {
        return videoPlayerFragment;
    }

    public AudioPlayerFragment getAudioPlayerFragment() {
        return audioPlayerFragment;
    }

    public SlidingUpPanelLayout getPanelLayout() {
        return panelLayout;
    }

    public RelativeLayout getMainRelativeLayout() {
        return mainRelativeLayout;
    }

    public boolean isPanelExpanded() {
        if (panelLayout != null) {
            if (panelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
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
                Video video = ResponseFactory.parseVideo(response);
                if (video != null) {
                    if (deepLink) {
                        playNewVideo(video);
                        deepLink = false;
                    }
                }
                break;
            case GET_PHOTO_ALBUM:
                PhotoAlbum photoAlbum = ResponseFactory.parseAlbum(response);
                if (photoAlbum != null) {
                    if (deepLink) {
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
                SmartLog.Log(SmartLog.LogLevel.ERROR, "error", exception.toString());
                break;
            default:
                super.onErrorResponse(exception, requestType);
        }
    }

    @Override
    public void playbackPlayPauseAudio() {
        if (getAudioPlayerFragment() != null) {
            getAudioPlayerFragment().playPause();
        }
    }

    @Override
    public void playbackSeekFF() {

    }

    @Override
    public void playbackSeekREW() {

    }

    @Override
    public void playbackStop() {

    }
}
