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
 * Created by chaemil on 1.11.16.
 */
public class SongViewHolder extends RecyclerView.ViewHolder {

    public RelativeLayout mainView;
    public TextView tag;
    public TextView name;
    public TextView author;

    public SongViewHolder(View itemView) {
        super(itemView);
        this.mainView = (RelativeLayout) itemView.findViewById(R.id.main_view);
        this.tag = (TextView) itemView.findViewById(R.id.tag);
        this.name = (TextView) itemView.findViewById(R.id.name);
        this.author = (TextView) itemView.findViewById(R.id.author);
    }
}
