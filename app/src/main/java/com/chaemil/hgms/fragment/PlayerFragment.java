package com.chaemil.hgms.fragment;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.BitmapUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.squareup.picasso.Picasso;

import at.markushi.ui.CircleButton;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * Created by chaemil on 2.12.15.
 */
public class PlayerFragment extends Fragment implements View.OnClickListener, View.OnTouchListener, MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener {

    public static final String TAG = "player_fragment";
    private static final String IMAGES_ALREADY_BLURRED = "images_already_blurred";
    private static final String MINI_PLAYER_DRAWABLE = "mini_player_drawable";
    private static final String BG_DRAWABLE = "bg_drawable";
    private ImageView playerBg;
    private RelativeLayout miniPlayer;
    private ImageView miniPlayerImageView;
    private Toolbar playerToolbar;
    private boolean imagesAlreadyBlurred = false;
    private BitmapDrawable miniPlayerDrawable;
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
    private SeekBar progressBar;
    private Bitmap thumb;
    private Video currentVideo;
    private CircleButton miniPlayerPause;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.player_fragment, container, false);

        if (savedInstanceState != null) {
            imagesAlreadyBlurred = savedInstanceState.getBoolean(IMAGES_ALREADY_BLURRED);

            miniPlayerDrawable = new BitmapDrawable(getResources(),
                    (Bitmap) savedInstanceState.getParcelable(MINI_PLAYER_DRAWABLE));

            bgDrawable = new BitmapDrawable(getResources(),
                    (Bitmap) savedInstanceState.getParcelable(BG_DRAWABLE));
        }

        getUI(rootView);
        setupUI();
        adjustLayout();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IMAGES_ALREADY_BLURRED, imagesAlreadyBlurred);
        outState.putParcelable(MINI_PLAYER_DRAWABLE, BitmapUtils.drawableToBitmap(miniPlayerDrawable));
        outState.putParcelable(BG_DRAWABLE, BitmapUtils.drawableToBitmap(bgDrawable));
    }

    private void getUI(ViewGroup rootView) {
        miniPlayer = (RelativeLayout) rootView.findViewById(R.id.mini_player);
        playerBg = (ImageView) rootView.findViewById(R.id.player_bg);
        miniPlayerImageView = (ImageView) rootView.findViewById(R.id.mini_player_image);
        miniPlayerText = (TextView) rootView.findViewById(R.id.mini_player_text);
        playerToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        playerTitle = (TextView) rootView.findViewById(R.id.player_title);
        videoView = (VideoView) rootView.findViewById(R.id.video);
        playPause = (CircleButton) rootView.findViewById(R.id.play_pause);
        rew = (CircleButton) rootView.findViewById(R.id.rew);
        ff = (CircleButton) rootView.findViewById(R.id.ff);
        currentTime = (TextView) rootView.findViewById(R.id.current_time);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        progressBar = (SeekBar) rootView.findViewById(R.id.progress_bar);
        progressBar.setOnSeekBarChangeListener(this);
        miniPlayerPause = (CircleButton) rootView.findViewById(R.id.mini_play_pause);
    }

    private void setupUI() {
        //resizeAndBlurBg();
        playPause.setOnClickListener(this);
        rew.setOnClickListener(this);
        ff.setOnClickListener(this);
        videoView.setOnTouchListener(this);
        videoView.setOnPreparedListener(this);
        miniPlayerPause.setOnClickListener(this);
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
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(v.getId()) {
            case R.id.video:
                //do nothing
                break;
        }

        return true;
    }

    public void adjustLayout() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {

        }
        else {

        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        duration = videoView.getDuration();
        progressBar.setMax(duration);
        progressBar.postDelayed(onEverySecond, 1000);
    }

    private Runnable onEverySecond = new Runnable() {
        @Override
        public void run(){
            if (progressBar != null) {
                progressBar.setProgress(videoView.getCurrentPosition());
            }

            if (videoView.isPlaying()) {
                if (progressBar != null) {
                    progressBar.postDelayed(onEverySecond, 1000);
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
        }else{
            currentTime.setText(String.format("%02d:%02d:%02d", cHours, cMinutes, cSeconds));
            totalTime.setText(String.format("%02d:%02d:%02d", dHours, dMinutes, dSeconds));
        }
    }

    private void playPauseVideo() {
        if (videoView.isPlaying()) {
            videoView.pause();
        } else {
            videoView.start();
            if (progressBar != null) {
                progressBar.postDelayed(onEverySecond, 1000);
            }
        }
    }

    public void switchMiniPlayer(boolean show) {
        if (show) {
            playerToolbar.setVisibility(View.GONE);
            miniPlayer.setVisibility(View.VISIBLE);
        } else {
            playerToolbar.setVisibility(View.VISIBLE);
            miniPlayer.setVisibility(View.GONE);
        }
    }

    public void playNewVideo(Video video) {

        this.currentVideo = video;

        miniPlayerText.setText(video.getName());
        playerTitle.setText(video.getName());

        ((MainActivity) getActivity()).expandPanel();

        videoView.stopPlayback();
        videoView.setVideoPath(video.getVideoFile());
        videoView.start();

        imagesAlreadyBlurred = false;
        miniPlayerDrawable = null;
        bgDrawable = null;

        resizeAndBlurBg();

    }

    private void resizeAndBlurBg() {
        new ComputeImage().execute(null);
    }

    public Toolbar getPlayerToolbar() {
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
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private class ComputeImage extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                thumb = BitmapUtils.getBitmapFromURL(currentVideo.getThumbFile());
                if (thumb == null) {
                    thumb = BitmapUtils.drawableToBitmap(getResources().getDrawable(R.drawable.placeholder));
                }
            } catch (Exception e) {
                SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
            }
        }

        @Override
        protected Object doInBackground(Object[] params) {

            if (!imagesAlreadyBlurred && thumb != null && miniPlayerDrawable == null || playerBg == null) {
                SmartLog.Log(SmartLog.LogLevel.DEBUG, "resizeAndBlurBg", "blurring bg image");
                Bitmap originalBitmap = thumb;
                Bitmap blurredPlayerBitmap = BitmapUtils.blur(getContext(), originalBitmap, 25);
                Bitmap resizedBitmap = BitmapUtils.resizeImageForImageView(blurredPlayerBitmap, 255);
                miniPlayerDrawable = new BitmapDrawable(getResources(), BitmapUtils.resizeImageForImageView(originalBitmap, 255));
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
                    miniPlayerImageView.setImageDrawable(miniPlayerDrawable);

                    YoYo.with(Techniques.FadeIn).duration(400).playOn(playerBg);
                    YoYo.with(Techniques.FadeIn).duration(400).playOn(miniPlayerImageView);
                }
            }, 400);

            imagesAlreadyBlurred = true;
        }
    }
}
