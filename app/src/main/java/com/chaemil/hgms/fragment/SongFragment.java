package com.chaemil.hgms.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Song;
import com.chaemil.hgms.service.RequestService;

import org.json.JSONObject;

/**
 * Created by chaemil on 1.11.16.
 */

public class SongFragment extends BaseFragment {

    public static final String SONG_ID = "song_id";
    public static final String TAG = "song_fragment";
    private int songId;
    private Song song;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        songId = bundle.getInt(SONG_ID);
        if (songId == 0) {
            goBack();
        }

        getSong(songId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.song_fragment, container, false);

        getUI(rootView);
        setupUI();

        return rootView;
    }

    private void setupUI() {

    }

    private void getUI(ViewGroup rootView) {

    }

    private void getSong(int songId) {
        JsonObjectRequest getSong = RequestFactory.getSong(this, songId);
        RequestService.getRequestQueue().add(getSong);
        showProgress();
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        super.onSuccessResponse(response, requestType);

        switch (requestType) {
            case GET_SONG:
                if (response != null) {
                    Song song = ResponseFactory.parseSong(response);
                    if (song != null) {
                        this.song = song;
                    }
                }
                hideProgress();
                break;

        }
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        super.onErrorResponse(exception, requestType);
        hideProgress();
    }
}
