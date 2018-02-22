package com.chaemil.hgms.ui.tv.presenter;

import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.ui.tv.view.VideoCardView;

/**
 * Created by Michal Mlejnek on 22/02/2018.
 */

public class VideoPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(new VideoCardView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((VideoCardView) viewHolder.view).bind((Video) item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
