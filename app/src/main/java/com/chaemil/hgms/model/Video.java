package com.chaemil.hgms.model;

import com.orm.SugarRecord;

import java.util.ArrayList;

/**
 * Created by chaemil on 18.12.15.
 */
public class Video extends SugarRecord {

    private Long id;
    private int serverId;
    private String hash;
    private String date;
    private String nameCS;
    private String nameEN;
    private String tags;
    private String videoFileName;
    private String audioFileName;
    private String thumbFileName;
    private int views;
    private ArrayList<Integer> categories;
    private String descriptionCS;
    private String descriptionEN;
    private boolean downloaded;

    public Video() {
    }

    public Video(int serverId, String hash, String date, String nameCS,
                 String nameEN, String tags, String videoFileName, String audioFileName,
                 String thumbFileName, int views, ArrayList<Integer> categories,
                 String descriptionCS, String descriptionEN, boolean downloaded) {
        this.serverId = serverId;
        this.hash = hash;
        this.date = date;
        this.nameCS = nameCS;
        this.nameEN = nameEN;
        this.tags = tags;
        this.videoFileName = videoFileName;
        this.audioFileName = audioFileName;
        this.thumbFileName = thumbFileName;
        this.views = views;
        this.categories = categories;
        this.descriptionCS = descriptionCS;
        this.descriptionEN = descriptionEN;
        this.downloaded = downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public void setViews(int views) {
        this.views = views;
    }

    @Override
    public Long getId() {
        return id;
    }

    public int getServerId() {
        return serverId;
    }

    public String getHash() {
        return hash;
    }

    public String getDate() {
        return date;
    }

    public String getNameCS() {
        return nameCS;
    }

    public String getNameEN() {
        return nameEN;
    }

    public String getTags() {
        return tags;
    }

    public String getVideoFileName() {
        return videoFileName;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    public String getThumbFileName() {
        return thumbFileName;
    }

    public int getViews() {
        return views;
    }

    public ArrayList<Integer> getCategories() {
        return categories;
    }

    public String getDescriptionCS() {
        return descriptionCS;
    }

    public String getDescriptionEN() {
        return descriptionEN;
    }

    public boolean isDownloaded() {
        return downloaded;
    }
}
