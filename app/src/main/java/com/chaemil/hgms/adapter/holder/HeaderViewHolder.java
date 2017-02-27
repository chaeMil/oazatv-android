package com.chaemil.hgms.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaemil.hgms.R;

/**
 * Created by chaemil on 5.9.16.
 */
public class HeaderViewHolder extends RecyclerView.ViewHolder{

    public final TextView sectionName;
    public final ImageView sectionIcon;

    public HeaderViewHolder(View itemView) {
        super(itemView);
        this.sectionName = (TextView) itemView.findViewById(R.id.section_name);
        this.sectionIcon = (ImageView) itemView.findViewById(R.id.section_icon);
    }
}