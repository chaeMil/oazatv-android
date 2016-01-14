package com.chaemil.hgms.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.chaemil.hgms.R;
import com.chaemil.hgms.fragment.AudioPlayerFragment;
import com.chaemil.hgms.fragment.MainFragment;
import com.chaemil.hgms.fragment.VideoPlayerFragment;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.DownloadService;
import com.chaemil.hgms.utils.SmartLog;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by chaemil on 2.12.15.
 */
public class MainActivity extends BaseActivity implements
        SlidingUpPanelLayout.PanelSlideListener {

    private SlidingUpPanelLayout panelLayout;
    private VideoPlayerFragment videoPlayerFragment;
    private AudioPlayerFragment audioPlayerFragment;
    private MainFragment mainFragment;
    private int currentOrientation;
    private RelativeLayout mainRelativeLayout;
    private AudioPlaybackControlsReceiver audioPlaybackReceiver;


    @Override
    protected void onResume() {
        super.onResume();
        adjustLayout();

        currentOrientation = getResources().getConfiguration().orientation;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        getUI();
        setupUI(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioPlayerFragment.NOTIFY_PLAY_PAUSE);
        filter.addAction(AudioPlayerFragment.NOTIFY_OPEN);
        filter.addAction(AudioPlayerFragment.NOTIFY_FF);
        filter.addAction(AudioPlayerFragment.NOTIFY_REW);

        audioPlaybackReceiver = new AudioPlaybackControlsReceiver();
        registerReceiver(audioPlaybackReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(audioPlaybackReceiver);
    }

    private void bringToFront() {
        Intent i = new Intent(getBaseContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
        getBaseContext().startActivity(i);
    }

    private void getUI() {
        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_up_panel_layout);
        mainRelativeLayout = (RelativeLayout) findViewById(R.id.main_relative_layout);
    }

    private void setupUI(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            hidePanel();

            mainFragment = new MainFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_fragment, mainFragment);
            transaction.commit();
        }

        changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        panelLayout.setPanelSlideListener(this);
    }

    public void playNewVideo(final Video video) {

        audioPlayerFragment = null;
        videoPlayerFragment = new VideoPlayerFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.player_fragment, videoPlayerFragment);
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

    public void playNewAudio(final Video video, final boolean downloaded) {

        videoPlayerFragment = null;
        audioPlayerFragment = new AudioPlayerFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.player_fragment, audioPlayerFragment);
        transaction.commit();
        expandPanel();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getAudioPlayerFragment().playNewAudio(video, downloaded);
            }
        }, 600);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adjustLayout();

        currentOrientation = getResources().getConfiguration().orientation;
    }

    private void adjustLayout() {

        adjustPlayersLayout();
        getMainFragment().getArchiveFragment().adjustLayout();
        getMainFragment().getDownloadedFragment().adjustLayout();

        if (panelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            changeStatusBarColor(getResources().getColor(R.color.black));
            if (videoPlayerFragment != null) {
                panelLayout.setDragView(videoPlayerFragment.getPlayerToolbar());
            }
            if (audioPlayerFragment != null) {
                panelLayout.setDragView(audioPlayerFragment.getPlayerToolbar());
            }
        } else {
            changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            if (videoPlayerFragment != null) {
                panelLayout.setDragView(videoPlayerFragment.getMiniPlayer());
            }
            if (audioPlayerFragment != null) {
                panelLayout.setDragView(audioPlayerFragment.getMiniPlayer());
            }
        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if (slideOffset > 0.7) {
            changeStatusBarColor(getResources().getColor(R.color.black));
        } else if (slideOffset < 0.7 && slideOffset > 0.4) {
            changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDarker));
        } else if (slideOffset < 0.4) {
            changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        if (slideOffset < 0.2) {
            adjustPlayersLayout();
            if (videoPlayerFragment != null) {
                videoPlayerFragment.switchMiniPlayer(true);
            }
            if (audioPlayerFragment != null) {
                audioPlayerFragment.switchMiniPlayer(true);
            }
        } else {
            adjustPlayersLayout();
            if (videoPlayerFragment != null) {
                videoPlayerFragment.switchMiniPlayer(false);
            }
            if (audioPlayerFragment != null) {
                audioPlayerFragment.switchMiniPlayer(false);
            }
        }
    }

    @Override
    public void onPanelCollapsed(View panel) {
        adjustLayout();
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
        adjustPlayersLayout();
    }

    public void colapsePanel() {
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        adjustPlayersLayout();
    }

    private void adjustPlayersLayout() {
        if (videoPlayerFragment != null) {
            videoPlayerFragment.adjustLayout();
        }
    }

    public void hidePanel() {
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    @Override
    public void onBackPressed() {

        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) { //if player in fullscreen rotate screen to portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            //this will reset orientation back to sensor after 2 sec
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }, 2000);

        } else if (panelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    private void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
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
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private class AudioPlaybackControlsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            SmartLog.Log(SmartLog.LogLevel.DEBUG, "onReceive", intent.getAction());

            switch (intent.getAction()) {

                case AudioPlayerFragment.NOTIFY_PLAY_PAUSE:
                    getAudioPlayerFragment().playPauseAudio();
                    break;
                case AudioPlayerFragment.NOTIFY_OPEN:

                    Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    context.sendBroadcast(it);

                    bringToFront();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            expandPanel();
                        }
                    }, 500);
                    break;
                case AudioPlayerFragment.NOTIFY_FF:
                    getAudioPlayerFragment().seekFF();
                    break;
                case AudioPlayerFragment.NOTIFY_REW:
                    getAudioPlayerFragment().seekREW();
                    break;
            }


        }
    }


}
