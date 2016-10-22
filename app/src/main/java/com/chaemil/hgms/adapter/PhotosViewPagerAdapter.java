package com.chaemil.hgms.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Parcelable;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.chaemil.hgms.fragment.PhotoFragment;
import com.chaemil.hgms.model.Photo;

import java.util.ArrayList;

/**
 * Created by chaemil on 21.7.15.
 */
public class PhotosViewPagerAdapter extends FragmentStatePagerAdapter {

    ArrayList<Photo> photos;

    public PhotosViewPagerAdapter(FragmentManager fm, ArrayList<Photo> photos) {
        super(fm);
        this.photos = photos;
    }

    @Override
    public Fragment getItem(int position) {
        return PhotoFragment.newInstance(photos.get(position));
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
