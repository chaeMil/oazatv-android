package com.chaemil.hgms.receiver;

/**
 * Created by chaemil on 20.6.16.
 */
public interface PlaybackReceiverListener {
    void playbackPlayPauseAudio();

    void playbackSeekFF();

    void playbackSeekREW();

    void playbackStop();
}
