package com.chaemil.hgms.model;

import java.util.ArrayList;

/**
 * Created by chaemil on 28.3.16.
 */
public class Homepage {

    public ArrayList<Video> newestVideos;
    public ArrayList<PhotoAlbum> newestAlbums;
    public ArrayList<Video> popularVideos;
    public ArrayList<ArchiveItem> featured;

    public Homepage(ArrayList<Video> newestVideos, ArrayList<PhotoAlbum> newestAlbums, ArrayList<Video> popularVideos, ArrayList<ArchiveItem> featured) {
        this.newestVideos = newestVideos;
        this.newestAlbums = newestAlbums;
        this.popularVideos = popularVideos;
        this.featured = featured;
    }

}
