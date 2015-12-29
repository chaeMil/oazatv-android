package com.chaemil.hgms.model;

import com.chaemil.hgms.utils.Constants;
import com.orm.SugarRecord;

import java.util.Locale;

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
    private String videoFile;
    private String audioFile;
    private String thumbFile;
    private int views;
    private String categories;
    private String descriptionCS;
    private String descriptionEN;
    private boolean downloaded;

    public Video() {
    }

    public Video(int serverId, String hash, String date, String nameCS,
                 String nameEN, String tags, String videoFile, String audioFile,
                 String thumbFile, int views, String categories,
                 String descriptionCS, String descriptionEN, boolean downloaded) {
        this.serverId = serverId;
        this.hash = hash;
        this.date = date;
        this.nameCS = nameCS;
        this.nameEN = nameEN;
        this.tags = tags;
        this.videoFile = videoFile;
        this.audioFile = audioFile;
        this.thumbFile = thumbFile;
        this.views = views;
        this.categories = categories;
        this.descriptionCS = descriptionCS;
        this.descriptionEN = descriptionEN;
        this.downloaded = downloaded;
    }

    public boolean equals(Object other) {

        if (other instanceof Video) {
            return ((Video) other).getServerId() == this.serverId;
        } else {
            return false;
        }

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

    public String getVideoFile() {
        return videoFile;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public String getThumbFile() {
        return thumbFile;
    }

    public int getViews() {
        return views;
    }

    public  String getCategories() {
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
