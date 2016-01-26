package com.chaemil.hgms.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.chaemil.hgms.R;
import com.chaemil.hgms.view.SquareImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class PhotosAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<String> filepaths;
    private int thumbWidth;

    public PhotosAdapter(Activity activity, int thumbWidth, ArrayList filepaths, ArrayList filenames) {
        this.activity = activity;
        this.filepaths = filepaths;
        this.thumbWidth = thumbWidth;
    }

    public int getCount() {
        return filepaths.size();
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

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        Picasso.with(activity.getApplicationContext())
                .load(new File(filepaths.get(position)))
                .resize(thumbWidth, thumbWidth)
                .centerCrop()
                .into(holder.image);

        return convertView;
    }

    static class ViewHolder {
        SquareImageView image;
    }
}