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
import com.chaemil.hgms.model.Photo;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.StringUtils;
import com.chaemil.hgms.view.VideoThumbImageView;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by chaemil on 27.8.16.
 */
public class SectionNewAlbums extends StatelessSection {

    private final Context context;
    private final MainActivity mainActivity;
    ArrayList<PhotoAlbum> archive = new ArrayList<>();

    public SectionNewAlbums(Context context, MainActivity mainActivity, ArrayList<PhotoAlbum> archive) {
        super(R.layout.homepage_section_header, R.layout.homepage_section_footer, R.layout.archive_item);
        this.context = context;
        this.mainActivity = mainActivity;
        this.archive = archive;
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

        final PhotoAlbum photoalbum = archive.get(position);

        videoViewHolder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.getMainFragment().openAlbum(photoalbum);
            }
        });
        videoViewHolder.name.setText(photoalbum.getName());
        videoViewHolder.date.setText(StringUtils.formatDate(photoalbum.getDate(), context));
        videoViewHolder.views.setVisibility(View.GONE);
        videoViewHolder.more.setVisibility(View.VISIBLE);
        videoViewHolder.more.setVisibility(View.GONE);

        int thumbWidth = videoViewHolder.thumb.getWidth();

        Ion.with(context)
                .load(photoalbum.getThumbs().getThumb1024())
                .withBitmap()
                .resize(thumbWidth, (int) (thumbWidth * 0.5625))
                .intoImageView(videoViewHolder.thumb);


    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        headerHolder.sectionName.setText(context.getString(R.string.newest_videos));
        headerHolder.sectionIcon.setImageDrawable(context.getResources()
                .getDrawable(R.drawable.newest_videos));
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
