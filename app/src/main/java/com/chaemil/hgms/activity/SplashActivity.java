package com.chaemil.hgms.activity;

import android.content.Intent;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.utils.FileUtils;
import com.chaemil.hgms.utils.SharedPrefUtils;
import com.chaemil.hgms.utils.TVUtils;

/**
 * Created by chaemil on 8.2.16.
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        ((OazaApp) getApplication()).splashActivity = this;
        init();
    }

    private void init() {
        changeStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        if (TVUtils.isTV(this)) {
            startTvActivity();
        } else {
            SharedPrefUtils sharedPrefUtils = SharedPrefUtils.getInstance(this);
            if (sharedPrefUtils.loadFirstLaunch()) {
                FileUtils.clearApplicationData(getApplicationContext());
                startTutorial();
            } else {
                startMainActivity();
            }
        }
    }

    public void startMainActivity() {
        delay(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        }, 750);
    }

    public void startTvActivity() {
        delay(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, TvActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        }, 750);
    }

    public void startTutorial() {
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);

        overridePendingTransition(R.anim.fadeout, R.anim.fadeout);
    }

    @Override
    public void onBackPressed() {
        //Do nothing
    }
}
