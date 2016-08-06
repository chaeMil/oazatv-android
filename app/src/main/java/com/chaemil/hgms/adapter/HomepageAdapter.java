package com.chaemil.hgms.adapter;

import android.content.Context;
import android.graphics.Color;
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

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.Homepage;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.StringUtils;
import com.chaemil.hgms.view.VideoThumbImageView;
import com.koushikdutta.ion.Ion;

/**
 * Created by chaemil on 28.3.16.
 */
public class HomepageAdapter extends SectionedRecyclerViewAdapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final MainActivity mainActivity;
    private final Homepage homepage;
    private final LayoutInflater inflater;

    public HomepageAdapter(Context context, MainActivity mainActivity, Homepage homepage) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.homepage = homepage;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getSectionCount() {
        return 4;
    }

    @Override
    public int getItemCount(int section) {
        switch (section) {
            case 0:
                return homepage.featured.size();
            case 1:
                return homepage.newestVideos.size();
            case 2:
                return homepage.newestAlbums.size();
            case 3:
                return homepage.popularVideos.size();
        }

        return 0;
    }

    private String getSectionName(int section) {
        switch (section) {
            case 0:
                return context.getString(R.string.featured);
            case 1:
                return context.getString(R.string.newest_videos);
            case 2:
                return context.getString(R.string.newest_albums);
            case 3:
                return context.getString(R.string.popular_videos);
        }

        return "";
    }

    private Object getItem(int section, int relativePosition) {
        switch (section) {
            case 0:
                if (homepage.featured.get(relativePosition).getType() == ArchiveItem.Type.VIDEO) {
                    return homepage.featured.get(relativePosition).getVideo();
                }
                if (homepage.featured.get(relativePosition).getType() == ArchiveItem.Type.ALBUM) {
                    return homepage.featured.get(relativePosition).getAlbum();
                }
                return homepage.featured.get(relativePosition);
            case 1:
                return homepage.newestVideos.get(relativePosition);
            case 2:
                return homepage.newestAlbums.get(relativePosition);
            case 3:
                return homepage.popularVideos.get(relativePosition);
        }

        return null;
    }

    private int getLayout(int section, int relativePosition) {
        switch (section) {
            case 0:
                return R.layout.featured_item;
            case 1:
                return R.layout.archive_item;
            case 2:
                return R.layout.archive_item;
            case 3:
                return R.layout.archive_item;
        }

        return 0;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int section) {
        ((HeaderViewHolder) holder).sectionName.setText(getSectionName(section));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int section, int relativePosition,
                                 int absolutePosition) {

        Object item = getItem(section, relativePosition);

        if (item instanceof Video) {
            final Video video = (Video) item;

            ((ViewHolder) holder).mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainActivity.playNewVideo(video);
                }
            });
            ((ViewHolder) holder).name.setText(video.getName());
            ((ViewHolder) holder).date.setText(StringUtils.formatDate(video.getDate(), context));
            ((ViewHolder) holder).views.setText(video.getViews() + " " + context.getString(R.string.views));
            ((ViewHolder) holder).more.setVisibility(View.VISIBLE);
            ((ViewHolder) holder).more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AdapterUtils.contextDialog(context, mainActivity, video);
                }
            });
            ((ViewHolder) holder).thumb.setBackgroundColor(Color.parseColor(video.getThumbColor()));

            setupTime(((ViewHolder) holder), video);

            Ion.with(context)
                    .load(video.getThumbFile())
                    .intoImageView(((ViewHolder) holder).thumb);

        }

        if (item instanceof PhotoAlbum) {

            final PhotoAlbum photoAlbum = (PhotoAlbum) item;

            ((ViewHolder) holder).mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAlbum(photoAlbum);
                }
            });
            ((ViewHolder) holder).name.setText(photoAlbum.getName());
            ((ViewHolder) holder).date.setText(StringUtils.formatDate(photoAlbum.getDate(), context));
            ((ViewHolder) holder).views.setText(context.getString(R.string.photo_album));
            ((ViewHolder) holder).more.setVisibility(View.GONE);

            ((ViewHolder) holder).viewProgress.setVisibility(View.GONE);
            ((ViewHolder) holder).time.setVisibility(View.GONE);

            Ion.with(context)
                    .load(photoAlbum.getThumbs().getThumb1024())
                    .intoImageView(((ViewHolder) holder).thumb);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_HEADER:
                View header = inflater.inflate(R.layout.homepage_section_header,
                        parent, false);
                return new HeaderViewHolder(header);
            case VIEW_TYPE_ITEM:
                View item = inflater.inflate(R.layout.featured_item,
                        parent, false);
                return new ViewHolder(item);
        }
        return null;

    }

    private void openAlbum(PhotoAlbum album) {
        mainActivity.getMainFragment().openAlbum(album);
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder{
        public final TextView sectionName;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            this.sectionName = (TextView) itemView.findViewById(R.id.section_name);
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

    public class ViewHolder extends RecyclerView.ViewHolder{
        public final RelativeLayout mainView;
        public final VideoThumbImageView thumb;
        public final TextView name;
        public final TextView date;
        public final TextView views;
        public final ImageButton more;
        public final ProgressBar viewProgress;
        public final TextView time;

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
