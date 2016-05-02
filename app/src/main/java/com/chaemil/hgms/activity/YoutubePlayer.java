package com.chaemil.hgms.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.factory.ResponseFactory;
import com.chaemil.hgms.model.LiveStream;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.Constants;
import com.chaemil.hgms.utils.LocalUtils;
import com.chaemil.hgms.utils.NetworkUtils;
import com.chaemil.hgms.utils.ShareUtils;
import com.crashlytics.android.Crashlytics;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

/**
 * Created by chaemil on 16.4.16.
 */
public class YoutubePlayer extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener,
        View.OnClickListener, RequestFactoryListener {

    private static final int RECOVERY_REQUEST = 1;
    public static final String LIVESTREAM = "livestream";
    private YouTubePlayerView youTubeView;
    private ImageView back;
    private LiveStream liveStream;
    private Timer liveRequestTimer;
    private TextView bottomText;
    private YouTubePlayer player;
    private ImageView share;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_player);

        liveStream = getIntent().getExtras().getParcelable(LIVESTREAM);

        getUI();
        setupUI();
        setupLiveRequestTimer();

        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.LIVESTREAM);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((OazaApp) getApplication()).appVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((OazaApp) getApplication()).appVisible = false;
    }

    private void setupUI() {
        back.setOnClickListener(this);
        youTubeView.initialize(Constants.YOUTUBE_API_KEY, this);
        share.setOnClickListener(this);

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
        share = (ImageView) findViewById(R.id.share);
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
        this.player = player;
        if (!wasRestored) {
            player.cueVideo(liveStream.getYoutubeLink());
            player.play();
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
            case R.id.share:
                ShareUtils.shareLink(this, Constants.LIVESTREAM_LINK,
                        getString(R.string.live_stream_share),
                        getString(R.string.share_live_stream));
                break;
        }
    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        switch (requestType) {
            case GET_LIVESTREAM:
                LiveStream responseLiveStream = ResponseFactory.parseLiveStream(response);

                if (responseLiveStream != null) {

                    if (responseLiveStream.getOnAir()) {

                        String oldVideoLink = liveStream.getYoutubeLink();
                        String newVideoLink = responseLiveStream.getYoutubeLink();

                        if (!oldVideoLink.equals(newVideoLink) && !newVideoLink.equals("")) {
                            player.cueVideo(newVideoLink);
                        }

                        liveStream.setOnAir(responseLiveStream.getOnAir());
                        liveStream.setYoutubeLink(responseLiveStream.getYoutubeLink());
                        liveStream.setBottomTextCS(responseLiveStream.getBottomTextCS());
                        liveStream.setBottomTextEN(responseLiveStream.getBottomTextEN());

                        setupBottomText();
                    } else {
                        SuperToast.create(this, getString(R.string.stream_has_ended), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {
        BaseActivity.responseError(exception, this);
    }
}
