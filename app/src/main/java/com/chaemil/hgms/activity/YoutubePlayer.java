package com.chaemil.hgms.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.LiveStream;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.Constants;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONObject;

/**
 * Created by chaemil on 16.4.16.
 */
public class YoutubePlayer extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener, View.OnClickListener, RequestFactoryListener {

    private static final int RECOVERY_REQUEST = 1;
    public static final String LIVESTREAM = "livestream";
    private YouTubePlayerView youTubeView;
    private ImageView back;
    private LiveStream liveStream;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_player);

        liveStream = getIntent().getExtras().getParcelable(LIVESTREAM);

        getUI();
        setupUI();
    }

    private void setupUI() {
        back.setOnClickListener(this);
        youTubeView.initialize(Constants.YOUTUBE_API_KEY, this);
    }

    private void getUI() {
        back = (ImageView) findViewById(R.id.back);
        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);

    }

    private void getLiveStream() {
        JsonObjectRequest request = RequestFactory.getLiveStream(this);
        MyRequestService.getRequestQueue().add(request);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            player.cueVideo(liveStream.getYoutubeLink());
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        switch (requestType) {
            case GET_LIVESTREAM:
                liveStream = ResponseFactory.parseLiveStream(response);

                if (liveStream != null) {

                }
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError exception) {
        BaseActivity.responseError(exception, this);
    }
}
