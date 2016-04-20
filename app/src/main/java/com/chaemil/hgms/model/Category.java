package com.chaemil.hgms.model;

import com.chaemil.hgms.utils.Constants;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by chaemil on 20.4.16.
 */
public class Category {
    private int id;
    private String nameCS;
    private String nameEN;
    private String color;
    private ArrayList<Video> videos;

    public Category(int id, String nameCS, String nameEN, String color, ArrayList<Video> videos) {
        this.id = id;
        this.nameCS = nameCS;
        this.nameEN = nameEN;
        this.color = color;
        this.videos = videos;
    }

    public String getName() {
        switch (Locale.getDefault().getLanguage()) {

            case Constants.SK:
                return getNameCS();
            case Constants.CS:
                return getNameCS();
            case Constants.EN:
                return getNameEN();
            default:
                return getNameEN();
        }
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

    public String getNameEN() {
        return nameEN;
    }

    public void setNameEN(String nameEN) {
        this.nameEN = nameEN;
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
