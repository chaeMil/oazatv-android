package com.chaemil.hgms.viewmodel.model;

import android.databinding.BaseObservable;
import android.databinding.BindingConversion;

import java.util.Objects;

/**
 * Created by Michal Mlejnek on 20/09/2017.
 */

public class BindableString extends BaseObservable {
    private String value;

    public String get() {
        return value != null ? value : "";
    }

    public void set(String value) {
        this.value = value;
        notifyChange();
    }

    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }

    @BindingConversion
    public static String convertBindableToString(
            BindableString bindableString) {
        return bindableString.get();
    }
}