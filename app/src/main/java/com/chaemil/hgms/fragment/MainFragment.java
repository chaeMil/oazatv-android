package com.chaemil.hgms.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chaemil.hgms.R;
import com.chaemil.hgms.utils.SmartLog;

/**
 * Created by chaemil on 4.12.15.
 */
public class MainFragment extends Fragment {

    public static final String TAG = "main_fragment";
    private TabLayout tabLayout;
    private ViewPager pager;
    private FragmentActivity context;
    private HomeFragment homeFragment;
    private ArchiveFragment archiveFragment;
    private DownloadedFragment downloadedFragment;

    @Override
    public void onAttach(Activity activity) {
        context = (FragmentActivity) activity;
        super.onAttach(activity);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            SmartLog.Log(SmartLog.LogLevel.DEBUG, TAG + " savedInstanceState", "null");
            homeFragment = new HomeFragment();
            archiveFragment = new ArchiveFragment();
            downloadedFragment = new DownloadedFragment();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.main_fragment, container, false);

        getUI(rootView);
        setupUI(savedInstanceState);

        return rootView;
    }

    private void getUI(ViewGroup rootView) {
        tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.home)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.archive)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.settings)));
        pager = (ViewPager) rootView.findViewById(R.id.pager);
    }

    private void setupUI(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            pager.setAdapter(new MainFragmentsAdapter(context.getSupportFragmentManager()));
            pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            pager.setOffscreenPageLimit(2);
        }

    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }

    public ArchiveFragment getArchiveFragment() {
        return archiveFragment;
    }

    public DownloadedFragment getDownloadedFragment() {
        return downloadedFragment;
    }

    private class MainFragmentsAdapter extends FragmentPagerAdapter {
        public MainFragmentsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return getHomeFragment();
                case 1:
                    return getArchiveFragment();
                case 2:
                    return getDownloadedFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
