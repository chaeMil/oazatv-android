package com.chaemil.hgms.model;

import com.chaemil.hgms.utils.Constants;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by chaemil on 24.1.16.
 */
public class PhotoAlbum {

    @Expose
    @SerializedName(Constants.JSON_ID)
    private int serverId;
    @Expose
    private String hash;
    @Expose
    private String date;
    @Expose
    @SerializedName(Constants.JSON_NAME_CS)
    private String nameCS;
    @Expose
    @SerializedName(Constants.JSON_NAME_EN)
    private String nameEN;
    @Expose
    private String tags;
    @Expose
    @SerializedName(Constants.JSON_DESCRIPTION_CS)
    private String descriptionCS;
    @Expose
    @SerializedName(Constants.JSON_DESCRIPTION_EN)
    private String descriptionEN;
    @Expose
    private ArrayList<Photo> photos;
    @Expose
    private Photo thumbs;

    public PhotoAlbum() {
    }

    public PhotoAlbum(int serverId, String hash, String date, String nameCS, String nameEN,
                      String tags, String descriptionCS, String descriptionEN, Photo thumbs,
                      ArrayList<Photo> photos) {
        this.serverId = serverId;
        this.hash = hash;
        this.date = date;
        this.nameCS = nameCS;
        this.nameEN = nameEN;
        this.tags = tags;
        this.descriptionCS = descriptionCS;
        this.descriptionEN = descriptionEN;
        this.thumbs = thumbs;
        this.photos = photos;
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

    public String getDescription() {
        switch (Locale.getDefault().getLanguage()) {

            case Constants.SK:
                return getDescriptionCS();
            case Constants.CS:
                return getDescriptionCS();
            case Constants.EN:
                return getDescriptionEN();
            default:
                return getDescriptionEN();
        }
    }


    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
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

    public String getDescriptionCS() {
        return descriptionCS;
    }

    public String getDescriptionEN() {
        return descriptionEN;
    }

    public Photo getThumbs() {
        return thumbs;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }
}
