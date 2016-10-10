package com.chaemil.hgms.adapter.homepage_sections;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.holder.VideoViewHolder;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.PhotoAlbum;
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
public class SectionFeatured extends BaseSection {

    private final Context context;
    private final MainActivity mainActivity;
    private final int displayWidth;
    ArrayList<ArchiveItem> archive = new ArrayList<>();

    public SectionFeatured(Context context, MainActivity mainActivity, ArrayList<ArchiveItem> archive) {
        super(R.layout.homepage_section_header,
                R.layout.homepage_section_footer,
                R.layout.featured_item);
        this.context = context;
        this.mainActivity = mainActivity;
        this.archive = archive;
        this.displayWidth = DimensUtils.getDisplayHeight(mainActivity);
    }

    @Override
    public int getContentItemsTotal() {
        return archive.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoViewHolder videoViewHolder = (VideoViewHolder) holder;

        ArchiveItem archiveItem = archive.get(position);

        switch (archiveItem.getType()) {
            case ArchiveItem.Type.VIDEO:

                final Video video = archiveItem.getVideo();

                videoViewHolder.mainView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivity.playNewVideo(video);
                    }
                });
                videoViewHolder.name.setText(video.getName());
                videoViewHolder.date.setText(StringUtils.formatDate(video.getDate(), context));
                videoViewHolder.views.setText(video.getViews() + " " + context.getString(R.string.views));
                videoViewHolder.more.setVisibility(View.VISIBLE);
                videoViewHolder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AdapterUtils.contextDialog(context, mainActivity, video);
                    }
                });
                videoViewHolder.thumb.setBackgroundColor(Color.parseColor(video.getThumbColor()));
                videoViewHolder.time.setText(StringUtils.getDurationString(video.getDuration()));
                if (video.isAudioDownloaded(context)) {
                    videoViewHolder.downloaded.setVisibility(View.VISIBLE);
                    videoViewHolder.downloaded.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mainActivity.playNewAudio(video);
                        }
                    });
                } else {
                    videoViewHolder.downloaded.setVisibility(View.GONE);
                }

                setupTime(videoViewHolder, video);

                Ion.with(context)
                        .load(video.getThumbFile())
                        .withBitmap()
                        .resize(displayWidth, (int) (displayWidth * 0.5625))
                        .intoImageView(videoViewHolder.thumb);

                break;

            case ArchiveItem.Type.ALBUM:

                final PhotoAlbum photoAlbum = archiveItem.getAlbum();

                videoViewHolder.mainView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openAlbum(photoAlbum);
                    }
                });
                videoViewHolder.name.setText(photoAlbum.getName());
                videoViewHolder.date.setText(StringUtils.formatDate(photoAlbum.getDate(), context));
                videoViewHolder.views.setText(context.getString(R.string.photo_album));
                videoViewHolder.more.setVisibility(View.GONE);
                videoViewHolder.viewProgress.setVisibility(View.GONE);
                videoViewHolder.time.setVisibility(View.GONE);

                Ion.with(context)
                        .load(photoAlbum.getThumbs().getThumb1024())
                        .withBitmap()
                        .resize(displayWidth, (int) (displayWidth * 0.5625))
                        .intoImageView(videoViewHolder.thumb);
                break;
        }
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        super.onBindHeaderViewHolder(holder);

        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        headerHolder.sectionName.setText(context.getString(R.string.featured));
        headerHolder.sectionIcon.setImageDrawable(context.getResources()
                .getDrawable(R.drawable.featured));
    }
}
