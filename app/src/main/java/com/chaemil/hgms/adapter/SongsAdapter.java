package com.chaemil.hgms.adapter;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.fragment.SongFragment;
import com.chaemil.hgms.fragment.SongsFragment;
import com.chaemil.hgms.model.Song;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import java.util.ArrayList;

/**
 * Created by chaemil on 20.4.16.
 */
public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder>
        implements SectionTitleProvider {

    private final SongsFragment songsFragment;
    private final ArrayList<Song> songs;
    private final Context context;
    private MainActivity mainActivity;

    public SongsAdapter(Context context, MainActivity mainActivity,
                        SongsFragment songsFragment, ArrayList<Song> songs) {
        this.mainActivity = mainActivity;
        this.songsFragment = songsFragment;
        this.context = context;
        this.songs = songs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_name, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ViewHolder songViewHolder = (ViewHolder) holder;

        final Song song = songs.get(position);

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
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public String getSectionTitle(int position) {
        return songs.get(position).getName().substring(0, 1);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout mainView;
        public TextView tag;
        public TextView name;
        public TextView author;

        public ViewHolder(View itemView) {
            super(itemView);
            this.mainView = (RelativeLayout) itemView.findViewById(R.id.main_view);
            this.tag = (TextView) itemView.findViewById(R.id.tag);
            this.name = (TextView) itemView.findViewById(R.id.name);
            this.author = (TextView) itemView.findViewById(R.id.author);
        }
    }
}