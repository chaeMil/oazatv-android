package com.chaemil.hgms.ui.universal.activity;

import android.content.Intent;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.ui.mobile.activity.BaseActivity;
import com.chaemil.hgms.ui.mobile.activity.TutorialActivity;
import com.chaemil.hgms.ui.tv.activity.MainActivity;
import com.chaemil.hgms.utils.FileUtils;
import com.chaemil.hgms.utils.SharedPrefUtils;
import com.chaemil.hgms.utils.TVUtils;

/**
 * Created by chaemil on 8.2.16.
 */
public class SplashForceTVActivity extends BaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        startTvActivity();
    }

    public void startTvActivity() {
        delay(() -> {
            Intent intent = new Intent(SplashForceTVActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        }, 750);
    }

    @Override
    public void onBackPressed() {
        //Do nothing
    }
}
