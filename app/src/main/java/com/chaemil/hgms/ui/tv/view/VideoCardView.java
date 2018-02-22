package com.chaemil.hgms.ui.tv.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.databinding.TvVideoCardViewBinding;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.ui.tv.view.BindableCardView;
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
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.tv_video_card_view;
    }
}
