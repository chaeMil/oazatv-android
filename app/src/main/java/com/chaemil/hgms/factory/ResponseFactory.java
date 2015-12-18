package com.chaemil.hgms.factory;


import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class ResponseFactory {

    public static Video parseVideo(JSONObject response) {

        try {
            if (response.has(Constants.JSON_VIDEO)) {
                JSONObject videoJson = response.getJSONObject(Constants.JSON_VIDEO);

                int serverId = videoJson.getInt(Constants.JSON_ID);
                String hash = videoJson.getString(Constants.JSON_HASH);
                String date = videoJson.getString(Constants.JSON_DATE);
                String nameCS = videoJson.getString(Constants.JSON_NAME_CS);
                String nameEN = videoJson.getString(Constants.JSON_NAME_EN);
                String tags = videoJson.getString(Constants.JSON_TAGS);
                String videoFile = videoJson.getString(Constants.JSON_MP4_FILE);
                String audioFile = videoJson.getString(Constants.JSON_MP3_FILE);
                String thumbFile = videoJson.getString(Constants.THUMB_FILE);
                int views = videoJson.getInt(Constants.JSON_VIEWS);
                String categories = videoJson.getString(Constants.JSON_CATEGORIES);
                String descriptionCS = videoJson.getString(Constants.JSON_DESCRIPTION_CS);
                String descriptionEN = videoJson.getString(Constants.JSON_DESCRIPTION_EN);

                return new Video(serverId, hash, date, nameCS, nameEN, tags, videoFile, audioFile,
                        thumbFile, views, categories, descriptionCS, descriptionEN, false);
            }
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

                    Video video = parseVideo(archiveJson.getJSONObject(c));

                    ArchiveItem archiveItem = new ArchiveItem(ArchiveItem.Type.VIDEO);
                    archiveItem.setVideo(video);

                    archive.add(archiveItem);

                }

                return archive;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

}