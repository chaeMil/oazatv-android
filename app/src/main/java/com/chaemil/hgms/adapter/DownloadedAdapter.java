package com.chaemil.hgms.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.StringUtils;
import com.chaemil.hgms.view.VideoThumbImageView;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by chaemil on 8.1.16.
 */
public class DownloadedAdapter extends ArrayAdapter<Video> {
    private final Context context;
    private final MainActivity mainActivity;
    private ArrayList<Video> videos;

    public DownloadedAdapter(Context context, MainActivity mainActivity, ArrayList<Video> videos) {
        super(context, 0);
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
            holder.thumb = (VideoThumbImageView) convertView.findViewById(R.id.thumb);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.size = (TextView) convertView.findViewById(R.id.size);
            holder.more = (ImageButton) convertView.findViewById(R.id.context_menu);
            holder.downloadCover = (RelativeLayout) convertView.findViewById(R.id.download_cover);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Video video = videos.get(position);
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.playNewAudio(video);
            }
        });
        holder.name.setText(video.getName());
        holder.date.setText(video.getDate());
        holder.size.setText(StringUtils.getStringSizeLengthFile(Video.getDownloadedAudioSize(context, video)));
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contextDialog(video);
            }
        });

        File thumbFile = new File(String.valueOf(context.getExternalFilesDir(null)) + "/" + video.getHash() + ".jpg");

        Ion.with(context).load(thumbFile).intoImageView(holder.thumb);

        if (!video.isDownloaded() && video.isInDownloadQueue()) {
            holder.downloadCover.setVisibility(View.VISIBLE);
        } else {
            holder.downloadCover.setVisibility(View.GONE);
        }


        return convertView;
    }

    @Override
    public int getCount() {
        return videos.size();
    }

    public class ViewHolder {

        private RelativeLayout mainView;
        public VideoThumbImageView thumb;
        public TextView name;
        public TextView date;
        public TextView size;
        public RelativeLayout downloadCover;
        public ImageButton more;
    }

    private void contextDialog(final Video video) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String[] menu = new String[] {context.getString(R.string.delete_downloaded_audio)};

        builder.setItems(menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case 0:
                        createDeleteDialog(video, videos, DownloadedAdapter.this).show();
                        break;
                }
            }
        });


        builder.create().show();
    }


    private AlertDialog createDeleteDialog(final Video video, final ArrayList<Video> adapterData,
                                           final DownloadedAdapter adapter) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Video.deleteDownloadedAudio(getContext(), video);
                        dialog.dismiss();
                        adapterData.remove(adapterData.indexOf(video));
                        adapter.notifyDataSetChanged();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener);

        return builder.create();
    }
}
