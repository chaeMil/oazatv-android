package com.chaemil.hgms.ui.tv.view;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.model.Category;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.StringUtils;
import com.koushikdutta.ion.Ion;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Michal Mlejnek on 22/02/2018.
 */

public class HomeCardView extends BindableCardView<Object> {

    @BindView(R.id.thumb)
    ImageView thumb;

    @BindView(R.id.name)
    TextView videoName;

    @BindView(R.id.cc)
    TextView cc;

    @BindView(R.id.language)
    TextView language;

    @BindView(R.id.video_time)
    TextView videoTime;

    @BindView(R.id.video)
    LinearLayout videoWrapper;

    @BindView(R.id.category)
    RelativeLayout categoryWrapper;

    @BindView(R.id.category_name)
    TextView category;

    private final Context context;

    public HomeCardView(Context context) {
        super(context);
        this.context = context;
        ButterKnife.bind(this);
    }

    @Override
    public void bind(Object data) {
        if (data instanceof Video) {
            bindVideo((Video) data);
        }
        if (data instanceof Category) {
            bindCategory((Category) data);
        }
    }

    public void bindVideo(Video video) {
        videoWrapper.setVisibility(VISIBLE);
        categoryWrapper.setVisibility(GONE);

        Ion.with(context)
                .load(video.getThumbFile())
                .intoImageView(thumb);
        videoName.setText(video.getName());
        cc.setVisibility(video.getSubtitlesFile() != null ? View.VISIBLE : View.GONE);
        videoTime.setText(StringUtils.getDurationString(video.getDuration()));
        language.setVisibility(video.getVideoLanguage(context) != null ? View.VISIBLE : View.GONE);
        language.setText(video.getVideoLanguage(context));
    }

    public void bindCategory(Category item) {
        videoWrapper.setVisibility(GONE);
        categoryWrapper.setVisibility(VISIBLE);

        categoryWrapper.setBackgroundColor(Color.parseColor(item.getColor()));
        category.setText(item.getName());
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.tv_home_card_view;
    }
}
