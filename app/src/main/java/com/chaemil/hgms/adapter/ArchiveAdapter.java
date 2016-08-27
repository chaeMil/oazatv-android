package com.chaemil.hgms.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
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
import java.util.List;

/**
 * Created by chaemil on 20.12.15.
 */
public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.ViewHolder> {

    private final Context context;
    private final MainActivity mainActivity;
    private final int layout;
    private final ArrayList<Video> videos;
    private ArrayList<ArchiveItem> archive;

    public ArchiveAdapter(Context context, int layout, MainActivity mainActivity,
                          ArrayList<ArchiveItem> archive) {
        this.context = context;
        this.archive = archive;
        this.mainActivity = mainActivity;
        this.layout = layout;
        this.videos = null;
    }

    public ArchiveAdapter(Context context, MainActivity mainActivity, int layout,
                          ArrayList<Video> archive) {
        this.archive = null;
        this.videos = archive;
        this.layout = layout;
        this.mainActivity = mainActivity;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        ArchiveAdapter.ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ArchiveItem archiveItem = null;
        if (archive != null) {
            archiveItem = archive.get(position);
        }

        if (videos != null) {
            archiveItem = new ArchiveItem();
            archiveItem.setVideo(videos.get(position));
        }

        if (archiveItem != null) {
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

                    setupTime(holder, video);

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
        }

    }

    private void setupTime(final ViewHolder holder, final Video video) {
        holder.viewProgress.setVisibility(View.GONE);
        holder.time.setVisibility(View.GONE);

        new AsyncTask<Void, Void, Void>() {

            public Video savedVideo;

            @Override
            protected Void doInBackground( Void... voids ) {
                savedVideo = Video.findByServerId(video.getServerId());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                holder.viewProgress.setVisibility(View.VISIBLE);
                if (savedVideo != null && savedVideo.equals(video)) {
                    holder.viewProgress.setMax(video.getDuration());
                    holder.viewProgress.setProgress(savedVideo.getCurrentTime() / 1000);
                } else {
                    holder.viewProgress.setMax(100);
                    holder.viewProgress.setProgress(0);
                }

                holder.time.setVisibility(View.VISIBLE);
                holder.time.setText(StringUtils.getDurationString(video.getDuration()));
            }
        }.execute();
    }

    private void openAlbum(PhotoAlbum album) {
        mainActivity.getMainFragment().openAlbum(album);
    }

    @Override
    public int getItemCount() {
        if (archive != null) {
            return archive.size();
        }
        if (videos != null) {
            return videos.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private RelativeLayout mainView;
        public VideoThumbImageView thumb;
        public TextView name;
        public TextView date;
        public TextView views;
        public ImageButton more;
        public ProgressBar viewProgress;
        public TextView time;

        public ViewHolder(View itemView) {
            super(itemView);
            this.mainView = (RelativeLayout) itemView.findViewById(R.id.main_view);
            this.thumb = (VideoThumbImageView) itemView.findViewById(R.id.thumb);
            this.name = (TextView) itemView.findViewById(R.id.name);
            this.date = (TextView) itemView.findViewById(R.id.date);
            this.views = (TextView) itemView.findViewById(R.id.views);
            this.more = (ImageButton) itemView.findViewById(R.id.context_menu);
            this.viewProgress = (ProgressBar) itemView.findViewById(R.id.view_progress);
            this.time = (TextView) itemView.findViewById(R.id.video_time);
        }
    }


}
