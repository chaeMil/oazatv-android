package com.chaemil.hgms.model;

import android.view.KeyEvent;
import com.chaemil.hgms.activity.MainActivity;

/**
 * Created by chaemil on 2.10.16.
 */
public class KeyboardHandler {
    public static final int SCREEN_MAIN_FRAGMENT = 1;
    private MainActivity activity;
    private int currentScreen = 0;

    public KeyboardHandler(MainActivity activity) {
        this.activity = activity;
    }

    public void event(int keyCode, KeyEvent event) {

        switch (currentScreen) {
            case SCREEN_MAIN_FRAGMENT:
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        activity.nextTab();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        activity.prevTab();
                }
                break;
        }

    }

    public int getCurrentScreen() {
        return currentScreen;
    }

    public void setCurrentScreen(int currentScreen) {
        this.currentScreen = currentScreen;
    }
}
