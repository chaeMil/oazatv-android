package com.chaemil.hgms.activity;

import android.content.Intent;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.utils.SharedPrefUtils;

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

        SharedPrefUtils sharedPrefUtils = SharedPrefUtils.getInstance(this);
        if (sharedPrefUtils.loadFirstLaunch()) {
            startTutorial();
        } else {
            startMainActivity();
        }
    }

    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    public void startTutorial() {
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);

        overridePendingTransition(R.anim.fadeout, R.anim.fadeout);
    }


}
