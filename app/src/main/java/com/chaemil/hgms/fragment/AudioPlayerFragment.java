package com.chaemil.hgms.fragment;

/**
 * Created by chaemil on 5.1.16.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.factory.RequestFactoryListener;
import com.chaemil.hgms.model.RequestType;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.receiver.AudioPlaybackReceiver;
import com.chaemil.hgms.service.AnalyticsService;
import com.chaemil.hgms.service.AudioPlaybackService;
import com.chaemil.hgms.utils.ShareUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import at.markushi.ui.CircleButton;
import ru.rambler.libs.swipe_layout.SwipeLayout;

/**
 * Created by chaemil on 2.12.15.
 */
public class AudioPlayerFragment extends BaseFragment implements View.OnClickListener,
         SeekBar.OnSeekBarChangeListener, RequestFactoryListener {

    public static final String TAG = "audio_player_fragment";
    private RelativeLayout miniPlayer;
    private ImageView miniPlayerImageView;
    private RelativeLayout playerToolbar;
    private TextView miniPlayerText;
    private TextView playerTitle;
    private CircleButton playPause;
    private CircleButton rew;
    private TextView currentTime;
    private TextView totalTime;
    private int currentTimeInt;
    private AppCompatSeekBar seekBar;
    private CircleButton miniPlayerPause;
    private ProgressBar bufferBar;
    private int bufferFail;
    private ViewGroup rootView;
    private ImageView audioThumb;
    private MainActivity mainActivity;
    private ImageView back;
    private ImageView share;
    private TextView description;
    private TextView tags;
    private TimerTask timerTask;
    private Timer timer;
    private Intent playIntent;
    private boolean isReconnecting = false;
    private int audioDuration;
    private AudioPlaybackReceiver audioPlaybackReceiver;
    private SwipeLayout miniPlayerSwipe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshPlayButtons();

        if (mainActivity.isPanelExpanded()) {
            switchMiniPlayer(1);
        } else {
            switchMiniPlayer(0);
        }

        activateUI(true);

        /*int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            SuperToast.create(getActivity(),
                    getString(R.string.audio_focus_not_granted),
                    SuperToast.Duration.SHORT).show();
        }

        AnalyticsService.getInstance().setPage(AnalyticsService.Pages.AUDIOPLAYER_FRAGMENT);
        setupTimer();*/
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //saveCurrentAudioTime();
    }

    public void init(boolean isReconnecting) {
        this.isReconnecting = isReconnecting;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.audio_player_fragment, container, false);

        getUI(rootView);
        activateUI(false);
        setupUI();

        if (isReconnecting) {
            reconnectToService(mainActivity);
        }

        refreshPlayButtons();

        return rootView;
    }

    private void setupTimer() {
        stopTimer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        postGA();
                    }
                });
            }
        };
        resetTimer();
    }

    private void resetTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 5000, 5000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void postGA() {
        /*if (audioPlayer != null && audioPlayer.isPlaying()) {
            GAUtils.sendGAScreen(
                    ((OazaApp) getActivity().getApplication()),
                    "AudioPlayer",
                    currentAudio.getNameCS());
        }*/
    }

    private void activateUI(boolean state) {
        playPause.setEnabled(state);
        rew.setEnabled(state);
        seekBar.setEnabled(state);
    }

    private void getUI(ViewGroup rootView) {
        miniPlayer = (RelativeLayout) rootView.findViewById(R.id.mini_player);
        miniPlayerImageView = (ImageView) rootView.findViewById(R.id.mini_player_image);
        miniPlayerText = (TextView) rootView.findViewById(R.id.mini_player_text);
        playerToolbar = (RelativeLayout) rootView.findViewById(R.id.toolbar);
        playerTitle = (TextView) rootView.findViewById(R.id.player_title);
        audioThumb = (ImageView) rootView.findViewById(R.id.audio_thumb);
        playPause = (CircleButton) rootView.findViewById(R.id.play_pause);
        rew = (CircleButton) rootView.findViewById(R.id.rew);
        currentTime = (TextView) rootView.findViewById(R.id.current_time);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        seekBar = (AppCompatSeekBar) rootView.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        miniPlayerPause = (CircleButton) rootView.findViewById(R.id.mini_play_pause);
        bufferBar = (ProgressBar) rootView.findViewById(R.id.buffer_bar);
        back = (ImageView) rootView.findViewById(R.id.back);
        share = (ImageView) rootView.findViewById(R.id.share);
        description = (TextView) rootView.findViewById(R.id.description);
        tags = (TextView) rootView.findViewById(R.id.tags);
        miniPlayerSwipe = (SwipeLayout) rootView.findViewById(R.id.mini_player_swipe);
    }

    private void setupUI() {
        back.setOnClickListener(this);
        playPause.setOnClickListener(this);
        rew.setOnClickListener(this);
        miniPlayerPause.setOnClickListener(this);
        share.setOnClickListener(this);
        miniPlayer.setOnClickListener(this);
        playerToolbar.setOnClickListener(this);

        seekBar.setMax(getAudioDuration());
        seekBar.postDelayed(onEverySecond, 1000);

        miniPlayerSwipe.setOnSwipeListener(createSwipeListener());


        refreshPlayButtons();
    }

    private SwipeLayout.OnSwipeListener createSwipeListener() {
        return new SwipeLayout.OnSwipeListener() {
            @Override
            public void onBeginSwipe(SwipeLayout swipeLayout, boolean moveToRight) {

            }

            @Override
            public void onSwipeClampReached(SwipeLayout swipeLayout, boolean moveToRight) {
                Intent delete = new Intent(AudioPlaybackReceiver.NOTIFY_DELETE);
                getActivity().sendBroadcast(delete);
            }

            @Override
            public void onLeftStickyEdge(SwipeLayout swipeLayout, boolean moveToRight) {

            }

            @Override
            public void onRightStickyEdge(SwipeLayout swipeLayout, boolean moveToRight) {

            }
        };
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.play_pause:
                if (mainActivity != null) {
                    mainActivity.sendBroadcast(new Intent(AudioPlaybackReceiver.NOTIFY_PLAY_PAUSE));
                    refreshPlayButtons();
                }
                break;
            case R.id.rew:
                if (mainActivity != null) {
                    mainActivity.sendBroadcast(new Intent(AudioPlaybackReceiver.NOTIFY_REW));
                    updateTime();
                }
                break;
            case R.id.mini_play_pause:
                if (mainActivity != null) {
                    mainActivity.sendBroadcast(new Intent(AudioPlaybackReceiver.NOTIFY_PLAY_PAUSE));
                    refreshPlayButtons();
                }
                break;
            case R.id.back:
                if (mainActivity != null) {
                    mainActivity.collapsePanel();
                }
                break;
            case R.id.share:
                ShareUtils.shareAudioLink(getActivity(), getCurrentAudio());
                break;
            case R.id.mini_player:
            case R.id.toolbar:
                if (mainActivity != null && mainActivity.getPanelLayout() != null) {
                    switch(mainActivity.getPanelLayout().getPanelState()) {
                        case EXPANDED:
                            mainActivity.collapsePanel();
                            break;
                        case COLLAPSED:
                            mainActivity.expandPanel();
                            break;
                    }
                }
                break;
        }
    }

    private void refreshPlayButtons() {
        if (isAdded()) {
            if (isServicePlayingAudio()) {
                playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
                miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            } else {
                playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
                miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
            }
        }
    }

    private boolean isServicePlayingAudio() {
        AudioPlaybackService service = getService();
        if (service != null) {
            return service.getAudioPlayer().isPlaying();
        } else {
            return false;
        }
    }

    private int getAudioDuration() {
        AudioPlaybackService service = getService();
        if (service != null && service.getAudioPlayer() != null) {
            return service.getAudioPlayer().getDuration();
        }
        return 0;
    }

    private Runnable onEverySecond = new Runnable() {
        @Override
        public void run(){
            try {
                if (seekBar != null) {
                    updateTime();
                    refreshPlayButtons();
                    if (isAdded()) {
                        seekBar.postDelayed(onEverySecond, 1000);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void updateTime() {
        AudioPlaybackService service = getService();

        if (service != null && service.getAudioPlayer() != null) {

            try {
                currentTimeInt = service.getAudioPlayer().getCurrentPosition();
                audioDuration = getAudioDuration();
                seekBar.setMax(audioDuration);
                seekBar.setProgress(currentTimeInt);

                int dSeconds = (audioDuration / 1000) % 60;
                int dMinutes = ((audioDuration / (1000 * 60)) % 60);
                int dHours = ((audioDuration / (1000 * 60 * 60)) % 24);

                int cSeconds = (currentTimeInt / 1000) % 60;
                int cMinutes = ((currentTimeInt / (1000 * 60)) % 60);
                int cHours = ((currentTimeInt / (1000 * 60 * 60)) % 24);

                if (dHours == 0) {
                    currentTime.setText(String.format("%02d:%02d", cMinutes, cSeconds));
                    totalTime.setText(String.format("%02d:%02d", dMinutes, dSeconds));
                } else {
                    currentTime.setText(String.format("%02d:%02d:%02d", cHours, cMinutes, cSeconds));
                    totalTime.setText(String.format("%02d:%02d:%02d", dHours, dMinutes, dSeconds));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void switchMiniPlayer(float alpha) {
        if (isAdded()) {

            if (alpha == 0) {
                playerToolbar.setVisibility(View.GONE);
                miniPlayerSwipe.setVisibility(View.VISIBLE);
                miniPlayer.setVisibility(View.VISIBLE);
            } else if (alpha == 1) {
                playerToolbar.setVisibility(View.VISIBLE);
                miniPlayerSwipe.setVisibility(View.GONE);
                miniPlayer.setVisibility(View.GONE);
            } else {
                miniPlayerSwipe.setVisibility(View.VISIBLE);
                playerToolbar.setVisibility(View.VISIBLE);
                miniPlayer.setVisibility(View.VISIBLE);

                playerToolbar.setAlpha(alpha);
                miniPlayer.setAlpha(1 - alpha);
            }
        }
    }

    public Video getCurrentAudio() {
        return AudioPlaybackService.getInstance().getCurrentAudio();
    }

    public void reconnectToService(Context context) {
        Video audio = getService().getCurrentAudio();
        boolean downloaded = getService().getIsPlayingDownloaded();

        Picasso.with(context)
                .load(getCurrentAudio().getThumbFile())
                .into(audioThumb);

        Picasso.with(context)
                .load(getCurrentAudio().getThumbFile())
                .into(miniPlayerImageView);

        playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        miniPlayerPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

        String downloadedString = "";
        if (downloaded) {
            downloadedString = "[" + getString(R.string.downloaded) + "] ";
        }

        miniPlayerText.setText(downloadedString + audio.getName());
        playerTitle.setText(downloadedString + audio.getName());
        if (!getCurrentAudio().getDescription().equals("")) {
            description.setText(getCurrentAudio().getDescription());
        } else {
            description.setVisibility(View.GONE);
        }
        if (!getCurrentAudio().getTags().equals("")) {
            String tagsString = "";
            for (String tag : getCurrentAudio().getTags().split(",")) {
                tagsString += "#" + tag + " ";
            }
            tags.setText(tagsString);
        } else {
            tags.setVisibility(View.GONE);
        }

        currentTime.setText("00:00:00");
        totalTime.setText("???");
    }

    public void playNewAudio(Context context) {
        reconnectToService(context);
        activateUI(true);
        AnalyticsService.getInstance()
                .setPage(AnalyticsService.Pages.AUDIOPLAYER_FRAGMENT + "audioHash: "
                        + getCurrentAudio().getHash());
        postGA();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            AudioPlaybackService service = getService();
            if (service != null) {
                service.getAudioPlayer().seekTo(progress);
                updateTime();
                service.saveCurrentAudioTime();
            }
        }
    }

    private AudioPlaybackService getService() {
        return ((OazaApp) mainActivity.getApplication()).playbackService;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onSuccessResponse(JSONObject response, RequestType requestType) {

    }

    @Override
    public void onErrorResponse(VolleyError exception, RequestType requestType) {

    }

    public void playPause() {
        refreshPlayButtons();
    }

    public void bufferingStart() {
        bufferBar.setVisibility(View.VISIBLE);
    }

    public void bufferingEnd() {
        bufferBar.setVisibility(View.GONE);
    }
}

