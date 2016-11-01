package com.chaemil.hgms.adapter.songs_sections;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chaemil.hgms.R;
import com.chaemil.hgms.adapter.holder.SongViewHolder;
import com.chaemil.hgms.adapter.homepage_sections.HeaderViewHolder;
import com.chaemil.hgms.model.Song;
import com.chaemil.hgms.model.SongGroup;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by chaemil on 1.11.16.
 */

public class SongsSection extends StatelessSection {

    private final Context context;
    private SongGroup songGroup;

    public SongsSection(Context context, SongGroup songGroup) {
        super(R.layout.songs_section_header,
                R.layout.song_name);
        this.context = context;
        this.songGroup = songGroup;
    }

    @Override
    public int getContentItemsTotal() {
        return songGroup.getSongs().size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new SongViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        SongViewHolder songViewHolder = (SongViewHolder) holder;

        Song song = songGroup.getSongs().get(position);

        songViewHolder.name.setText(song.getName());
        songViewHolder.author.setText(song.getAuthor());
        songViewHolder.tag.setText(song.getTag());
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        super.onBindHeaderViewHolder(holder);

        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        headerHolder.sectionName.setText(songGroup.getTag());
    }
}
