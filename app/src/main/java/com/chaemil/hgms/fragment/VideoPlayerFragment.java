package com.chaemil.hgms.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.factory.RequestFactory;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.MyRequestService;
import com.chaemil.hgms.utils.BitmapUtils;
import com.chaemil.hgms.utils.DimensUtils;
import com.chaemil.hgms.utils.OnSwipeTouchListener;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import at.markushi.ui.CircleButton;

/**
 * Created by chaemil on 2.12.15.
 */
public class VideoPlayerFragment extends Fragment implements View.OnClickListener, View.OnTouchListener,
        MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener, RequestFactoryListener {

    public static final String TAG = "player_fragment";
    private static final String IMAGES_ALREADY_BLURRED = "images_already_blurred";
    private static final String BG_DRAWABLE = "bg_drawable";
    private static final String CURRENT_TIME = "current_time";
    private ImageView playerBg;
    private RelativeLayout miniPlayer;
    private ImageView miniPlayerImageView;
    private RelativeLayout playerToolbar;
    private boolean imagesAlreadyBlurred = false;
    private BitmapDrawable bgDrawable;
    private TextView miniPlayerText;
    private TextView playerTitle;
    private VideoView videoView;
    private CircleButton playPause;
    private CircleButton rew;
    private CircleButton ff;
    private TextView currentTime;
    private TextView totalTime;
    private int duration;
    private int currentTimeInt;
    private AppCompatSeekBar seekBar;
    private Bitmap thumb;
    private Video currentVideo;
    private CircleButton miniPlayerPause;
    private ProgressBar bufferBar;
    private int bufferFail;
    private RelativeLayout controlsWrapper;
    private RelativeLayout videoWrapper;
    private ViewGroup rootView;
    private ImageView fullscreen;
    private RelativeLayout.LayoutParams videoWrapperParamsFullscreen;
    private RelativeLayout.LayoutParams videoWrapperParamsNormal;
    public boolean isInFullscreenMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public void onPause() {
        super.onPause();

        videoView.pause();

        saveCurrentVideoTime();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (currentVideo != null) {
            videoView.seekTo(currentVideo.getCurrentTime());
        }

        playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play));

        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.VIDEOPLAYER_FRAGMENT + "videoHash: " + currentVideo.getHash());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.video_player_fragment, container, false);

        if (savedInstanceState != null) {
            imagesAlreadyBlurred = savedInstanceState.getBoolean(IMAGES_ALREADY_BLURRED);

            bgDrawable = new BitmapDrawable(getResources(),
                    (Bitmap) savedInstanceState.getParcelable(BG_DRAWABLE));
        }

        getUI(rootView);
        activateUI(false);
        setupUI();

        return rootView;
    }

    private void postVideoView() {
        JsonObjectRequest postView = RequestFactory.postVideoView(this, currentVideo.getHash());
        MyRequestService.getRequestQueue().add(postView);
    }

    private void activateUI(boolean state) {
        playPause.setEnabled(state);
        rew.setEnabled(state);
        ff.setEnabled(state);
        seekBar.setEnabled(state);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IMAGES_ALREADY_BLURRED, imagesAlreadyBlurred);
        if (bgDrawable != null) {
            outState.putParcelable(BG_DRAWABLE, BitmapUtils.drawableToBitmap(bgDrawable));
        }
        outState.putInt(CURRENT_TIME, videoView.getCurrentPosition());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            videoView.seekTo(savedInstanceState.getInt(CURRENT_TIME));
        }
    }

    private void getUI(ViewGroup rootView) {
        miniPlayer = (RelativeLayout) rootView.findViewById(R.id.mini_player);
        playerBg = (ImageView) rootView.findViewById(R.id.player_bg);
        miniPlayerImageView = (ImageView) rootView.findViewById(R.id.mini_player_image);
        miniPlayerText = (TextView) rootView.findViewById(R.id.mini_player_text);
        playerToolbar = (RelativeLayout) rootView.findViewById(R.id.toolbar);
        playerTitle = (TextView) rootView.findViewById(R.id.player_title);
        videoView = (VideoView) rootView.findViewById(R.id.video_view);
        playPause = (CircleButton) rootView.findViewById(R.id.play_pause);
        rew = (CircleButton) rootView.findViewById(R.id.rew);
        ff = (CircleButton) rootView.findViewById(R.id.ff);
        currentTime = (TextView) rootView.findViewById(R.id.current_time);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        seekBar = (AppCompatSeekBar) rootView.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        miniPlayerPause = (CircleButton) rootView.findViewById(R.id.mini_play_pause);
        bufferBar = (ProgressBar) rootView.findViewById(R.id.buffer_bar);
        controlsWrapper = (RelativeLayout) rootView.findViewById(R.id.controls_wrapper);
        videoWrapper = (RelativeLayout) rootView.findViewById(R.id.video_wrapper);
        fullscreen = (ImageView) rootView.findViewById(R.id.fullscreen);
    }

    private void setupUI() {
        playPause.setOnClickListener(this);
        rew.setOnClickListener(this);
        ff.setOnClickListener(this);
        videoView.setOnTouchListener(this);
        videoView.setOnPreparedListener(this);
        miniPlayerPause.setOnClickListener(this);
        fullscreen.setOnClickListener(this);

        int bottomMargin = (int) DimensUtils.pxFromDp(getActivity(),
                getResources().getInteger(R.integer.video_player_wrapper_bottom_margin));

        videoWrapperParamsNormal = (RelativeLayout.LayoutParams) videoWrapper.getLayoutParams();
        videoWrapperParamsNormal.setMargins(16, 16, 16, bottomMargin);  // left, top, right, bottom
        videoWrapper.setLayoutParams(videoWrapperParamsNormal);

        videoWrapperParamsFullscreen = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        videoWrapperParamsFullscreen.setMargins(0, 0, 0, 0);  // left, top, right, bottom

        miniPlayer.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                swipeDismissPlayer(true);
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                swipeDismissPlayer(false);
            }
        });
    }

    private void swipeDismissPlayer(boolean right) {
        saveCurrentVideoTime();

        videoView.pause();
        if (right) {
            YoYo.with(Techniques.SlideOutRight).duration(300).playOn(miniPlayer);
        } else {
            YoYo.with(Techniques.SlideOutLeft).duration(300).playOn(miniPlayer);
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) getActivity()).hidePanel();
            }
        }, 300);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.play_pause:
                playPauseVideo();
                break;
            case R.id.rew:
                if (videoView.canSeekBackward()) {
                    videoView.seekTo(videoView.getCurrentPosition() - 10000);
                }
                break;
            case R.id.ff:
                if (videoView.canSeekForward()) {
                    videoView.seekTo(videoView.getCurrentPosition() + 10000);
                }
                break;
            case R.id.mini_play_pause:
                playPauseVideo();
                break;
            case R.id.fullscreen:
                requestFullscreenPlayer();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(v.getId()) {
            case R.id.video_view:
                toggleControls(true);
                ((BaseActivity) getActivity()).setFullscreen(false);

                if (videoView.isPlaying()) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (videoView.isPlaying() && isInFullscreenMode) {
                                requestFullscreenPlayer();
                                toggleControls(false);
                            }
                        }
                    }, 3000);
                }
                break;
        }

        return true;
    }

    public void requestFullscreenPlayer() {
        ((BaseActivity) getActivity()).setFullscreen(true);
        getActivity().getWindow().getDecorView()
                .setBackgroundColor(getResources().getColor(R.color.black));

        playerToolbar.setVisibility(View.GONE);
        playerBg.setVisibility(View.GONE);

        videoWrapper.setLayoutParams(videoWrapperParamsFullscreen);
        toggleControls(false);

        ((MainActivity) getActivity()).getMainRelativeLayout().setFitsSystemWindows(false);

        isInFullscreenMode = true;
    }

    public void cancelFullscreenPlayer() {
        ((BaseActivity) getActivity()).setFullscreen(false);
        getActivity().getWindow().getDecorView()
                .setBackgroundColor(getResources().getColor(R.color.white));

        playerToolbar.setVisibility(View.VISIBLE);
        playerBg.setVisibility(View.VISIBLE);

        videoWrapper.setLayoutParams(videoWrapperParamsNormal);
        toggleControls(true);

        ((MainActivity) getActivity()).getMainRelativeLayout().setFitsSystemWindows(true);

        isInFullscreenMode = false;
    }

    public void toggleControls(boolean visible) {
        if (visible) {
            if (controlsWrapper.getVisibility() != View.VISIBLE) {
                controlsWrapper.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.BounceInUp).duration(400).playOn(controlsWrapper);
            }
        } else {
            if (controlsWrapper.getVisibility() != View.GONE) {
                YoYo.with(Techniques.FadeOutDown).duration(400).playOn(controlsWrapper);
                YoYo.with(Techniques.FadeIn).duration(400).playOn(seekBar);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        controlsWrapper.setVisibility(View.GONE);
                    }
                }, 400);
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        duration = videoView.getDuration();
        seekBar.setMax(duration);
        seekBar.postDelayed(onEverySecond, 1000);
        YoYo.with(Techniques.FadeIn).duration(350).delay(250).playOn(videoView);

        mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {

                float temp = ((float) mp.getCurrentPosition() / (float) mp.getDuration()) * 100;
                if (Math.abs(percent - temp) < 1) {
                    bufferFail++;
                    if (bufferFail == 15) {
                        SmartLog.Log(SmartLog.LogLevel.WARN, "bufferFail", "buffering failed");
                    }
                }
            }
        });

        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    bufferBar.setVisibility(View.VISIBLE);
                    saveCurrentVideoTime();
                }
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    bufferBar.setVisibility(View.GONE);
                    saveCurrentVideoTime();
                }
                return false;
            }
        });
    }

    private Runnable onEverySecond = new Runnable() {
        @Override
        public void run(){
            if (seekBar != null) {
                seekBar.setProgress(videoView.getCurrentPosition());
            }

            if (videoView.isPlaying()) {
                if (seekBar != null) {
                    seekBar.postDelayed(onEverySecond, 1000);
                }
                updateTime();
            }
        }
    };

    private void updateTime() {
        currentTimeInt = videoView.getCurrentPosition();

        int dSeconds = (duration / 1000) % 60 ;
        int dMinutes = ((duration / (1000*60)) % 60);
        int dHours   = ((duration / (1000*60*60)) % 24);

        int cSeconds = (currentTimeInt / 1000) % 60 ;
        int cMinutes = ((currentTimeInt / (1000*60)) % 60);
        int cHours   = ((currentTimeInt / (1000*60*60)) % 24);

        if(dHours == 0){
            currentTime.setText(String.format("%02d:%02d", cMinutes, cSeconds));
            totalTime.setText(String.format("%02d:%02d", dMinutes, dSeconds));
        } else{
            currentTime.setText(String.format("%02d:%02d:%02d", cHours, cMinutes, cSeconds));
            totalTime.setText(String.format("%02d:%02d:%02d", dHours, dMinutes, dSeconds));
        }
    }

    private void playPauseVideo() {
        saveCurrentVideoTime();

        if (videoView.isPlaying()) {
            videoView.pause();
            playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
            miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
        } else {
            videoView.start();
            playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            if (seekBar != null) {
                seekBar.postDelayed(onEverySecond, 1000);
            }
        }
    }

    public void switchMiniPlayer(boolean show) {
        if (isAdded()) {
            if (show) {
                playerToolbar.setVisibility(View.GONE);
                miniPlayer.setVisibility(View.VISIBLE);
            } else {
                playerToolbar.setVisibility(View.VISIBLE);
                miniPlayer.setVisibility(View.GONE);
            }
        }
    }

    public void saveCurrentVideoTime() {
        if (videoView != null && currentVideo != null) {
            try {
                currentVideo.setCurrentTime(videoView.getCurrentPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentVideo.save();
        }
    }

    public void playNewVideo(final Video video) {

        saveCurrentVideoTime();

        Video savedVideo = null;

        try {
            savedVideo = Video.findByServerId(video.getServerId());
        } catch (Exception e) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
        }

        if (savedVideo != null) {
            this.currentVideo = savedVideo;

            SuperToast.create(getActivity(),
                    getString(R.string.resuming_from_saved_time),
                    SuperToast.Duration.SHORT).show();
        } else {
            this.currentVideo = video;
        }

        Ion.with(getActivity()).load(currentVideo.getThumbFile()).intoImageView(miniPlayerImageView);

        playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

        miniPlayerText.setText(video.getName());
        playerTitle.setText(video.getName());
        videoView.setAlpha(0);

        currentTime.setText("00:00:00");
        totalTime.setText("???");

        ((MainActivity) getActivity()).expandPanel();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                imagesAlreadyBlurred = false;
                bgDrawable = null;

                resizeAndBlurBg();

                activateUI(true);
                videoView.stopPlayback();
                videoView.setVideoPath(video.getVideoFile());
                videoView.start();
                videoView.seekTo(currentVideo.getCurrentTime());

            }
        }, 500);

        postVideoView();

    }

    private void resizeAndBlurBg() {
        new ComputeImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public RelativeLayout getPlayerToolbar() {
        return playerToolbar;
    }

    public RelativeLayout getMiniPlayer() {
        return miniPlayer;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            videoView.seekTo(progress);
            updateTime();
            saveCurrentVideoTime();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {
        switch(requestType) {
            case POST_VIDEO_VIEW:
                SmartLog.Log(SmartLog.LogLevel.DEBUG, "postedVideoView", "ok");
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError exception) {
        BaseActivity.responseError(exception, getActivity());
    }

    private class ComputeImage extends AsyncTask {


        @Override
        protected Object doInBackground(Object[] params) {

            try {
                thumb = BitmapUtils.getBitmapFromURL(currentVideo.getThumbFile());
                if (thumb == null) {
                    thumb = BitmapUtils.drawableToBitmap(getResources().getDrawable(R.drawable.placeholder));
                }
            } catch (Exception e) {
                SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
            }

            if (!imagesAlreadyBlurred && thumb != null) {
                SmartLog.Log(SmartLog.LogLevel.DEBUG, "resizeAndBlurBg", "blurring bg image");
                Bitmap originalBitmap = thumb;
                Bitmap blurredPlayerBitmap = BitmapUtils.blur(getContext(), originalBitmap, 25);
                Bitmap resizedBitmap = BitmapUtils.resizeImageForImageView(blurredPlayerBitmap, 255);
                bgDrawable = new BitmapDrawable(getResources(), resizedBitmap);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            YoYo.with(Techniques.FadeOut).duration(400).playOn(playerBg);
            YoYo.with(Techniques.FadeOut).duration(400).playOn(miniPlayerImageView);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playerBg.setImageDrawable(bgDrawable);

                    YoYo.with(Techniques.FadeIn).duration(400).playOn(playerBg);
                    YoYo.with(Techniques.FadeIn).duration(400).playOn(miniPlayerImageView);
                }
            }, 400);

            imagesAlreadyBlurred = true;
        }
    }
}
