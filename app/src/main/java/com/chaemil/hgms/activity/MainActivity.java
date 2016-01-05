package com.chaemil.hgms.activity;

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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by chaemil on 2.12.15.
 */
public class MainActivity extends BaseActivity implements
        SlidingUpPanelLayout.PanelSlideListener, View.OnClickListener {

    private SlidingUpPanelLayout panelLayout;
    private VideoPlayerFragment videoPlayerFragment;
    private AudioPlayerFragment audioPlayerFragment;
    private MainFragment mainFragment;
    private int currentOrientation;
    private RelativeLayout mainRelativeLayout;


    @Override
    protected void onResume() {
        super.onResume();
        adjustLayout();

        currentOrientation = getResources().getConfiguration().orientation;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getVideoPlayerFragment() != null) {
            getVideoPlayerFragment().saveCurrentVideoTime();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        getUI();
        setupUI(savedInstanceState);
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

    public void playNewAudio(final Video video) {

        audioPlayerFragment = new AudioPlayerFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.player_fragment, audioPlayerFragment);
        transaction.commit();
        expandPanel();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getAudioPlayerFragment().playNewAudio(video);
            }
        }, 600);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adjustLayout();
        getMainFragment().getArchiveFragment().adjustLayout();

        currentOrientation = getResources().getConfiguration().orientation;
    }

    private void adjustLayout() {
        if (panelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            changeStatusBarColor(getResources().getColor(R.color.black));
            adjustPlayersLayout();
            if (videoPlayerFragment != null) {
                panelLayout.setDragView(videoPlayerFragment.getPlayerToolbar());
            }
            if (audioPlayerFragment != null) {
                panelLayout.setDragView(audioPlayerFragment.getPlayerToolbar());
            }
        } else {
            changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            adjustPlayersLayout();
            if (videoPlayerFragment != null) {
                panelLayout.setDragView(videoPlayerFragment.getMiniPlayer());
            }
            if (audioPlayerFragment != null) {
                panelLayout.setDragView(audioPlayerFragment.getMiniPlayer());
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mini_player:
                if (panelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                    panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
                break;
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
        if (audioPlayerFragment != null) {
            audioPlayerFragment.adjustLayout();
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


}
