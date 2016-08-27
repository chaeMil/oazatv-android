package com.chaemil.hgms.utils;

import com.chaemil.hgms.model.Video;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by chaemil on 27.8.16.
 */
public class HomepageVideoComparator implements Comparator<Video> {
    @Override
    public int compare(Video video, Video otherVideo) {

        int videoValue = 0;
        int otherVideoValue = 0;

        Date videoDate = new Date(video.getDate());
        Date otherVideoDate = new Date(otherVideo.getDate());

        if (videoDate.after(otherVideoDate)) {
            videoValue += 50;
        } else {
            otherVideoValue += 50;
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }
}
