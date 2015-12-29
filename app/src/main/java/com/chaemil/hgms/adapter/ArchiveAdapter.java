package com.chaemil.hgms.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.Video;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by chaemil on 20.12.15.
 */
public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<ArchiveItem> archive;

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView thumb;
        public TextView name;
        public TextView date;
        public TextView views;

        public ViewHolder(View itemView) {
            super(itemView);
            thumb = (ImageView) itemView.findViewById(R.id.thumb);
            name = (TextView) itemView.findViewById(R.id.name);
            date = (TextView) itemView.findViewById(R.id.date);
            views = (TextView) itemView.findViewById(R.id.views);
        }

    }

    public ArchiveAdapter(Context context, ArrayList<ArchiveItem> archive) {
        this.context = context;
        this.archive = archive;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.archive_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ArchiveItem archiveItem = archive.get(position);

        switch (archiveItem.getType()) {
            case ArchiveItem.Type.VIDEO:

                Video video = archiveItem.getVideo();

                holder.name.setText(video.getName());
                holder.date.setText(video.getDate());
                holder.views.setText(video.getViews() + " " + context.getString(R.string.views));
                Picasso.with(context).load(video.getThumbFile()).into(holder.thumb);
                break;
        }


    }

    @Override
    public int getItemCount() {
        return archive.size();
    }


}
