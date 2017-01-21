package com.chaemil.hgms.adapter.homepage_sections;

import android.content.Context;
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
public class SectionContinueWatching extends BaseSection {

    public static final String TAG = "continue_watching";
    private final Context context;
    private final MainActivity mainActivity;
    ArrayList<Video> videosToWatch = new ArrayList<>();

    public SectionContinueWatching(Context context, MainActivity mainActivity, ArrayList<Video> videosToWatch) {
        super(R.layout.homepage_section_header,
                R.layout.homepage_section_footer,
                AdapterUtils.getArchiveLayout(context));
        this.context = context;
        this.mainActivity = mainActivity;
        this.videosToWatch = videosToWatch;
    }

    @Override
    public int getContentItemsTotal() {
        return videosToWatch.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
        final Video video = videosToWatch.get(position);

        videoViewHolder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainActivity.isSomethingPlaying()) {
                    AdapterUtils.contextDialog(context, mainActivity, video, false);
                } else {
                    mainActivity.playNewVideo(video);
                }
            }
        });
        videoViewHolder.name.setText(video.getName());
        videoViewHolder.date.setText(StringUtils.formatDate(video.getDate(), context));
        videoViewHolder.views.setVisibility(View.GONE);
        videoViewHolder.viewProgress.setMax(video.getDuration());
        videoViewHolder.viewProgress.setProgress(video.getCurrentTime());
        videoViewHolder.time.setText(StringUtils.getDurationString(video.getDuration()));
        videoViewHolder.cc.setVisibility(video.getSubtitlesFile() != null ? View.VISIBLE : View.GONE);
        videoViewHolder.language.setVisibility(video.getVideoLanguage(context) != null ? View.VISIBLE : View.GONE);
        videoViewHolder.language.setText(video.getVideoLanguage(context));
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

        int thumbWidth = videoViewHolder.thumb.getWidth();

        Ion.with(context)
                .load(video.getThumbFile())
                .withBitmap()
                .resize(thumbWidth, (int) (thumbWidth * 0.5625))
                .intoImageView(videoViewHolder.thumb);

        setupTime(videoViewHolder, video);

        videoViewHolder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdapterUtils.contextDialog(context, mainActivity, video, true);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        super.onBindHeaderViewHolder(holder);

        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        headerHolder.sectionName.setText(context.getString(R.string.continue_watching));
        headerHolder.sectionIcon.setImageDrawable(context.getResources()
                .getDrawable(R.drawable.continue_watching));
    }
}
