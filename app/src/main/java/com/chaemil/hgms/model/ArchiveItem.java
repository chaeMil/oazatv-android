package com.chaemil.hgms.model;

/**
 * Created by chaemil on 18.12.15.
 */
public class ArchiveItem {

    private int type;
    private Video video;
    private PhotoAlbum album;

    public static abstract class Type {
        public static final int VIDEO = 0;
        public static final int ALBUM = 1;
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
                        if (((ArchiveItem) other).getAlbum().getServerId() == this.getAlbum().getServerId()) {
                            return true;
                        }
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

    public PhotoAlbum getAlbum() {
        return album;
    }

    public void setAlbum(PhotoAlbum album) {
        this.album = album;
    }
}
