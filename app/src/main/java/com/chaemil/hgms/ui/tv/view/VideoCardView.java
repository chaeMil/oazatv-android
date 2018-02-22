package com.chaemil.hgms.ui.tv.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.databinding.TvVideoCardViewBinding;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.ui.tv.view.BindableCardView;
import com.chaemil.hgms.utils.StringUtils;
import com.koushikdutta.ion.Ion;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Michal Mlejnek on 22/02/2018.
 */

public class VideoCardView extends BindableCardView<Video> {

    @BindView(R.id.thumb)
    ImageView thumb;

    @BindView(R.id.name)
    TextView name;

    @BindView(R.id.cc)
    TextView cc;

    @BindView(R.id.language)
    TextView language;

    @BindView(R.id.video_time)
    TextView videoTime;

    private final Context context;

    public VideoCardView(Context context) {
        super(context);
        this.context = context;
        ButterKnife.bind(this);
    }

    @Override
    public void bind(Video video) {
        Ion.with(context)
                .load(video.getThumbFile())
                .intoImageView(thumb);
        name.setText(video.getName());
        cc.setVisibility(video.getSubtitlesFile() != null ? View.VISIBLE : View.GONE);
        videoTime.setText(StringUtils.getDurationString(video.getDuration()));
        language.setVisibility(video.getVideoLanguage(context) != null ? View.VISIBLE : View.GONE);
        language.setText(video.getVideoLanguage(context));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.tv_video_card_view;
    }
}
