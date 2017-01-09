package com.chaemil.hgms.model;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

/**
 * Created by chaemil on 1.11.16.
 */

public class SongGroup {

    @Expose
    private String tag;
    @Expose
    private ArrayList<Song> songs;

    public SongGroup(String tag, ArrayList<Song> songs) {
        this.tag = tag;
        this.songs = songs;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }
}
