package com.chaemil.hgms.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.service.DownloadService;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.SmartLog;
import com.orm.SugarRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by chaemil on 18.12.15.
 */
public class Video extends SugarRecord implements Parcelable {

    public static final int NOT_DOWNLOADED = 0;
    public static final int IN_DOWNLOAD_QUEUE = 1;
    public static final int DOWNLOADED = 2;
    public static final int CURRENTLY_DOWNLOADING = 3;

    private Long id;
    private int serverId;
    private String hash;
    private String date;
    private String nameCS;
    private String nameEN;
    private String tags;
    private String videoFileLowRes;
    private String videoFile;
    private String audioFile;
    private String thumbFile;
    private String thumbColor;
    private int views;
    private String categories;
    private String descriptionCS;
    private String descriptionEN;
    private boolean downloaded;
    private boolean inDownloadQueue;
    private int currentTime;

    public Video() {
    }

    public Video(int serverId, String hash, String date, String nameCS,
                 String nameEN, String tags, String videoFileLowRes, String videoFile, String audioFile,
                 String thumbFile, String thumbColor, int views, String categories,
                 String descriptionCS, String descriptionEN, boolean downloaded) {
        this.serverId = serverId;
        this.hash = hash;
        this.date = date;
        this.nameCS = nameCS;
        this.nameEN = nameEN;
        this.tags = tags;
        this.videoFileLowRes = videoFileLowRes;
        this.videoFile = videoFile;
        this.audioFile = audioFile;
        this.thumbFile = thumbFile;
        this.thumbColor = thumbColor;
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

    public static int getDownloadStatus(OazaApp oazaApp, int serverId) {
        Video savedVideo = findByServerId(serverId);
        if (savedVideo != null) {
            if (savedVideo.isCurrentlyDownloading(oazaApp)) {
                return CURRENTLY_DOWNLOADING;
            }
            if (savedVideo.isInDownloadQueue()) {
                return IN_DOWNLOAD_QUEUE;
            }
            if (savedVideo.isDownloaded()) {
                return DOWNLOADED;
            }
            return NOT_DOWNLOADED;
        }
        return NOT_DOWNLOADED;
    }

    public static Video findByServerId(int serverId) {
        List<Video> videos = Video.find(Video.class, "server_id = ?", String.valueOf(serverId));
        if (videos.size() <= 0) {
            return null;
        } else {
            return videos.get(0);
        }
    }

    public static List<Video> getDownloadQueue() {
        ArrayList<Video> queue = new ArrayList<>();
        List<Video> list = new ArrayList<Video>();
        try {
            list = Video.find(Video.class, "in_download_queue = ? AND downloaded = ?", String.valueOf(1), String.valueOf(0));
        } catch (Exception e) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
        }
        queue.addAll(list);

        return queue;
    }

    public static List<Video> getWholeDownloadQueue() {
        ArrayList<Video> queue = new ArrayList<>();
        try {
            List<Video> list = Video.find(Video.class, "in_download_queue = 1 OR downloaded = 1 ORDER BY id DESC");
            queue.addAll(list);
        } catch (Exception e) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
        }

        return queue;
    }

    public static List<Video> getAllVideoFromLocalDB() {
        return Video.findWithQuery(Video.class, "SELECT * FROM Video");
    }

    public static ArrayList<Video> getAllDownloadedVideos() {
        List<Video> list = Video.find(Video.class, "downloaded = ?", String.valueOf(1));
        ArrayList<Video> downloadedVideos = new ArrayList<>();
        downloadedVideos.addAll(list);

        return downloadedVideos;
    }

    public static long getDownloadedAudioSize(Context context, Video video) {
        File file = new File(context.getExternalFilesDir(null) + "/" + video.getHash() + ".mp3");
        return file.length();
    }

    public static boolean deleteDownloadedAudio(Context context, Video video) {
        File audio = new File(context.getExternalFilesDir(null) + "/" + video.getHash() + ".mp3");
        File thumb = new File(context.getExternalFilesDir(null) + "/" + video.getHash() + ".jpg");
        audio.delete();
        thumb.delete();
        return Video.delete(video);

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

    public boolean isCurrentlyDownloading(OazaApp oazaApp) {
        /*if (oazaApp.downloadService != null) {
            DownloadService downloadService = oazaApp.downloadService;
            Video currentDownload = downloadService.getCurrentDownload();
            return currentDownload.serverId == this.serverId;
        }*/
        return false;
    }

    public void addToDownloadQueue() {
        this.setInDownloadQueue(true);
        this.save();
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public void setInDownloadQueue(boolean inDownloadQueue) {
        this.inDownloadQueue = inDownloadQueue;
    }

    public boolean isInDownloadQueue() {
        return inDownloadQueue;
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

    public boolean isDownloaded() {
        return downloaded;
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
        thumbColor = in.readString();
        views = in.readInt();
        categories = in.readString();
        descriptionCS = in.readString();
        descriptionEN = in.readString();
        downloaded = in.readByte() != 0x00;
        inDownloadQueue = in.readByte() != 0x00;
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
        dest.writeString(thumbColor);
        dest.writeInt(views);
        dest.writeString(categories);
        dest.writeString(descriptionCS);
        dest.writeString(descriptionEN);
        dest.writeByte((byte) (downloaded ? 0x01 : 0x00));
        dest.writeByte((byte) (inDownloadQueue ? 0x01 : 0x00));
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