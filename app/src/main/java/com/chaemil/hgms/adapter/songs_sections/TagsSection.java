package com.chaemil.hgms.adapter.songs_sections;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chaemil.hgms.R;
import com.chaemil.hgms.adapter.holder.SongTagViewHolder;
import com.chaemil.hgms.model.SongGroup;

import java.util.ArrayList;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by chaemil on 1.11.16.
 */

public class TagsSection extends StatelessSection {

    private final Context context;
    private ArrayList<SongGroup> songGroups;

    public TagsSection(Context context, ArrayList<SongGroup> songGroups) {
        super(R.layout.tag_name);
        this.context = context;
        this.songGroups = songGroups;
    }

    @Override
    public int getContentItemsTotal() {
        return songGroups.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new SongTagViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        SongTagViewHolder songTagViewHolder = (SongTagViewHolder) holder;

        String tag = songGroups.get(position).getTag();

        songTagViewHolder.tag.setText(tag);
    }
}
