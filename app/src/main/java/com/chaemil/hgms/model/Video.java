package com.chaemil.hgms.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.SmartLog;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Query;
import com.orm.SugarRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by chaemil on 18.12.15.
 */
public class Video extends SugarRecord implements Parcelable {

    private Long id;
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
    @SerializedName(Constants.JSON_MP4_FILE_LOWRES)
    private String videoFileLowRes;
    @Expose
    @SerializedName(Constants.JSON_MP4_FILE)
    private String videoFile;
    @Expose
    @SerializedName(Constants.JSON_MP3_FILE)
    private String audioFile;
    @Expose
    @SerializedName(Constants.THUMB_FILE)
    private String thumbFile;
    @Expose
    @SerializedName(Constants.THUMB_FILE_LOW_RES)
    private String thumbFileLowRes;
    @Expose
    @SerializedName(Constants.THUMB_COLOR)
    private String thumbColor;
    @Expose
    @SerializedName(Constants.METADATA_DURATION)
    private int duration;
    @Expose
    private int views;
    @Expose
    private String categories;
    @Expose
    @SerializedName(Constants.JSON_DESCRIPTION_CS)
    private String descriptionCS;
    @Expose
    @SerializedName(Constants.JSON_DESCRIPTION_EN)
    private String descriptionEN;
    private int currentTime;

    public Video() {
    }

    public Video(int id, String hash, String date, String nameCS,
                 String nameEN, String tags, String videoFileLowRes, String videoFile, String audioFile,
                 String thumbFile, String thumbFileLowRes, String thumbColor, int duration,
                 int views, String categories,
                 String descriptionCS, String descriptionEN) {
        this.serverId = id;
        this.hash = hash;
        this.date = date;
        this.nameCS = nameCS;
        this.nameEN = nameEN;
        this.tags = tags;
        this.videoFileLowRes = videoFileLowRes;
        this.videoFile = videoFile;
        this.audioFile = audioFile;
        this.thumbFile = thumbFile;
        this.thumbFileLowRes = thumbFileLowRes;
        this.thumbColor = thumbColor;
        this.duration = duration;
        this.views = views;
        this.categories = categories;
        this.descriptionCS = descriptionCS;
        this.descriptionEN = descriptionEN;
    }

    public boolean equals(Object other) {

        if (other instanceof Video) {
            return ((Video) other).getServerId() == this.serverId;
        } else {
            return false;
        }

    }

    public static Video findByServerId(int serverId) {
        List<Video> videos = Video.find(Video.class, "server_id = ?", String.valueOf(serverId));
        if (videos.size() <= 0) {
            return null;
        } else {
            return videos.get(0);
        }
    }

    public boolean isAudioDownloaded(Context context) {
        DownloadManager downloadManager = DownloadManagerBuilder.from(context).build();
        Cursor cursor = downloadManager.query(new Query().setFilterByExtraData(String.valueOf(getServerId())));

        boolean downloaded = false;

        try {
            while (cursor.moveToNext()) {
                long videoId = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_EXTRA_DATA));

                downloaded = (videoId == getServerId());
            }
        } finally {
            cursor.close();
        }
        return downloaded;
    }

    public long getDownloadedAudioSize(Context context) {
        File file = new File(context.getExternalFilesDir(null) + "/" + getHash() + ".mp3");
        return file.length();
    }

    public void deleteDownloadedAudio(Context context) {
        File audio = new File(context.getExternalFilesDir(null) + "/" + getHash() + ".mp3");
        File thumb = new File(context.getExternalFilesDir(null) + "/" + getHash() + ".jpg");
        audio.delete();
        thumb.delete();
        save();
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

    public static List<Video> getNotFullyWatchedVideos() {
        return Video.find(Video.class, "current_time != 0");
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
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

    public String getVideoFileLowRes() {
        return videoFileLowRes;
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

    public int getCurrentTime() {
        return currentTime;
    }

    public String getThumbColor() {
        if (thumbColor == null || thumbColor.equals("null")) {
            return "#3a3f44";
        }
        return thumbColor;
    }

    public String getThumbFileLowRes() {
        return thumbFileLowRes;
    }

    public void setThumbFileLowRes(String thumbFileLowRes) {
        this.thumbFileLowRes = thumbFileLowRes;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    protected Video(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readLong();
        serverId = in.readInt();
        hash = in.readString();
        date = in.readString();
        nameCS = in.readString();
        nameEN = in.readString();
        tags = in.readString();
        videoFileLowRes = in.readString();
        videoFile = in.readString();
        audioFile = in.readString();
        thumbFile = in.readString();
        thumbFileLowRes = in.readString();
        thumbColor = in.readString();
        duration = in.readInt();
        views = in.readInt();
        categories = in.readString();
        descriptionCS = in.readString();
        descriptionEN = in.readString();
        currentTime = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeLong(id);
        }
        dest.writeInt(serverId);
        dest.writeString(hash);
        dest.writeString(date);
        dest.writeString(nameCS);
        dest.writeString(nameEN);
        dest.writeString(tags);
        dest.writeString(videoFileLowRes);
        dest.writeString(videoFile);
        dest.writeString(audioFile);
        dest.writeString(thumbFile);
        dest.writeString(thumbFileLowRes);
        dest.writeString(thumbColor);
        dest.writeInt(duration);
        dest.writeInt(views);
        dest.writeString(categories);
        dest.writeString(descriptionCS);
        dest.writeString(descriptionEN);
        dest.writeInt(currentTime);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
}