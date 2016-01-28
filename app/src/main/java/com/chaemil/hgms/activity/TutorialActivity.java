package com.chaemil.hgms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.chaemil.hgms.R;
import com.crashlytics.android.Crashlytics;
import com.viewpagerindicator.CirclePageIndicator;

import io.fabric.sdk.android.Fabric;


public class TutorialActivity extends BaseActivity implements View.OnClickListener {

    private ViewPager viewPager;
    private CirclePageIndicator indicator;
    private static final int NUM_PAGES = 3;
    private PagerAdapter pagerAdapter;
    private Button next;
    private Button back;
    private Button finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_tutorial);

        startMainActivity();

        getUI();
        setupUI();
    }

    private void startMainActivity() {
        Intent mainActivity = new Intent(TutorialActivity.this, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(mainActivity);
    }

    private void getUI() {
        viewPager = (ViewPager) findViewById(R.id.tutorial_pager);
        indicator = (com.viewpagerindicator.CirclePageIndicator) findViewById(R.id.viewpager_indicator);
        next = (Button) findViewById(R.id.next);
        back = (Button) findViewById(R.id.back);
        finish = (Button) findViewById(R.id.finish);
    }

    private void setupUI() {
        pagerAdapter = new TutorialPager();
        viewPager.setAdapter(pagerAdapter);
        indicator.setViewPager(viewPager);
        next.setOnClickListener(this);
        back.setOnClickListener(this);
        finish.setOnClickListener(this);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        back.setVisibility(View.GONE);
                        break;
                    case 1:
                        back.setVisibility(View.VISIBLE);
                        next.setVisibility(View.VISIBLE);
                        finish.setVisibility(View.GONE);
                        break;
                    case 2:
                        next.setVisibility(View.GONE);
                        finish.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                break;
            case R.id.finish:
                finish();
                startMainActivity();
                break;
            case R.id.back:
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                break;
        }
    }

    private class TutorialPager extends PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup collection, int position) {

            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

            ViewGroup layout = null;
            switch (position) {
                case 0:
                    layout = (ViewGroup) inflater.inflate(R.layout.tutorial_1, collection, false);
                    break;
                case 1:
                    layout = (ViewGroup) inflater.inflate(R.layout.tutorial_2, collection, false);
                    break;
                case 2:
                    layout = (ViewGroup) inflater.inflate(R.layout.tutorial_3, collection, false);
                    break;
            }

            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
