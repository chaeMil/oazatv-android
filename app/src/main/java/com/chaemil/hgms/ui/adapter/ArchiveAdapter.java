package com.chaemil.hgms.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.ui.activity.MainActivity;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.StringUtils;
import com.chaemil.hgms.ui.view.VideoThumbImageView;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

/**
 * Created by chaemil on 20.12.15.
 */
public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.ViewHolder> {

    private final Context context;
    private final int layout;
    private final ArrayList<Video> videos;
    private ArrayList<ArchiveItem> archive;

    public ArchiveAdapter(Context context, int layout, ArrayList<ArchiveItem> archive) {
        this.context = context;
        this.archive = archive;
        this.layout = layout;
        this.videos = null;
    }

    public ArchiveAdapter(Context context, ArrayList<Video> videos, int layout) {
        this.archive = null;
        this.videos = videos;
        this.layout = layout;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        return new ViewHolder(v);
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
                            MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                            if (mainActivity.isSomethingPlaying()) {
                                AdapterUtils.contextDialog(context, video, false);
                            } else {
                                mainActivity.playNewVideo(video);
                            }
                        }
                    });
                    holder.name.setText(video.getName());
                    holder.date.setText(StringUtils.formatDate(video.getDate(), context));
                    holder.views.setText(video.getViews() + " " + context.getString(R.string.views));
                    holder.more.setVisibility(View.VISIBLE);
                    holder.more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AdapterUtils.contextDialog(context, video, false);
                        }
                    });
                    holder.thumb.setBackgroundColor(Color.parseColor(video.getThumbColor()));
                    holder.time.setText(StringUtils.getDurationString(video.getDuration()));
                    holder.cc.setVisibility(video.getSubtitlesFile() != null ? View.VISIBLE : View.GONE);
                    holder.language.setVisibility(video.getVideoLanguage(context) != null ? View.VISIBLE : View.GONE);
                    holder.language.setText(video.getVideoLanguage(context));
                    if (video.isAudioDownloaded(context)) {
                        holder.downloaded.setVisibility(View.VISIBLE);
                        holder.downloaded.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                                mainActivity.playNewAudio(video);
                            }
                        });
                    } else {
                        holder.downloaded.setVisibility(View.GONE);
                    }

                    setupTime(holder, video);

                    int thumbWidth = holder.thumb.getWidth();

                    Ion.with(context)
                            .load(video.getThumbFile())
                            .withBitmap()
                            .resize(thumbWidth, (int) (thumbWidth * 0.5625))
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
                    holder.cc.setVisibility(View.GONE);
                    holder.language.setVisibility(View.GONE);
                    holder.downloaded.setVisibility(View.GONE);

                    int thumbWidthAlbum = holder.thumb.getWidth();

                    Ion.with(context)
                            .load(photoAlbum.getThumbs().getThumb512())
                            .withBitmap()
                            .resize(thumbWidthAlbum, (int) (thumbWidthAlbum * 0.5625))
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
            }
        }.execute();
    }

    private void openAlbum(PhotoAlbum album) {
        MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
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

        public RelativeLayout mainView;
        public ImageButton downloaded;
        public VideoThumbImageView thumb;
        public TextView name;
        public TextView date;
        public TextView views;
        public ImageButton more;
        public ProgressBar viewProgress;
        public TextView time;
        public TextView cc;
        public TextView language;

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
            this.downloaded = (ImageButton) itemView.findViewById(R.id.downloaded);
            this.cc = (TextView) itemView.findViewById(R.id.cc);
            this.language = (TextView) itemView.findViewById(R.id.language);
        }
    }


}
