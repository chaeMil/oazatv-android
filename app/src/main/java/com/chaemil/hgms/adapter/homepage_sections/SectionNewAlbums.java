package com.chaemil.hgms.adapter.homepage_sections;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.adapter.holder.VideoViewHolder;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.StringUtils;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

/**
 * Created by chaemil on 27.8.16.
 */
public class SectionNewAlbums extends BaseSection {

    private final Context context;
    ArrayList<PhotoAlbum> archive = new ArrayList<>();

    public SectionNewAlbums(Context context, ArrayList<PhotoAlbum> archive) {
        super(R.layout.homepage_section_header,
                R.layout.homepage_section_footer,
                AdapterUtils.getArchiveLayout(context));
        this.context = context;
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

        final PhotoAlbum photoalbum = archive.get(position);

        videoViewHolder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                mainActivity.getMainFragment().openAlbum(photoalbum);
            }
        });
        videoViewHolder.name.setText(photoalbum.getName());
        videoViewHolder.date.setText(StringUtils.formatDate(photoalbum.getDate(), context));
        videoViewHolder.views.setVisibility(View.GONE);
        videoViewHolder.more.setVisibility(View.VISIBLE);
        videoViewHolder.more.setVisibility(View.GONE);

        int thumbWidth = videoViewHolder.thumb.getWidth();

        Ion.with(context)
                .load(photoalbum.getThumbs().getThumb1024())
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
        headerHolder.sectionName.setText(context.getString(R.string.newest_albums));
        headerHolder.sectionIcon.setImageDrawable(context.getResources()
                .getDrawable(R.drawable.newest_albums));
    }
}
