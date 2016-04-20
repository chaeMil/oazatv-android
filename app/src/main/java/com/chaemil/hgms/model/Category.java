package com.chaemil.hgms.model;

import java.util.ArrayList;

/**
 * Created by chaemil on 20.4.16.
 */
public class Category {
    private int id;
    private String nameCS;
    private String nameEn;
    private String color;
    private ArrayList<Video> videos;

    public Category(int id, String nameCS, String nameEn, String color, ArrayList<Video> videos) {
        this.id = id;
        this.nameCS = nameCS;
        this.nameEn = nameEn;
        this.color = color;
        this.videos = videos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNameCS() {
        return nameCS;
    }

    public void setNameCS(String nameCS) {
        this.nameCS = nameCS;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public ArrayList<Video> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<Video> videos) {
        this.videos = videos;
    }
}
