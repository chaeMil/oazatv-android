package com.chaemil.hgms.model;

/**
 * Created by chaemil on 24.1.16.
 */
public class Photo {

    private String originalFile;
    private String thumb128;
    private String thumb256;
    private String thumb512;
    private String thumb1024;
    private String thumb2048;
    private String descriptionCS;
    private String descriptionEN;

    public Photo(String originalFile, String thumb128, String thumb256, String thumb512, String thumb1024, String thumb2048, String descriptionCS, String descriptionEN) {
        this.originalFile = originalFile;
        this.thumb128 = thumb128;
        this.thumb256 = thumb256;
        this.thumb512 = thumb512;
        this.thumb1024 = thumb1024;
        this.thumb2048 = thumb2048;
        this.descriptionCS = descriptionCS;
        this.descriptionEN = descriptionEN;
    }

    public String getOriginalFile() {
        return originalFile;
    }

    public String getThumb128() {
        return thumb128;
    }

    public String getThumb256() {
        return thumb256;
    }

    public String getThumb512() {
        return thumb512;
    }

    public String getThumb1024() {
        return thumb1024;
    }

    public String getThumb2048() {
        return thumb2048;
    }

    public String getDescriptionCS() {
        return descriptionCS;
    }

    public String getDescriptionEN() {
        return descriptionEN;
    }
}
