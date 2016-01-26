package com.chaemil.hgms.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.chaemil.hgms.R;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.utils.SmartLog;

/**
 * Created by chaemil on 4.12.15.
 */
public class MainFragment extends Fragment implements TabLayout.OnTabSelectedListener {

    public static final String TAG = "main_fragment";
    private TabLayout tabLayout;
    private ViewPager pager;
    private FragmentActivity context;
    private HomeFragment homeFragment;
    private ArchiveFragment archiveFragment;
    private DownloadedFragment downloadedFragment;
    private Toolbar toolbar;
    private LinearLayout appBar;
    private FrameLayout photoalbumWrapper;
    private PhotoAlbumFragment photoAlbumFragment;

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
            photoAlbumFragment = new PhotoAlbumFragment(null);
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
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.downloaded)));
        tabLayout.setOnTabSelectedListener(this);
        appBar = (LinearLayout) rootView.findViewById(R.id.app_bar);
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        photoalbumWrapper = (FrameLayout) rootView.findViewById(R.id.photoalbum_wrapper);
    }

    private void setupUI(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            pager.setAdapter(new MainFragmentsAdapter(context.getSupportFragmentManager()));
            pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            pager.setOffscreenPageLimit(2);
        }

        toolbar.setTitle(getString(R.string.app_name));
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

    public TabLayout getTabLayout() {
        return tabLayout;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public LinearLayout getAppBar() {
        return appBar;
    }

    public FrameLayout getPhotoalbumWrapper() {
        return photoalbumWrapper;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        pager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    public void openAlbum(PhotoAlbum album) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.photoalbum_wrapper, photoAlbumFragment, PhotoAlbumFragment.TAG);
        transaction.commit();
        photoAlbumFragment.setAlbum(album);
        photoalbumWrapper.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.GONE);
        pager.setVisibility(View.GONE);
    }

    public void closeAlbum() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(getFragmentManager().findFragmentByTag(PhotoAlbumFragment.TAG));
        transaction.commit();
        photoalbumWrapper.setVisibility(View.GONE);
        tabLayout.setVisibility(View.VISIBLE);
        pager.setVisibility(View.VISIBLE);
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
