package com.chaemil.hgms.viewmodel.model;

import android.databinding.BaseObservable;

/**
 * Created by Michal Mlejnek on 06/11/2017.
 */

public class BindableInteger extends BaseObservable {
    int mValue;

    public int get() {
        return mValue;
    }

    public void set(int value) {
        if (mValue != value) {
            this.mValue = value;
            notifyChange();
        }
    }
}
