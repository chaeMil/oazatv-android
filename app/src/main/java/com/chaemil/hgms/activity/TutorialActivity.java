package com.chaemil.hgms.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.chaemil.hgms.R;
import com.chaemil.hgms.utils.SharedPrefUtils;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;


public class TutorialActivity extends IntroActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setSkipEnabled(false);
        setFinishEnabled(true);

        addSlide(new SimpleSlide.Builder()
                .title(R.string.app_name)
                .description(R.string.tutorial_text_1)
                .image(R.drawable.tutorial_slide_1)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.tutorial_title_2)
                .description(R.string.tutorial_text_2)
                .image(R.drawable.tutorial_slide_2)
                .background(R.color.tutorial_2_bg)
                .backgroundDark(R.color.tutorial_2_bg_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.tutorial_title_3)
                .description(R.string.tutorial_text_3)
                .image(R.drawable.tutorial_slide_3)
                .background(R.color.tutorial_3_bg)
                .backgroundDark(R.color.tutorial_3_bg_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.tutorial_title_4)
                .description(R.string.tutorial_text_4)
                .image(R.drawable.tutorial_slide_4)
                .background(R.color.tutorial_4_bg)
                .backgroundDark(R.color.tutorial_4_bg_dark)
                .build());

        /* Add your own page change listeners */
        addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                if (position == 4) {
                    SharedPrefUtils.getInstance(TutorialActivity.this).saveFirstLaunch(false);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
}