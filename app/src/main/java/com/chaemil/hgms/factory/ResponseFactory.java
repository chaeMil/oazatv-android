package com.chaemil.hgms.factory;


import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.Photo;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.SmartLog;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class ResponseFactory {

    public static Video parseVideo(JSONObject response) {

        try {
            if (response.getString(Constants.JSON_TYPE).equals(Constants.JSON_TYPE_VIDEO)) {

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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static PhotoAlbum parseAlbum(JSONObject response) {

        try  {
            if (response.getString(Constants.JSON_TYPE).equals(Constants.JSON_TYPE_ALBUM)) {

                int serverId = response.getInt(Constants.JSON_ID);
                String hash = response.getString(Constants.JSON_HASH);
                String date = response.getString(Constants.JSON_DATE);
                String nameCS = response.getString(Constants.JSON_NAME_CS);
                String nameEN = response.getString(Constants.JSON_NAME_EN);
                String tags = response.getString(Constants.JSON_TAGS);
                String descriptionCS = response.getString(Constants.JSON_DESCRIPTION_CS);
                String descriptionEN = response.getString(Constants.JSON_DESCRIPTION_EN);
                JSONArray jsonPhotos = response.getJSONArray(Constants.JSON_PHOTOS);

                ArrayList<Photo> photos = new ArrayList<>();

                for(int c = 0; c < jsonPhotos.length(); c++) {

                    Photo photo = parsePhoto(jsonPhotos.getJSONObject(c));
                    photos.add(photo);

                }

                return new PhotoAlbum(serverId, hash, date, nameCS, nameEN, tags, descriptionCS,
                        descriptionEN, photos);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static Photo parsePhoto(JSONObject response) {

        try {

            String descriptionCS = response.getString(Constants.JSON_DESCRIPTION_CS);
            String descriptionEN = response.getString(Constants.JSON_DESCRIPTION_EN);
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

                    switch (archiveJson.getJSONObject(c).getString(Constants.JSON_TYPE)) {

                        case Constants.JSON_TYPE_VIDEO:

                            Video video = parseVideo(archiveJson.getJSONObject(c));

                            ArchiveItem archiveItem = new ArchiveItem();
                            archiveItem.setVideo(video);

                            archive.add(archiveItem);
                            break;

                        case Constants.JSON_TYPE_ALBUM:

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

}