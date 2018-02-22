package com.chaemil.hgms.ui.tv.presenter;

import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import com.chaemil.hgms.ui.tv.view.HomeCardView;

/**
 * Created by Michal Mlejnek on 22/02/2018.
 */

public class VideoPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(new HomeCardView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((HomeCardView) viewHolder.view).bind(item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
