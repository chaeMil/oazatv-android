package com.chaemil.hgms.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.view.VideoThumbImageView;

/**
 * Created by chaemil on 26.9.16.
 */

public class VideoViewHolder extends RecyclerView.ViewHolder{

    public RelativeLayout mainView;
    public ImageButton downloaded;
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
        this.downloaded = (ImageButton) itemView.findViewById(R.id.downloaded);
    }
}
