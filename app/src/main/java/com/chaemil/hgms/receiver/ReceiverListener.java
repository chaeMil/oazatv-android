package com.chaemil.hgms.receiver;

/**
 * Created by chaemil on 20.6.16.
 */
public interface ReceiverListener {
    void playPauseAudio();

    void seekFF();

    void seekREW();

    void stop();
}
