package com.chaemil.hgms.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.chaemil.hgms.utils.LocalUtils;
import com.chaemil.hgms.utils.NetworkUtils;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chaemil on 16.4.16.
 */
public class YoutubePlayer extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener, View.OnClickListener, RequestFactoryListener {

    private static final int RECOVERY_REQUEST = 1;
    public static final String LIVESTREAM = "livestream";
    private YouTubePlayerView youTubeView;
    private ImageView back;
    private LiveStream liveStream;
    private Timer liveRequestTimer;
    private TextView bottomText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_player);

        liveStream = getIntent().getExtras().getParcelable(LIVESTREAM);

        getUI();
        setupUI();
        setupLiveRequestTimer();
    }

    private void setupUI() {
        back.setOnClickListener(this);
        youTubeView.initialize(Constants.YOUTUBE_API_KEY, this);

        setupBottomText();
    }

    private void setupBottomText() {
        switch (LocalUtils.getLocale()) {
            case Constants.CS:
                bottomText.setText(Html.fromHtml(liveStream.getBottomTextCS()));
                break;
            case Constants.EN:
                bottomText.setText(Html.fromHtml(liveStream.getBottomTextEN()));
                break;
        }
    }

    private void getUI() {
        back = (ImageView) findViewById(R.id.back);
        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        bottomText = (TextView) findViewById(R.id.bottom_text);
    }

    private void getLiveStream() {
        JsonObjectRequest request = RequestFactory.getLiveStream(this);
        MyRequestService.getRequestQueue().add(request);
    }

    public void setupLiveRequestTimer() {
        if (liveRequestTimer != null) {
            liveRequestTimer.cancel();
        }
        if (NetworkUtils.isConnected(this)) {
            liveRequestTimer = new Timer();
            liveRequestTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    getLiveStream();
                }
            }, 10 * 1000, 10 * 1000);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            player.cueVideo(liveStream.getYoutubeLink());
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.player_error), errorReason.toString());
            SuperToast.create(this, error, Toast.LENGTH_LONG).show();
        }
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
                LiveStream responseLiveStream = ResponseFactory.parseLiveStream(response);

                if (responseLiveStream != null) {
                    liveStream = responseLiveStream;

                    setupBottomText();
                }
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError exception) {
        BaseActivity.responseError(exception, this);
    }
}
