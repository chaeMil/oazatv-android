package com.chaemil.hgms.ui.tv.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v17.leanback.app.PlaybackFragment;
import android.support.v17.leanback.app.PlaybackFragmentGlueHost;
import android.support.v17.leanback.app.VideoFragment;
import android.support.v17.leanback.app.VideoFragmentGlueHost;
import android.support.v17.leanback.media.MediaPlayerAdapter;
import android.support.v17.leanback.media.PlaybackGlue;
import android.support.v17.leanback.media.PlaybackTransportControlGlue;

import com.chaemil.hgms.model.Video;

/**
 * Created by Michal Mlejnek on 22/02/2018.
 */

public class VideoPlaybackFragment extends VideoFragment {
    private static final String TAG = VideoPlaybackFragment.class.getSimpleName();

    private Bundle arguments;
    private Video video;

    public static VideoPlaybackFragment newInstance() {
        Bundle args = new Bundle();
        VideoPlaybackFragment fragment = new VideoPlaybackFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arguments = getArguments();
        video = arguments.getParcelable("video");

        if (video != null) {
            setupPlayer();
        }
    }

    private void setupPlayer() {
        PlaybackTransportControlGlue<MediaPlayerAdapter> playerGlue =
                new PlaybackTransportControlGlue<>(getActivity(),
                        new MediaPlayerAdapter(getActivity()));
        playerGlue.setHost(new VideoFragmentGlueHost(this));
        playerGlue.addPlayerCallback(new PlaybackGlue.PlayerCallback() {
            @Override
            public void onPreparedStateChanged(PlaybackGlue glue) {
                if (glue.isPrepared()) {
                    playerGlue.play();
                }
            }

            @Override
            public void onPlayStateChanged(PlaybackGlue glue) {
                if (glue.isPlaying()) {
                    playerGlue.play();
                } else {
                    playerGlue.pause();
                }
            }

            @Override
            public void onPlayCompleted(PlaybackGlue glue) {
                super.onPlayCompleted(glue);
            }
        });
        playerGlue.setTitle(video.getName());
        playerGlue.setSubtitle(video.getDescription());
        String uri = video.getVideoFileWebm();
        playerGlue.getPlayerAdapter().setDataSource(Uri.parse(uri));
    }
}
