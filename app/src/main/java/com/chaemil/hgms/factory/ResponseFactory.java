package com.chaemil.hgms.factory;


import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.Category;
import com.chaemil.hgms.model.Homepage;
import com.chaemil.hgms.model.LiveStream;
import com.chaemil.hgms.model.Photo;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.Constants;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ResponseFactory {


    public static ArrayList<ArchiveItem> parseSearch(JSONObject response) {

        try {

            if (response.has(Constants.JSON_SEARCH)) {
                if (response.get(Constants.JSON_SEARCH) instanceof JSONObject) {

                    ArrayList<ArchiveItem> result = new ArrayList<>();

                    JSONArray jsonVideos = response.getJSONObject(Constants.JSON_SEARCH)
                            .getJSONArray(Constants.JSON_VIDEOS);

                    for (int v = 0; v < jsonVideos.length(); v++) {
                        Video video = parseVideo(jsonVideos.getJSONObject(v));
                        ArchiveItem archiveItem = new ArchiveItem();
                        archiveItem.setVideo(video);
                        result.add(archiveItem);
                    }

                    JSONArray jsonAlbums = response.getJSONObject(Constants.JSON_SEARCH)
                            .getJSONArray(Constants.JSON_ALBUMS);

                    for (int a = 0; a < jsonAlbums.length(); a++) {
                        PhotoAlbum photoAlbum = parseAlbum(jsonAlbums.getJSONObject(a));
                        ArchiveItem archiveItem = new ArchiveItem();
                        archiveItem.setAlbum(photoAlbum);
                        result.add(archiveItem);
                    }

                    return result;

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static Video parseVideo(JSONObject response) {

        try {

            if (response.has(Constants.JSON_TYPE_VIDEO)) {
                response = response.getJSONObject(Constants.JSON_TYPE_VIDEO);
            }

            int serverId = response.getInt(Constants.JSON_ID);
            String hash = response.getString(Constants.JSON_HASH);
            String date = response.getString(Constants.JSON_DATE);
            String nameCS = response.getString(Constants.JSON_NAME_CS);
            String nameEN = response.getString(Constants.JSON_NAME_EN);
            String tags = response.getString(Constants.JSON_TAGS);
            String videoFile = response.getString(Constants.JSON_MP4_FILE);
            String audioFile = response.getString(Constants.JSON_MP3_FILE);
            String thumbFile = response.getString(Constants.THUMB_FILE);
            int views = response.getInt(Constants.JSON_VIEWS);
            String categories = response.getString(Constants.JSON_CATEGORIES);
            String descriptionCS = response.getString(Constants.JSON_DESCRIPTION_CS);
            String descriptionEN = response.getString(Constants.JSON_DESCRIPTION_EN);

            return new Video(serverId, hash, date, nameCS, nameEN, tags, videoFile, audioFile,
                    thumbFile, views, categories, descriptionCS, descriptionEN, false);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static PhotoAlbum parseAlbum(JSONObject response) {

        try  {
            if (response.has(Constants.JSON_TYPE_ALBUM)) {
                response = response.getJSONObject(Constants.JSON_TYPE_ALBUM);
            }

            int serverId = response.getInt(Constants.JSON_ID);
            String hash = response.getString(Constants.JSON_HASH);
            String date = response.getString(Constants.JSON_DATE);
            String nameCS = response.getString(Constants.JSON_NAME_CS);
            String nameEN = response.getString(Constants.JSON_NAME_EN);
            String tags = response.getString(Constants.JSON_TAGS);
            String descriptionCS = response.getString(Constants.JSON_DESCRIPTION_CS);
            String descriptionEN = response.getString(Constants.JSON_DESCRIPTION_EN);
            Photo thumb = parsePhoto(response.getJSONObject(Constants.JSON_THUMBS));
            JSONArray jsonPhotos = new JSONArray();
            if (response.has(Constants.JSON_PHOTOS)) {
                jsonPhotos = response.getJSONArray(Constants.JSON_PHOTOS);
            }

            ArrayList<Photo> photos = new ArrayList<>();

            for(int c = 0; c < jsonPhotos.length(); c++) {

                Photo photo = parsePhoto(jsonPhotos.getJSONObject(c));
                photos.add(photo);

            }

            return new PhotoAlbum(serverId, hash, date, nameCS, nameEN, tags, descriptionCS,
                    descriptionEN, thumb, photos);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static Photo parsePhoto(JSONObject response) {

        try {

            String descriptionCS = "";
            if (response.has(Constants.JSON_DESCRIPTION_CS)) {
                descriptionCS = response.getString(Constants.JSON_DESCRIPTION_CS);
            }
            String descriptionEN = "";
            if (response.has(Constants.JSON_DESCRIPTION_EN)) {
                descriptionEN = response.getString(Constants.JSON_DESCRIPTION_EN);
            }
            String originalFile = response.getString(Constants.JSON_ORIGINAL_FILE);
            String thumb128 = response.getString(Constants.JSON_THUMB + "_128");
            String thumb256 = response.getString(Constants.JSON_THUMB + "_256");
            String thumb512 = response.getString(Constants.JSON_THUMB + "_512");
            String thumb1024 = response.getString(Constants.JSON_THUMB + "_1024");
            String thumb2048 = response.getString(Constants.JSON_THUMB + "_2048");

            return new Photo(originalFile, thumb128, thumb256, thumb512, thumb1024, thumb2048,
                    descriptionCS, descriptionEN);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ArrayList<ArchiveItem> parseArchive(JSONObject response) {

        try {
            if (response.has(Constants.JSON_ARCHIVE)) {

                JSONArray archiveJson = response.getJSONArray(Constants.JSON_ARCHIVE);

                ArrayList<ArchiveItem> archive = new ArrayList<>();

                for (int c = 0; c < archiveJson.length(); c++) {

                    ArchiveItem archiveItem = new ArchiveItem();

                    switch (archiveJson.getJSONObject(c).getString(Constants.JSON_TYPE)) {

                        case Constants.JSON_TYPE_VIDEO:

                            Video video = parseVideo(archiveJson.getJSONObject(c));

                            archiveItem.setVideo(video);

                            archive.add(archiveItem);
                            break;

                        case Constants.JSON_TYPE_ALBUM:

                            PhotoAlbum photoAlbum = parseAlbum(archiveJson.getJSONObject(c));

                            archiveItem.setAlbum(photoAlbum);

                            archive.add(archiveItem);
                            break;

                    }


                }

                return archive;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static Homepage parseHomepage(JSONObject response) {
        try {

            ArrayList<Video> newestVideos = new ArrayList<>();
            ArrayList<Video> popularVideos = new ArrayList<>();
            ArrayList<PhotoAlbum> newestAlbums = new ArrayList<>();
            ArrayList<ArchiveItem> featured = new ArrayList<>();

            if (response.has(Constants.JSON_NEWEST_VIDEOS)) {
                JSONArray newestVideosJsonArray = response.getJSONArray(Constants.JSON_NEWEST_VIDEOS);
                for(int i = 0; i < newestVideosJsonArray.length(); i++) {
                    Video video = parseVideo(newestVideosJsonArray.getJSONObject(i));
                    newestVideos.add(video);
                }
            }

            if (response.has(Constants.JSON_POPULAR_VIDEOS)) {
                JSONArray popularVideosJsonArray = response.getJSONArray(Constants.JSON_POPULAR_VIDEOS);
                for(int i = 0; i < popularVideosJsonArray.length(); i++) {
                    Video video = parseVideo(popularVideosJsonArray.getJSONObject(i));
                    popularVideos.add(video);
                }
            }

            if (response.has(Constants.JSON_NEWEST_ALBUMS)) {
                JSONArray newestAlbumsJsonArray = response.getJSONArray(Constants.JSON_NEWEST_ALBUMS);
                for(int i = 0; i < newestAlbumsJsonArray.length(); i++) {
                    PhotoAlbum album = parseAlbum(newestAlbumsJsonArray.getJSONObject(i));
                    newestAlbums.add(album);
                }
            }

            if (response.has(Constants.JSON_FEATURED)) {
                JSONArray featuredJsonArray = response.getJSONArray(Constants.JSON_FEATURED);
                for(int i = 0; i < featuredJsonArray.length(); i++) {

                    ArchiveItem archiveItem = new ArchiveItem();
                    switch (featuredJsonArray.getJSONObject(i).getString(Constants.JSON_TYPE)) {
                        case Constants.JSON_TYPE_VIDEO:

                            Video video = parseVideo(featuredJsonArray.getJSONObject(i));
                            archiveItem.setVideo(video);
                            featured.add(archiveItem);
                            break;

                        case Constants.JSON_TYPE_ALBUM:

                            PhotoAlbum photoAlbum = parseAlbum(featuredJsonArray.getJSONObject(i));
                            archiveItem.setAlbum(photoAlbum);
                            featured.add(archiveItem);
                            break;

                    }
                }
            }

            return new Homepage(newestVideos, newestAlbums, popularVideos, featured);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static LiveStream parseLiveStream(JSONObject response) {
        try {
            String onAir = response.getString(Constants.JSON_ONAIR);
            String youtubeLink = response.getString(Constants.JSON_YOUTUBE_LINK);
            String bottomTextCS = response.getString(Constants.JSON_BOTTOM_TEXT_CS);
            String bottomTextEN = response.getString(Constants.JSON_BOTTOM_TEXT_EN);

            return new LiveStream(onAir, youtubeLink, bottomTextCS, bottomTextEN);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ArrayList<Category> parseCategories(JSONObject response) {
        try {
            JSONArray jsonArray = response.getJSONArray(Constants.JSON_CATEGORIES);
            ArrayList<Category> categories = new ArrayList<>();
            for(int i = 0; i < jsonArray.length(); i++) {
                Category category = parseCategory(jsonArray.getJSONObject(i));
                categories.add(category);
            }

            return categories;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Category parseCategory(JSONObject response) {

        try {
            int id = response.getInt(Constants.JSON_ID);
            String nameCS = response.getString(Constants.JSON_NAME_CS);
            String nameEN = response.getString(Constants.JSON_NAME_EN);
            String color = response.getString(Constants.JSON_COLOR);
            JSONArray jsonVideos = response.getJSONArray(Constants.JSON_VIDEOS);
            ArrayList<Video> videos = new ArrayList<>();
            for(int i = 0; i < jsonVideos.length(); i++) {
                Video video = parseVideo(jsonVideos.getJSONObject(i));
                videos.add(video);
            }

            return new Category(id, nameCS, nameEN, color, videos);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}