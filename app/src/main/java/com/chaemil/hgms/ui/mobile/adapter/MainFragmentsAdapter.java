package com.chaemil.hgms.ui.mobile.adapter;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.chaemil.hgms.ui.mobile.fragment.MainFragment;

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
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mainFragment.getHomeFragment();
            case 1:
                return mainFragment.getCategoriesFragment();
            case 2:
                return mainFragment.getArchiveFragment();
            case 3:
                return mainFragment.getSongsFragment();
            case 4:
                return mainFragment.getDownloadedFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 5;
    }
}
