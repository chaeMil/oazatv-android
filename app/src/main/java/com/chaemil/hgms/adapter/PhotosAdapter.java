package com.chaemil.hgms.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.chaemil.hgms.R;
import com.chaemil.hgms.model.Photo;
import com.chaemil.hgms.view.SquareImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PhotosAdapter extends BaseAdapter {
    private final ArrayList<Photo> photos;
    private Activity activity;
    private int thumbWidth;

    public PhotosAdapter(Activity activity, int thumbWidth, ArrayList<Photo> photos) {
        this.activity = activity;
        this.thumbWidth = thumbWidth;
        this.photos = photos;
    }

    public int getCount() {
        return photos.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.photo, parent, false);
            holder = new ViewHolder();
            holder.image = (SquareImageView) convertView.findViewById(R.id.image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Picasso.with(activity.getApplicationContext())
                .load(photos.get(position).getThumb512())
                .resize(thumbWidth, thumbWidth)
                .centerCrop()
                .into(holder.image);

        return convertView;
    }

    static class ViewHolder {
        SquareImageView image;
    }
}