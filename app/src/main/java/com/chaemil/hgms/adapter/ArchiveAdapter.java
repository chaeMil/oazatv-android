package com.chaemil.hgms.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.chaemil.hgms.utils.StringUtils;
import com.chaemil.hgms.view.VideoThumbImageView;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

/**
 * Created by chaemil on 20.12.15.
 */
public class ArchiveAdapter extends ArrayAdapter<ArchiveItem> {

    private final Context context;
    private final MainActivity mainActivity;
    private final int layout;
    private ArrayList<ArchiveItem> archive;

    public ArchiveAdapter(Context context, int layout, MainActivity mainActivity,
                          ArrayList<ArchiveItem> archive) {
        super(context, 0);
        this.context = context;
        this.archive = archive;
        this.mainActivity = mainActivity;
        this.layout = layout;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(layout, null);

            holder = new ViewHolder();
            holder.mainView = (RelativeLayout) convertView.findViewById(R.id.main_view);
            holder.thumb = (VideoThumbImageView) convertView.findViewById(R.id.thumb);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.views = (TextView) convertView.findViewById(R.id.views);
            holder.more = (ImageButton) convertView.findViewById(R.id.context_menu);
            holder.viewProgress = (ProgressBar) convertView.findViewById(R.id.view_progress);
            holder.time = (TextView) convertView.findViewById(R.id.video_time);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        ArchiveItem archiveItem = archive.get(position);

        switch (archiveItem.getType()) {
            case ArchiveItem.Type.VIDEO:

                final Video video = archiveItem.getVideo();
                Video savedVideo = Video.findByServerId(video.getServerId());

                holder.mainView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivity.playNewVideo(video);
                    }
                });
                holder.name.setText(video.getName());
                holder.date.setText(StringUtils.formatDate(video.getDate(), context));
                holder.views.setText(video.getViews() + " " + context.getString(R.string.views));
                holder.more.setVisibility(View.VISIBLE);
                holder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AdapterUtils.contextDialog(context, mainActivity, video);
                    }
                });
                holder.thumb.setBackgroundColor(Color.parseColor(video.getThumbColor()));

                holder.viewProgress.setVisibility(View.VISIBLE);
                if (savedVideo != null) {
                    holder.viewProgress.setMax(video.getDuration());
                    holder.viewProgress.setProgress(savedVideo.getCurrentTime() / 1000);
                }

                holder.time.setVisibility(View.VISIBLE);
                holder.time.setText(StringUtils.getDurationString(video.getDuration()));

                Ion.with(context)
                        .load(video.getThumbFile())
                        .intoImageView(holder.thumb);

                break;

            case ArchiveItem.Type.ALBUM:

                final PhotoAlbum photoAlbum = archiveItem.getAlbum();

                holder.mainView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openAlbum(photoAlbum);
                    }
                });
                holder.name.setText(photoAlbum.getName());
                holder.date.setText(StringUtils.formatDate(photoAlbum.getDate(), context));
                holder.views.setText(context.getString(R.string.photo_album));
                holder.more.setVisibility(View.GONE);
                holder.viewProgress.setVisibility(View.GONE);
                holder.time.setVisibility(View.GONE);

                Ion.with(context)
                        .load(photoAlbum.getThumbs().getThumb512())
                        .intoImageView(holder.thumb);
                break;
        }

        return convertView;
    }

    private void openAlbum(PhotoAlbum album) {
        mainActivity.getMainFragment().openAlbum(album);
    }

    @Override
    public int getCount() {
        return archive.size();
    }

    public class ViewHolder {

        private RelativeLayout mainView;
        public VideoThumbImageView thumb;
        public TextView name;
        public TextView date;
        public TextView views;
        public ImageButton more;
        public ProgressBar viewProgress;
        public TextView time;
    }


}
