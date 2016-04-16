package com.chaemil.hgms.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chaemil on 16.4.16.
 */
public class LiveStream implements Parcelable {

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

    protected LiveStream(Parcel in) {
        onAir = in.readString();
        youtubeLink = in.readString();
        bottomTextCS = in.readString();
        bottomTextEN = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(onAir);
        dest.writeString(youtubeLink);
        dest.writeString(bottomTextCS);
        dest.writeString(bottomTextEN);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<LiveStream> CREATOR = new Parcelable.Creator<LiveStream>() {
        @Override
        public LiveStream createFromParcel(Parcel in) {
            return new LiveStream(in);
        }

        @Override
        public LiveStream[] newArray(int size) {
            return new LiveStream[size];
        }
    };
}
