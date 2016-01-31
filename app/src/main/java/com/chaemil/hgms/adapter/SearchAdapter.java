package com.chaemil.hgms.adapter;

import android.content.Context;
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
import com.koushikdutta.ion.Ion;

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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.search_item, null);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        ArchiveItem archiveItem = result.get(position);

        switch (archiveItem.getType()) {
            case ArchiveItem.Type.VIDEO:

                final Video video = archiveItem.getVideo();

                holder.name.setText(video.getName());
                Ion.with(context)
                        .load(video.getThumbFile())
                        .intoImageView(holder.thumb);
                break;

            case ArchiveItem.Type.ALBUM:

                final PhotoAlbum photoAlbum = archiveItem.getAlbum();

                holder.name.setText(photoAlbum.getName());
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

        private RelativeLayout mainView;
        public ImageView thumb;
        public TextView name;
        public TextView date;
        public TextView views;
        public ImageView playAudio;
        public ImageView download;

    }


}
