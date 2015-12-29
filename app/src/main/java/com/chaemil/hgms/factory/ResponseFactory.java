package com.chaemil.hgms.factory;


import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.SmartLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class ResponseFactory {

    public static Video parseVideo(JSONObject response) {

        try {
            if (response.getString(Constants.JSON_TYPE).equals(Constants.JSON_VIDEO)) {

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