package com.chaemil.hgms.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.chaemil.hgms.utils.Constants;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Locale;

/**
 * Created by chaemil on 24.1.16.
 */
public class Photo implements Parcelable {

    public static final String PHOTO = "photo";
    @Expose
    @SerializedName(Constants.JSON_ORIGINAL_FILE)
    private String originalFile;
    @Expose
    @SerializedName(Constants.JSON_THUMB_128)
    private String thumb128;
    @Expose
    @SerializedName(Constants.JSON_THUMB_256)
    private String thumb256;
    @Expose
    @SerializedName(Constants.JSON_THUMB_512)
    private String thumb512;
    @Expose
    @SerializedName(Constants.JSON_THUMB_1024)
    private String thumb1024;
    @Expose
    @SerializedName(Constants.JSON_THUMB_2048)
    private String thumb2048;
    @Expose
    @SerializedName(Constants.JSON_DESCRIPTION_CS)
    private String descriptionCS;
    @Expose
    @SerializedName(Constants.JSON_DESCRIPTION_EN)
    private String descriptionEN;

    public Photo(String originalFile, String thumb128, String thumb256, String thumb512, String thumb1024, String thumb2048, String descriptionCS, String descriptionEN) {
        this.originalFile = originalFile;
        this.thumb128 = thumb128;
        this.thumb256 = thumb256;
        this.thumb512 = thumb512;
        this.thumb1024 = thumb1024;
        this.thumb2048 = thumb2048;
        this.descriptionCS = descriptionCS;
        this.descriptionEN = descriptionEN;
    }

    public Photo(Parcel source) {
        this.originalFile = source.readString();
        this.thumb128 = source.readString();
        this.thumb256 = source.readString();
        this.thumb512 = source.readString();
        this.thumb1024 = source.readString();
        this.thumb2048 = source.readString();
        this.descriptionCS = source.readString();
        this.descriptionEN = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(originalFile);
        dest.writeString(thumb128);
        dest.writeString(thumb256);
        dest.writeString(thumb512);
        dest.writeString(thumb1024);
        dest.writeString(thumb2048);
        dest.writeString(descriptionCS);
        dest.writeString(descriptionEN);
    }

    public static final Parcelable.Creator<Photo> CREATOR
            = new Parcelable.Creator<Photo>() {
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

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

    public String getOriginalFile() {
        return originalFile;
    }

    public String getThumb128() {
        return thumb128;
    }

    public String getThumb256() {
        return thumb256;
    }

    public String getThumb512() {
        return thumb512;
    }

    public String getThumb1024() {
        return thumb1024;
    }

    public String getThumb2048() {
        return thumb2048;
    }

    public String getDescriptionCS() {
        return descriptionCS;
    }

    public String getDescriptionEN() {
        return descriptionEN;
    }
}
