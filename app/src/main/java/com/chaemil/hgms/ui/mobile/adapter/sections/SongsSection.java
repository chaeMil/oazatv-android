package com.chaemil.hgms.ui.mobile.adapter.sections;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.ui.mobile.activity.MainActivity;
import com.chaemil.hgms.ui.mobile.fragment.SongFragment;
import com.chaemil.hgms.ui.mobile.fragment.SongsFragment;
import com.chaemil.hgms.model.Song;
import com.chaemil.hgms.model.SongGroup;

/**
 * Created by chaemil on 27.2.17.
 */

public class SongsSection extends BaseSection {


    private final SongsFragment songsFragment;
    private final Context context;
    private SongGroup songGroup;

    public SongsSection(SongsFragment songsFragment, Context context, SongGroup songGroup) {
        super(R.layout.songs_section_header,  R.layout.empty, R.layout.song_name);
        this.songGroup = songGroup;
        this.songsFragment = songsFragment;
        this.context = context;
    }

    @Override
    public int getContentItemsTotal() {
        return songGroup.getSongs().size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new SongHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        ((SongHeaderHolder) holder).sectionName.setText(songGroup.getTag());
        setFullSpan(holder);
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new SongHeaderHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Song song = songGroup.getSongs().get(position);

        ((SongHolder) holder).author.setText(song.getAuthor());
        ((SongHolder) holder).name.setText(song.getName());
        ((SongHolder) holder).tag.setText(song.getTag());
        ((SongHolder) holder).mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSong(song);
            }
        });
    }

    private class SongHolder extends RecyclerView.ViewHolder {
        RelativeLayout mainView;
        TextView tag;
        TextView name;
        TextView author;

        public SongHolder(View itemView) {
            super(itemView);
            this.mainView = (RelativeLayout) itemView.findViewById(R.id.main_view);
            this.tag = (TextView) itemView.findViewById(R.id.tag);
            this.name = (TextView) itemView.findViewById(R.id.name);
            this.author = (TextView) itemView.findViewById(R.id.author);
        }
    }

    private class SongHeaderHolder extends RecyclerView.ViewHolder {
        private final TextView sectionName;

        public SongHeaderHolder(View view) {
            super(view);
            this.sectionName = (TextView) itemView.findViewById(R.id.section_name);
        }
    }

    private void openSong(Song song) {
        SongFragment songFragment = new SongFragment();
        Bundle args = new Bundle();
        args.putInt(SongFragment.SONG_ID, song.getId());
        songFragment.setArguments(args);

        songsFragment.setSongFragment(songFragment);

        MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();

        FragmentTransaction transaction = mainActivity.getFragmentManager().beginTransaction();
        transaction.replace(R.id.song_fragment, songFragment);
        transaction.addToBackStack(SongFragment.TAG);
        transaction.commit();
        mainActivity.songVisible = true;
    }
}
