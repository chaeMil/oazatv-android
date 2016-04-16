package com.chaemil.hgms.model;

/**
 * Created by chaemil on 16.4.16.
 */
public class LiveStream {

    private String onAir;
    private String youtubeLink;
    private String bottomTextCS;
    private String bottomTextEN;

    public LiveStream(String onAir, String youtubeLink, String bottomTextCS, String bottomTextEN) {
        this.onAir = onAir;
        this.youtubeLink = youtubeLink;
        this.bottomTextCS = bottomTextCS;
        this.bottomTextEN = bottomTextEN;
    }

    public boolean getOnAir() {
        return this.onAir.equals("online");
    }

    public void setOnAir(String onAir) {
        this.onAir = onAir;
    }

    public String getYoutubeLink() {
        return youtubeLink;
    }

    public void setYoutubeLink(String youtubeLink) {
        this.youtubeLink = youtubeLink;
    }

    public String getBottomTextCS() {
        return bottomTextCS;
    }

    public void setBottomTextCS(String bottomTextCS) {
        this.bottomTextCS = bottomTextCS;
    }

    public String getBottomTextEN() {
        return bottomTextEN;
    }

    public void setBottomTextEN(String bottomTextEN) {
        this.bottomTextEN = bottomTextEN;
    }
}
