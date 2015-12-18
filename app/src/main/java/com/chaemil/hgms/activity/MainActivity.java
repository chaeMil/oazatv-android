package com.chaemil.hgms.activity;

import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

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
    private Toolbar toolbar;
    private PlayerFragment playerFragment;
    private MainFragment mainFragment;

    @Override
    protected void onResume() {
        super.onResume();
        adjustLayout();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        getUI();
        setupUI();
    }

    private void getUI() {
        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_up_panel_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        playerFragment = ((PlayerFragment) getSupportFragmentManager().findFragmentByTag(PlayerFragment.TAG));
    }

    private void setupUI() {

        mainFragment = new MainFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.main_fragment, mainFragment);
        transaction.commit();

        changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        panelLayout.setPanelSlideListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adjustLayout();
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

    }

    @Override
    public void onPanelExpanded(View panel) {

    }

    @Override
    public void onPanelAnchored(View panel) {

    }

    @Override
    public void onPanelHidden(View panel) {

    }

    @Override
    public void onBackPressed() {
        if (panelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
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

}
