package com.chaemil.hgms.model;

import android.content.Context;

import com.chaemil.hgms.utils.Constants;
import com.orm.SugarRecord;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by chaemil on 18.12.15.
 */
public class Video extends SugarRecord {

    public static int NOT_DOWNLOADED = 0;
    public static int IN_DOWNLOAD_QUEUE = 1;
    public static int DOWNLOADED = 2;

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
    private boolean inDownloadQueue;
    private int currentTime;

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

    public static int getDownloadStatus(int serverId) {
        Video savedVideo = findByServerId(serverId);
        if (savedVideo != null) {
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
        List<Video> list = Video.find(Video.class, "in_download_queue = ? AND downloaded = ?", String.valueOf(1), String.valueOf(0));
        ArrayList<Video> queue = new ArrayList<>();
        queue.addAll(list);

        return queue;
    }

    public static List<Video> getWholeDownloadQueue() {
        List<Video> list = Video.find(Video.class, "in_download_queue = 1 OR downloaded = 1");
        ArrayList<Video> queue = new ArrayList<>();
        queue.addAll(list);

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

    public static boolean deleteDownloadedVideo(Context context, Video video) {
        File audio = new File(context.getExternalFilesDir(null) + "/" + video.getHash() + ".mp3");
        File thumb = new File(context.getExternalFilesDir(null) + "/" + video.getHash() + ".jpg");
        audio.delete();
        thumb.delete();
        return Video.delete(video);

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
}
