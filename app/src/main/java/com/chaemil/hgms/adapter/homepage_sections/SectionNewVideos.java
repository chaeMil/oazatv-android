package com.chaemil.hgms.adapter.homepage_sections;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.holder.VideoViewHolder;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.StringUtils;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

/**
 * Created by chaemil on 27.8.16.
 */
public class SectionNewVideos extends BaseSection {

    private final Context context;
    private final MainActivity mainActivity;
    ArrayList<Video> archive = new ArrayList<>();

    public SectionNewVideos(Context context, MainActivity mainActivity, ArrayList<Video> archive) {
        super(R.layout.homepage_section_header,
                R.layout.homepage_section_footer,
                AdapterUtils.getArchiveLayout(context));
        this.context = context;
        this.mainActivity = mainActivity;
        this.archive = archive;
    }

    @Override
    public int getContentItemsTotal() {
        return archive.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoViewHolder videoViewHolder = (VideoViewHolder) holder;

        final Video video = archive.get(position);

        videoViewHolder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.playNewVideo(video);
            }
        });
        videoViewHolder.name.setText(video.getName());
        videoViewHolder.date.setText(StringUtils.formatDate(video.getDate(), context));
        videoViewHolder.views.setText(video.getViews() + " " + context.getString(R.string.views));
        videoViewHolder.more.setVisibility(View.VISIBLE);
        videoViewHolder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdapterUtils.contextDialog(context, mainActivity, video, false);
            }
        });
        videoViewHolder.thumb.setBackgroundColor(Color.parseColor(video.getThumbColor()));
        videoViewHolder.time.setText(StringUtils.getDurationString(video.getDuration()));
        if (video.isAudioDownloaded(context)) {
            videoViewHolder.downloaded.setVisibility(View.VISIBLE);
            videoViewHolder.downloaded.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainActivity.playNewAudio(video);
                }
            });
        } else {
            videoViewHolder.downloaded.setVisibility(View.GONE);
        }

        setupTime(videoViewHolder, video);

        int thumbWidth = videoViewHolder.thumb.getWidth();

        Ion.with(context)
                .load(video.getThumbFile())
                .withBitmap()
                .resize(thumbWidth, (int) (thumbWidth * 0.5625))
                .intoImageView(videoViewHolder.thumb);
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        super.onBindHeaderViewHolder(holder);

        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        headerHolder.sectionName.setText(context.getString(R.string.newest_videos));
        headerHolder.sectionIcon.setImageDrawable(context.getResources()
                .getDrawable(R.drawable.newest_videos));
    }
}
