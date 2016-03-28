package com.chaemil.hgms.adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Homepage;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.view.VideoThumbImageView;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by chaemil on 28.3.16.
 */
public class HomepageAdapter extends ArrayAdapter<Object> {
    private final Context context;
    private final MainActivity mainActivity;
    private final Homepage homepage;
    private final int display;

    public HomepageAdapter(Context context, MainActivity mainActivity, Homepage homepage, int display) {
        super(context, 0);
        this.context = context;
        this.homepage = homepage;
        this.mainActivity = mainActivity;
        this.display = display;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.archive_item, null);

            holder = new ViewHolder();
            holder.mainView = (RelativeLayout) convertView.findViewById(R.id.main_view);
            holder.thumb = (VideoThumbImageView) convertView.findViewById(R.id.thumb);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.views = (TextView) convertView.findViewById(R.id.views);
            holder.type = (ImageView) convertView.findViewById(R.id.type_drawable);
            holder.more = (ImageButton) convertView.findViewById(R.id.context_menu);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Object item = null;

        switch (display) {
            case 0:
                item = homepage.newestVideos.get(position);
                break;
            case 1:
                item = homepage.newestAlbums.get(position);
                break;
            case 3:
                item = homepage.popularVideos.get(position);
                break;
        }

        if (item instanceof Video) {

            final Video video = (Video) item;

            holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.thumb_corner_primary_color));
            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainActivity.playNewVideo(video);
                }
            });
            holder.name.setText(video.getName());
            holder.date.setText(video.getDate());
            holder.views.setText(video.getViews() + " " + context.getString(R.string.views));
            Ion.with(context).load(video.getThumbFile()).intoImageView(holder.thumb);
        }

        if (item instanceof PhotoAlbum) {

            final PhotoAlbum photoAlbum = (PhotoAlbum) item;

            holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.thumb_corner_green_color));
            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAlbum(photoAlbum);
                }
            });
            holder.name.setText(photoAlbum.getName());
            holder.date.setText(photoAlbum.getDate());
            holder.views.setText(context.getString(R.string.photo_album));
            holder.more.setVisibility(View.GONE);
            Ion.with(context).load(photoAlbum.getThumbs().getThumb512()).intoImageView(holder.thumb);
        }

        return convertView;
    }

    private void openAlbum(PhotoAlbum album) {
        mainActivity.getMainFragment().openAlbum(album);
    }

    @Override
    public int getCount() {
        switch (display) {
            case 0:
                return homepage.newestVideos.size();
            case 1:
                return homepage.newestAlbums.size();
            case 3:
                return homepage.popularVideos.size();
        }
        return 0;
    }

    public class ViewHolder {

        private RelativeLayout mainView;
        public VideoThumbImageView thumb;
        public TextView name;
        public TextView date;
        public TextView views;
        public ImageView type;
        public ImageButton more;

    }
}
