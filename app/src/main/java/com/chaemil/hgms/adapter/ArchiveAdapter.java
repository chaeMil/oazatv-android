package com.chaemil.hgms.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.DownloadService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by chaemil on 20.12.15.
 */
public class ArchiveAdapter extends ArrayAdapter<ArchiveItem> {

    private final Context context;
    private final MainActivity mainActivity;
    private ArrayList<ArchiveItem> archive;

    public ArchiveAdapter(Context context, int resource, MainActivity mainActivity, ArrayList<ArchiveItem> archive) {
        super(context, resource);
        this.context = context;
        this.archive = archive;
        this.mainActivity = mainActivity;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

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
            holder.download = (ImageView) convertView.findViewById(R.id.download_audio);

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
                       mainActivity.playNewVideo(video);
                    }
                });
                holder.name.setText(video.getName());
                holder.date.setText(video.getDate());
                holder.views.setText(video.getViews() + " " + context.getString(R.string.views));
                holder.playAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivity.playNewAudio(video, false);
                    }
                });
                holder.download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((OazaApp) mainActivity.getApplication()).addToDownloadQueue(video);
                        Intent downloadService = new Intent(mainActivity, DownloadService.class);
                        mainActivity.startService(downloadService);
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
        public ImageView download;

    }


}
