package com.chaemil.hgms.viewmodel.model;

import android.databinding.BaseObservable;

/**
 * Created by Michal Mlejnek on 21/09/2017.
 */

public class BindableBoolean extends BaseObservable {
    boolean mValue;

    public BindableBoolean() {
    }

    public BindableBoolean(boolean mValue) {
        this.mValue = mValue;
    }

    public boolean get() {
        return mValue;
    }

    public BindableBoolean set(boolean value) {
        if (mValue != value) {
            this.mValue = value;
            notifyChange();
        }
        return this;
    }

    public void toggle() {
        set(!mValue);
    }
}