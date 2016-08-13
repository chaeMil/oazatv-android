package com.chaemil.hgms.factory;


import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.Category;
import com.chaemil.hgms.model.Homepage;
import com.chaemil.hgms.model.LiveStream;
import com.chaemil.hgms.model.Photo;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.SmartLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResponseFactory {


    public static ArrayList<ArchiveItem> parseSearch(JSONObject response) {

        try {

            if (response.has(Constants.JSON_SEARCH)) {
                if (response.get(Constants.JSON_SEARCH) instanceof JSONArray) {

                    ArrayList<ArchiveItem> result = new ArrayList<>();

                    JSONArray jsonArray = response.getJSONArray(Constants.JSON_SEARCH);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        result.add(parseArchiveItem(jsonArray.getJSONObject(i)));

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
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        if (response != null) {
            return gson.fromJson(response.toString(), Video.class);
        } else {
            return null;
        }
    }

    public static PhotoAlbum parseAlbum(JSONObject response) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        if (response != null) {
            PhotoAlbum album = gson.fromJson(response.toString(), PhotoAlbum.class);
            ArrayList<Photo> photos = parsePhotos(response);
            album.setPhotos(photos);
            return album;
        } else {
            return null;
        }

    }

    public static ArrayList<Photo> parsePhotos(JSONObject response) {

        ArrayList<Photo> photos = null;

        try {
            JSONArray jsonPhotos = response
                    .getJSONObject(Constants.JSON_TYPE_ALBUM)
                    .getJSONArray(Constants.JSON_PHOTOS);

            photos = new ArrayList<>();

            for (int i = 0; i < jsonPhotos.length(); i++) {

                photos.add(parsePhoto(jsonPhotos.getJSONObject(i)));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return photos;

    }

    public static Photo parsePhoto(JSONObject response) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        if (response != null) {
            return gson.fromJson(response.toString(), Photo.class);
        } else {
            return null;
        }
    }

    private static ArchiveItem parseArchiveItem(JSONObject response) {
        ArchiveItem archiveItem = new ArchiveItem();
        try {
            switch (response.getString(Constants.JSON_TYPE)) {

                case Constants.JSON_TYPE_VIDEO:

                    Video video = parseVideo(response);
                    archiveItem.setVideo(video);

                    break;

                case Constants.JSON_TYPE_ALBUM:

                    PhotoAlbum photoAlbum = parseAlbum(response);
                    archiveItem.setAlbum(photoAlbum);

                    break;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return archiveItem;
    }

    public static ArrayList<ArchiveItem> parseArchive(JSONObject response) {

        try {
            if (response.has(Constants.JSON_ARCHIVE)) {

                JSONArray archiveJson = response.getJSONArray(Constants.JSON_ARCHIVE);

                ArrayList<ArchiveItem> archive = new ArrayList<>();

                for (int c = 0; c < archiveJson.length(); c++) {
                    archive.add(parseArchiveItem(archiveJson.getJSONObject(c)));
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

                    featured.add(parseArchiveItem(featuredJsonArray.getJSONObject(i)));
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