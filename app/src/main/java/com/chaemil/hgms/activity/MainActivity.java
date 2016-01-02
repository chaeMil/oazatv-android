package com.chaemil.hgms.activity;

import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.chaemil.hgms.R;
import com.chaemil.hgms.fragment.MainFragment;
import com.chaemil.hgms.fragment.PlayerFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by chaemil on 2.12.15.
 */
public class MainActivity extends BaseActivity implements
        SlidingUpPanelLayout.PanelSlideListener, View.OnClickListener {

    private SlidingUpPanelLayout panelLayout;
    private PlayerFragment playerFragment;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        getUI();
        setupUI(savedInstanceState);
    }

    private void getUI() {
        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_up_panel_layout);
        playerFragment = ((PlayerFragment) getSupportFragmentManager().findFragmentByTag(PlayerFragment.TAG));
        mainRelativeLayout = (RelativeLayout) findViewById(R.id.main_relative_layout);
    }

    private void setupUI(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            hidePanel();

            mainFragment = new MainFragment();

            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            transaction.replace(R.id.main_fragment, mainFragment);
            transaction.commit();
        }

        changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        panelLayout.setPanelSlideListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adjustLayout();
        playerFragment.adjustLayout();

        currentOrientation = getResources().getConfiguration().orientation;
    }

    private void adjustLayout() {
        if (panelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            changeStatusBarColor(getResources().getColor(R.color.black));
            playerFragment.switchMiniPlayer(false);
            panelLayout.setDragView(playerFragment.getPlayerToolbar());
        } else {
            changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            playerFragment.switchMiniPlayer(true);
            panelLayout.setDragView(playerFragment.getMiniPlayer());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mini_player:
                if (panelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                    panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    playerFragment.switchMiniPlayer(false);
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
            playerFragment.switchMiniPlayer(true);
        } else {
            playerFragment.switchMiniPlayer(false);
        }
    }

    @Override
    public void onPanelCollapsed(View panel) {
        getPlayerFragment().adjustLayout();
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else {

        }
    }

    @Override
    public void onPanelExpanded(View panel) {
        getPlayerFragment().adjustLayout();
    }

    @Override
    public void onPanelAnchored(View panel) {

    }

    @Override
    public void onPanelHidden(View panel) {

    }

    public void expandPanel() {
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        getPlayerFragment().adjustLayout();
    }

    public void colapsePanel() {
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        getPlayerFragment().adjustLayout();
    }

    public void hidePanel() {
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    @Override
    public void onBackPressed() {

        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE
                && !getPlayerFragment().isPlayingAudio()) { //if player in fullscreen rotate screen to portrait
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

    public PlayerFragment getPlayerFragment() {
        return playerFragment;
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
