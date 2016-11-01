package com.chaemil.hgms.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;

/**
 * Created by chaemil on 1.11.16.
 */
public class SongTagViewHolder extends RecyclerView.ViewHolder {

    public RelativeLayout mainView;
    public TextView tag;

    public SongTagViewHolder(View itemView) {
        super(itemView);
        this.mainView = (RelativeLayout) itemView.findViewById(R.id.main_view);
        this.tag = (TextView) itemView.findViewById(R.id.tag);
    }
}
