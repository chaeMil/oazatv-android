package com.chaemil.hgms.ui.adapter.sections;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.ui.activity.MainActivity;
import com.chaemil.hgms.ui.adapter.holder.HeaderViewHolder;
import com.chaemil.hgms.ui.adapter.holder.VideoViewHolder;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.DimensUtils;
import com.chaemil.hgms.utils.StringUtils;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

/**
 * Created by chaemil on 27.8.16.
 */
public class SectionPopularVideos extends BaseSection {

    private final Context context;
    private final int displayWidth;
    ArrayList<Video> archive = new ArrayList<>();

    public SectionPopularVideos(Context context, ArrayList<Video> archive) {
        super(R.layout.homepage_section_header,
                R.layout.homepage_section_footer,
                AdapterUtils.getArchiveLayout(context));
        this.context = context;
        this.archive = archive;
        MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
        this.displayWidth = DimensUtils.getDisplayHeight(mainActivity);
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
                MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                if (mainActivity.isSomethingPlaying()) {
                    AdapterUtils.contextDialog(context, video, false);
                } else {
                    mainActivity.playNewVideo(video);
                }
            }
        });
        videoViewHolder.name.setText(video.getName());
        videoViewHolder.date.setText(StringUtils.formatDate(video.getDate(), context));
        videoViewHolder.views.setText(video.getViews() + " " + context.getString(R.string.views));
        videoViewHolder.more.setVisibility(View.VISIBLE);
        videoViewHolder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdapterUtils.contextDialog(context, video, false);
            }
        });
        videoViewHolder.thumb.setBackgroundColor(Color.parseColor(video.getThumbColor()));
        videoViewHolder.time.setText(StringUtils.getDurationString(video.getDuration()));
        videoViewHolder.cc.setVisibility(video.getSubtitlesFile() != null ? View.VISIBLE : View.GONE);
        videoViewHolder.language.setVisibility(video.getVideoLanguage(context) != null ? View.VISIBLE : View.GONE);
        videoViewHolder.language.setText(video.getVideoLanguage(context));
        if (video.isAudioDownloaded(context)) {
            videoViewHolder.downloaded.setVisibility(View.VISIBLE);
            videoViewHolder.downloaded.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                    mainActivity.playNewAudio(video);
                }
            });
        } else {
            videoViewHolder.downloaded.setVisibility(View.GONE);
        }

        setupTime(videoViewHolder, video);

        Ion.with(context)
                .load(video.getThumbFile())
                .withBitmap()
                .resize(displayWidth, (int) (displayWidth * 0.5625))
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
        headerHolder.sectionName.setText(context.getString(R.string.popular_videos));
        headerHolder.sectionIcon.setImageDrawable(context.getResources()
                .getDrawable(R.drawable.popular_videos));
    }
}
