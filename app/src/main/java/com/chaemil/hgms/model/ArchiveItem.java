package com.chaemil.hgms.model;

/**
 * Created by chaemil on 18.12.15.
 */
public class ArchiveItem {

    private int type;
    private Video video;

    public static class Type {
        public static int VIDEO = 0;
        public static int AUDIO = 1;
        public static int ALBUM = 2;
    }

    public ArchiveItem(int type) {
        this.type = type;
    }

    public boolean equals(Object other) {

        if (other instanceof  ArchiveItem) {

            if (((ArchiveItem) other).getType() == this.getType()) {

                switch (((ArchiveItem) other).getType()) {

                    case 0:
                        if (((ArchiveItem) other).getVideo().getServerId() == this.getVideo().getServerId()) {
                            return true;
                        }
                        break;
                    case 1:
                        //TODO!!!
                        break;
                    default:
                        return false;
                }

            }

        }

        return false;

    }

    public int getType() {
        return type;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }
}
