package com.chaemil.hgms.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.SmartLog;
import com.chaemil.hgms.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by chaemil on 8.1.16.
 */
public class DownloadedAdapter extends ArrayAdapter<Video> {
    private final Context context;
    private final MainActivity mainActivity;
    private ArrayList<Video> videos;

    public DownloadedAdapter(Context context, int resource, MainActivity mainActivity, ArrayList<Video> videos) {
        super(context, resource);
        this.context = context;
        this.videos = videos;
        this.mainActivity = mainActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.downloaded_item, null);

            holder = new ViewHolder();
            holder.mainView = (RelativeLayout) convertView.findViewById(R.id.main_view);
            holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.size = (TextView) convertView.findViewById(R.id.size);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Video video = videos.get(position);
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        holder.name.setText(video.getName());
        holder.date.setText(video.getDate());
        holder.size.setText(StringUtils.getStringSizeLengthFile(Video.getDownloadedAudioSize(context, video)));

        File thumbFile = new File(String.valueOf(context.getExternalFilesDir(null)) + "/" + video.getHash() + ".jpg");

        Picasso.with(context).load(thumbFile).into(holder.thumb);


        return convertView;
    }

    @Override
    public int getCount() {
        return videos.size();
    }

    public class ViewHolder {

        private RelativeLayout mainView;
        public ImageView thumb;
        public TextView name;
        public TextView date;
        public TextView size;

    }
}
