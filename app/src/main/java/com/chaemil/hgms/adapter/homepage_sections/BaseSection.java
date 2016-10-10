package com.chaemil.hgms.adapter.homepage_sections;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.holder.VideoViewHolder;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.Video;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by chaemil on 26.9.16.
 */

public abstract class BaseSection extends StatelessSection {

    private final Context context = null;
    private final MainActivity mainActivity = null;

    public BaseSection(int headerResourceId, int footerResourceId, int itemResourceId) {
        super(headerResourceId, footerResourceId, itemResourceId);
    }

    public void setupTime(final VideoViewHolder holder, final Video video) {
        holder.viewProgress.setVisibility(View.GONE);
        holder.time.setVisibility(View.GONE);

        new AsyncTask<Void, Void, Void>() {

            public Video savedVideo;

            @Override
            protected Void doInBackground( Void... voids ) {
                savedVideo = Video.findByServerId(video.getServerId());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                holder.viewProgress.setVisibility(View.VISIBLE);
                if (savedVideo != null && savedVideo.equals(video)) {
                    holder.viewProgress.setMax(video.getDuration());
                    holder.viewProgress.setProgress(savedVideo.getCurrentTime() / 1000);
                } else {
                    holder.viewProgress.setMax(100);
                    holder.viewProgress.setProgress(0);
                }

                holder.time.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    public void openAlbum(PhotoAlbum album) {
        mainActivity.getMainFragment().openAlbum(album);
    }

    public void setFullSpan(RecyclerView.ViewHolder holder) {
        StaggeredGridLayoutManager.LayoutParams layoutParams =
                (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        layoutParams.setFullSpan(true);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        super.onBindHeaderViewHolder(holder);
        setFullSpan(holder);
    }

    @Override
    public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
        super.onBindFooterViewHolder(holder);
        setFullSpan(holder);
    }
}
