package com.chaemil.hgms.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.chaemil.hgms.fragment.MainFragment;

/**
 * Created by chaemil on 13.8.16.
 */
public class MainFragmentsAdapter extends FragmentPagerAdapter {
    private MainFragment mainFragment;

    public MainFragmentsAdapter(MainFragment mainFragment, FragmentManager fm) {
        super(fm);
        this.mainFragment = mainFragment;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mainFragment.getHomeFragment();
            case 1:
                return mainFragment.getCategoriesFragment();
            case 2:
                return mainFragment.getArchiveFragment();
            case 3:
                return mainFragment.getDownloadedFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
