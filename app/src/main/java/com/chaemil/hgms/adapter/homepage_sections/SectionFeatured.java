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
public class SectionFeatured extends StatelessSection {

    private final Context context;
    private final MainActivity mainActivity;
    private final int displayWidth;
    ArrayList<ArchiveItem> archive = new ArrayList<>();

    public SectionFeatured(Context context, MainActivity mainActivity, ArrayList<ArchiveItem> archive) {
        super(R.layout.homepage_section_header, R.layout.homepage_section_footer, R.layout.featured_item);
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
        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        headerHolder.sectionName.setText(context.getString(R.string.featured));
        headerHolder.sectionIcon.setImageDrawable(context.getResources()
                .getDrawable(R.drawable.featured));
    }

    private void setupTime(final VideoViewHolder holder, final Video video) {
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
                holder.time.setText(StringUtils.getDurationString(video.getDuration()));
            }
        }.execute();
    }

    private void openAlbum(PhotoAlbum album) {
        mainActivity.getMainFragment().openAlbum(album);
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder{

        private RelativeLayout mainView;
        public VideoThumbImageView thumb;
        public TextView name;
        public TextView date;
        public TextView views;
        public ImageButton more;
        public ProgressBar viewProgress;
        public TextView time;

        public VideoViewHolder(View itemView) {
            super(itemView);
            this.mainView = (RelativeLayout) itemView.findViewById(R.id.main_view);
            this.thumb = (VideoThumbImageView) itemView.findViewById(R.id.thumb);
            this.name = (TextView) itemView.findViewById(R.id.name);
            this.date = (TextView) itemView.findViewById(R.id.date);
            this.views = (TextView) itemView.findViewById(R.id.views);
            this.more = (ImageButton) itemView.findViewById(R.id.context_menu);
            this.viewProgress = (ProgressBar) itemView.findViewById(R.id.view_progress);
            this.time = (TextView) itemView.findViewById(R.id.video_time);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder{

        public final TextView sectionName;
        public final ImageView sectionIcon;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            this.sectionName = (TextView) itemView.findViewById(R.id.section_name);
            this.sectionIcon = (ImageView) itemView.findViewById(R.id.section_icon);
        }
    }
}