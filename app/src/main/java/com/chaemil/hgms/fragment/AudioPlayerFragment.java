package com.chaemil.hgms.fragment;

/**
 * Created by chaemil on 5.1.16.
 */

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.BitmapUtils;
import com.chaemil.hgms.utils.DimensUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.johnpersano.supertoasts.SuperToast;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import at.markushi.ui.CircleButton;

/**
 * Created by chaemil on 2.12.15.
 */
public class AudioPlayerFragment extends Fragment implements View.OnClickListener,
        MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener {

    public static final String TAG = "audio_player_fragment";
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
    private CircleButton playPause;
    private CircleButton rew;
    private CircleButton ff;
    private TextView currentTime;
    private TextView totalTime;
    private int duration;
    private int currentTimeInt;
    private AppCompatSeekBar seekBar;
    private Bitmap thumb;
    private Video currentAudio;
    private CircleButton miniPlayerPause;
    private ProgressBar bufferBar;
    private int bufferFail;
    private RelativeLayout videoWrapper;
    private ViewGroup rootView;
    private int currentOrientation;
    private MediaPlayer audioPlayer;
    private ImageView audioThumb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        audioPlayer = new MediaPlayer();
    }


    @Override
    public void onPause() {
        super.onPause();

        audioPlayer.pause();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (currentAudio != null) {
            audioPlayer.seekTo(currentAudio.getCurrentTime());
        }

        if (isAdded()) {
            adjustLayout();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.audio_player_fragment, container, false);

        if (savedInstanceState != null) {
            imagesAlreadyBlurred = savedInstanceState.getBoolean(IMAGES_ALREADY_BLURRED);

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
        if (bgDrawable != null) {
            outState.putParcelable(BG_DRAWABLE, BitmapUtils.drawableToBitmap(bgDrawable));
        }
        outState.putInt(CURRENT_TIME, audioPlayer.getCurrentPosition());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            audioPlayer.seekTo(savedInstanceState.getInt(CURRENT_TIME));
        }
    }

    private void getUI(ViewGroup rootView) {
        miniPlayer = (RelativeLayout) rootView.findViewById(R.id.mini_player);
        playerBg = (ImageView) rootView.findViewById(R.id.player_bg);
        miniPlayerImageView = (ImageView) rootView.findViewById(R.id.mini_player_image);
        miniPlayerText = (TextView) rootView.findViewById(R.id.mini_player_text);
        playerToolbar = (RelativeLayout) rootView.findViewById(R.id.toolbar);
        playerTitle = (TextView) rootView.findViewById(R.id.player_title);
        audioThumb = (ImageView) rootView.findViewById(R.id.audio_thumb);
        playPause = (CircleButton) rootView.findViewById(R.id.play_pause);
        rew = (CircleButton) rootView.findViewById(R.id.rew);
        ff = (CircleButton) rootView.findViewById(R.id.ff);
        currentTime = (TextView) rootView.findViewById(R.id.current_time);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        seekBar = (AppCompatSeekBar) rootView.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        miniPlayerPause = (CircleButton) rootView.findViewById(R.id.mini_play_pause);
        bufferBar = (ProgressBar) rootView.findViewById(R.id.buffer_bar);
        videoWrapper = (RelativeLayout) rootView.findViewById(R.id.thumb_wrapper);
    }

    private void setupUI() {
        playPause.setOnClickListener(this);
        rew.setOnClickListener(this);
        ff.setOnClickListener(this);
        miniPlayerPause.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.play_pause:
                playPauseAudio();
                break;
            case R.id.rew:
                audioPlayer.seekTo(audioPlayer.getCurrentPosition() - 10000);
                break;
            case R.id.ff:
                audioPlayer.seekTo(audioPlayer.getCurrentPosition() + 10000);
                break;
            case R.id.mini_play_pause:
                playPauseAudio();
                break;
        }
    }

    public void adjustLayout() {
        if (isAdded()) {
            currentOrientation = getResources().getConfiguration().orientation;

            if (((MainActivity) getActivity()).isPanelExpanded()) {

                playerToolbar.setVisibility(View.VISIBLE);
                playerBg.setVisibility(View.VISIBLE);
            } else {

                if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    videoWrapper.setVisibility(View.GONE);
                } else {
                    videoWrapper.setVisibility(View.VISIBLE);
                }

            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        duration = audioPlayer.getDuration();
        seekBar.setMax(duration);
        seekBar.postDelayed(onEverySecond, 1000);
        YoYo.with(Techniques.FadeIn).duration(350).delay(250).playOn(audioThumb);

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
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START)
                    bufferBar.setVisibility(View.VISIBLE);
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END)
                    bufferBar.setVisibility(View.GONE);
                return false;
            }
        });
    }

    private Runnable onEverySecond = new Runnable() {
        @Override
        public void run(){
            if (seekBar != null) {
                seekBar.setProgress(audioPlayer.getCurrentPosition());
            }

            if (audioPlayer.isPlaying()) {
                if (seekBar != null) {
                    seekBar.postDelayed(onEverySecond, 1000);
                }
                updateTime();
            }
        }
    };

    private void updateTime() {
        currentTimeInt = audioPlayer.getCurrentPosition();

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

    private void playPauseAudio() {
        if (audioPlayer.isPlaying()) {
            audioPlayer.pause();
            playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
            miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
        } else {
            audioPlayer.start();
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
        if (audioPlayer != null && currentAudio != null) {
            currentAudio.setCurrentTime(audioPlayer.getCurrentPosition());
            currentAudio.save();
        }
    }

    public void playNewAudio(final Video audio) {

        saveCurrentVideoTime();

        Video savedAudio = null;

        try {
            savedAudio = Video.findByServerId(audio.getServerId());
        } catch (Exception e) {
            SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
        }

        if (savedAudio != null) {
            this.currentAudio = savedAudio;

            SuperToast.create(getActivity(),
                    getString(R.string.resuming_from_saved_time),
                    SuperToast.Duration.SHORT).show();
        } else {
            this.currentAudio = audio;
        }

        Picasso.with(getActivity()).load(currentAudio.getThumbFile()).into(audioThumb);
        Picasso.with(getActivity()).load(currentAudio.getThumbFile()).centerCrop().resize(320, 320).into(miniPlayerImageView);

        playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

        miniPlayerText.setText(audio.getName());
        playerTitle.setText(audio.getName());

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

                try {
                    audioPlayer.setDataSource(currentAudio.getAudioFile());
                    audioPlayer.prepare();
                    audioPlayer.setOnPreparedListener(AudioPlayerFragment.this);
                    audioPlayer.start();
                    audioPlayer.seekTo(currentAudio.getCurrentTime());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 500);

    }

    private void resizeAndBlurBg() {
        new ComputeImage().execute(null);
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
            audioPlayer.seekTo(progress);
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
                thumb = BitmapUtils.getBitmapFromURL(currentAudio.getThumbFile());
                if (thumb == null) {
                    thumb = BitmapUtils.drawableToBitmap(getResources().getDrawable(R.drawable.placeholder));
                }
            } catch (Exception e) {
                SmartLog.Log(SmartLog.LogLevel.ERROR, "exception", e.toString());
            }
        }

        @Override
        protected Object doInBackground(Object[] params) {

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

