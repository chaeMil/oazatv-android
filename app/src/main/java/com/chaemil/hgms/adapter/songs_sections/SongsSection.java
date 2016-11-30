package com.chaemil.hgms.adapter.songs_sections;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.holder.SongViewHolder;
import com.chaemil.hgms.adapter.homepage_sections.HeaderViewHolder;
import com.chaemil.hgms.fragment.CategoryFragment;
import com.chaemil.hgms.fragment.SongFragment;
import com.chaemil.hgms.fragment.SongsFragment;
import com.chaemil.hgms.model.Song;
import com.chaemil.hgms.model.SongGroup;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by chaemil on 1.11.16.
 */

public class SongsSection extends StatelessSection {

    private final Context context;
    private final SongsFragment songsFragment;
    private final MainActivity mainActivity;
    private SongGroup songGroup;

    public SongsSection(Context context, MainActivity mainActivity,
                        SongsFragment songsFragment, SongGroup songGroup) {
        super(R.layout.songs_section_header,
                R.layout.song_name);
        this.mainActivity = mainActivity;
        this.songsFragment = songsFragment;
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

        final Song song = songGroup.getSongs().get(position);

        songViewHolder.name.setText(song.getName());
        songViewHolder.author.setText(song.getAuthor());
        songViewHolder.tag.setText(song.getTag());
        songViewHolder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SongFragment songFragment = new SongFragment();
                Bundle args = new Bundle();
                args.putInt(SongFragment.SONG_ID, song.getId());
                songFragment.setArguments(args);

                songsFragment.setSongFragment(songFragment);

                FragmentTransaction transaction = mainActivity.getFragmentManager().beginTransaction();
                transaction.replace(R.id.song_fragment, songFragment);
                transaction.addToBackStack(SongFragment.TAG);
                transaction.commit();
                mainActivity.songVisible = true;
            }
        });

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
