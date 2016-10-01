package com.chaemil.hgms.adapter.homepage_sections;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.holder.VideoViewHolder;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.DimensUtils;
import com.chaemil.hgms.utils.StringUtils;
import com.chaemil.hgms.view.VideoThumbImageView;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by chaemil on 27.8.16.
 */
public class SectionContinueWatching extends BaseSection {

    private final Context context;
    private final MainActivity mainActivity;
    ArrayList<Video> videosToWatch = new ArrayList<>();

    public SectionContinueWatching(Context context, MainActivity mainActivity, ArrayList<Video> videosToWatch) {
        super(R.layout.homepage_section_header,
                R.layout.homepage_section_footer,
                AdapterUtils.getArchiveLayout(context));
        this.context = context;
        this.mainActivity = mainActivity;
        this.videosToWatch = videosToWatch;
    }

    @Override
    public int getContentItemsTotal() {
        return videosToWatch.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
        final Video video = videosToWatch.get(position);

        videoViewHolder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.playNewVideo(video);
            }
        });
        videoViewHolder.name.setText(video.getName());
        videoViewHolder.date.setText(StringUtils.formatDate(video.getDate(), context));
        videoViewHolder.views.setText(video.getViews() + " " + context.getString(R.string.views));
        videoViewHolder.viewProgress.setMax(video.getDuration());
        videoViewHolder.viewProgress.setProgress(video.getCurrentTime());
        videoViewHolder.time.setText(StringUtils.getDurationString(video.getDuration()));
        if (video.isAudioDownloaded(context)) {
            videoViewHolder.downloaded.setVisibility(View.VISIBLE);
            videoViewHolder.downloaded.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainActivity.playNewAudio(video, true);
                }
            });
        } else {
            videoViewHolder.downloaded.setVisibility(View.GONE);
        }

        int thumbWidth = videoViewHolder.thumb.getWidth();

        Ion.with(context)
                .load(video.getThumbFile())
                .withBitmap()
                .resize(thumbWidth, (int) (thumbWidth * 0.5625))
                .intoImageView(videoViewHolder.thumb);

        setupTime(videoViewHolder, video);

        videoViewHolder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdapterUtils.contextDialog(context, mainActivity, video);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        headerHolder.sectionName.setText(context.getString(R.string.continue_watching));
        headerHolder.sectionIcon.setImageDrawable(context.getResources()
                .getDrawable(R.drawable.continue_watching));
    }
}
