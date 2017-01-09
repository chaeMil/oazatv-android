package com.chaemil.hgms.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.StringUtils;
import com.koushikdutta.ion.Ion;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by chaemil on 20.12.15.
 */
public class SearchAdapter extends ArrayAdapter<ArchiveItem> {

    private final Context context;
    private final MainActivity mainActivity;
    private ArrayList<ArchiveItem> result;

    public SearchAdapter(Context context, int resource, MainActivity mainActivity, ArrayList<ArchiveItem> result) {
        super(context, resource);
        this.context = context;
        this.result = result;
        this.mainActivity = mainActivity;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.search_item, null);

            holder = new ViewHolder();
            holder.mainView = (RelativeLayout) convertView.findViewById(R.id.main_view);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.info = (TextView) convertView.findViewById(R.id.info);
            holder.contextMenu = (ImageView) convertView.findViewById(R.id.context_menu);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        ArchiveItem archiveItem = result.get(position);

        switch (archiveItem.getType()) {
            case ArchiveItem.Type.VIDEO:

                final Video video = archiveItem.getVideo();

                holder.mainView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mainActivity.playNewVideo(video);
                    }
                });
                holder.name.setText(video.getName());
                holder.thumb.setBackgroundColor(Color.parseColor(video.getThumbColor()));
                holder.date.setText(StringUtils.formatDate(video.getDate(), context));
                holder.info.setText(video.getViews() + " " + context.getString(R.string.views));
                holder.contextMenu.setVisibility(View.VISIBLE);
                holder.contextMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AdapterUtils.contextDialog(context, mainActivity, video, false);
                    }
                });
                Ion.with(context)
                        .load(video.getThumbFile())
                        .intoImageView(holder.thumb);
                break;

            case ArchiveItem.Type.ALBUM:

                final PhotoAlbum photoAlbum = archiveItem.getAlbum();

                holder.mainView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mainActivity.getMainFragment().openAlbum(photoAlbum);
                    }
                });
                holder.name.setText(photoAlbum.getName());
                holder.date.setText(StringUtils.formatDate(photoAlbum.getDate(), context));
                holder.info.setText(context.getString(R.string.photo_album));
                holder.contextMenu.setVisibility(View.GONE);
                Ion.with(context)
                        .load(photoAlbum.getThumbs().getThumb256())
                        .intoImageView(holder.thumb);
                break;
        }

        return convertView;
    }


    @Override
    public int getCount() {
        return result.size();
    }

    public class ViewHolder {

        public RelativeLayout mainView;
        public ImageView thumb;
        public TextView name;
        public TextView date;
        public TextView views;
        public ImageView download;
        public TextView info;
        public ImageView contextMenu;
    }


}
