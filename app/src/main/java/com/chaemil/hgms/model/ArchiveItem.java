package com.chaemil.hgms.model;

/**
 * Created by chaemil on 18.12.15.
 */
public class ArchiveItem {

    private int type;
    private Video video;

    public static abstract class Type {
        public static final int VIDEO = 0;
        public static final int AUDIO = 1;
        public static final int ALBUM = 2;
    }

    public ArchiveItem() {
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
