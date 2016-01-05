package com.chaemil.hgms.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.fragment.PlayerFragment;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.SmartLog;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by chaemil on 20.12.15.
 */
public class ArchiveAdapter extends ArrayAdapter<ArchiveItem> {

    private final Context context;
    private final PlayerFragment playerFragment;
    private ArrayList<ArchiveItem> archive;

    public ArchiveAdapter(Context context, int resource, PlayerFragment playerFragment, ArrayList<ArchiveItem> archive) {
        super(context, resource);
        this.context = context;
        this.playerFragment = playerFragment;
        this.archive = archive;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        SmartLog.Log(SmartLog.LogLevel.DEBUG, "getView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.archive_item, null);

            holder = new ViewHolder();
            holder.mainView = (RelativeLayout) convertView.findViewById(R.id.main_view);
            holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.views = (TextView) convertView.findViewById(R.id.views);
            holder.playAudio = (ImageView) convertView.findViewById(R.id.play_audio);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        ArchiveItem archiveItem = archive.get(position);

        switch (archiveItem.getType()) {
            case ArchiveItem.Type.VIDEO:

                final Video video = archiveItem.getVideo();

                holder.mainView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playerFragment.playNewVideo(video);
                    }
                });
                holder.name.setText(video.getName());
                holder.date.setText(video.getDate());
                holder.views.setText(video.getViews() + " " + context.getString(R.string.views));
                holder.playAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                Picasso.with(context).load(video.getThumbFile()).into(holder.thumb);
                break;
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return archive.size();
    }

    public class ViewHolder {

        private RelativeLayout mainView;
        public ImageView thumb;
        public TextView name;
        public TextView date;
        public TextView views;
        public ImageView playAudio;

    }


}
