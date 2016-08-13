package com.chaemil.hgms.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.chaemil.hgms.utils.Constants;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by chaemil on 20.4.16.
 */
public class Category implements Parcelable {
    @Expose
    private int id;
    @Expose
    @SerializedName(Constants.JSON_NAME_CS)
    private String nameCS;
    @Expose
    @SerializedName(Constants.JSON_NAME_EN)
    private String nameEN;
    @Expose
    private String color;
    @Expose
    private ArrayList<Video> videos;

    public Category(int id, String nameCS, String nameEN, String color, ArrayList<Video> videos) {
        this.id = id;
        this.nameCS = nameCS;
        this.nameEN = nameEN;
        this.color = color;
        this.videos = videos;
    }

    protected Category(Parcel in) {
        id = in.readInt();
        nameCS = in.readString();
        nameEN = in.readString();
        color = in.readString();
        videos = in.createTypedArrayList(Video.CREATOR);
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nameCS);
        dest.writeString(nameEN);
        dest.writeString(color);
        dest.writeTypedList(videos);
    }
}
