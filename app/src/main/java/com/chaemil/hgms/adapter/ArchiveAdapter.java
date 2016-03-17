package com.chaemil.hgms.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.BaseActivity;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.ArchiveItem;
import com.chaemil.hgms.model.PhotoAlbum;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.service.DownloadService;
import com.chaemil.hgms.view.VideoThumbImageView;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

/**
 * Created by chaemil on 20.12.15.
 */
public class ArchiveAdapter extends ArrayAdapter<ArchiveItem> {

    private final Context context;
    private final MainActivity mainActivity;
    private ArrayList<ArchiveItem> archive;

    public ArchiveAdapter(Context context, MainActivity mainActivity, ArrayList<ArchiveItem> archive) {
        super(context, 0);
        this.context = context;
        this.archive = archive;
        this.mainActivity = mainActivity;
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

        ArchiveItem archiveItem = archive.get(position);

        switch (archiveItem.getType()) {
            case ArchiveItem.Type.VIDEO:

                final Video video = archiveItem.getVideo();

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
                holder.more.setVisibility(View.VISIBLE);
                holder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contextDialog(video);
                    }
                });
                Ion.with(context).load(video.getThumbFile()).intoImageView(holder.thumb);

                break;

            case ArchiveItem.Type.ALBUM:

                final PhotoAlbum photoAlbum = archiveItem.getAlbum();

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
                break;
        }

        return convertView;
    }

    private void contextDialog(final Video video) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String[] menu;

        if (Video.getDownloadStatus(((OazaApp) context.getApplicationContext()), video.getServerId()) == Video.NOT_DOWNLOADED) {
            menu = new String[] {context.getString(R.string.download_audio),
                                context.getString(R.string.stream_audio)};

            builder.setItems(menu, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0:
                            downloadAudio(video);
                            dialog.dismiss();
                            break;
                        case 1:
                            mainActivity.playNewAudio(video);
                            dialog.dismiss();
                            break;
                    }
                }
            });

        }

        if (Video.getDownloadStatus(((OazaApp) context.getApplicationContext()), video.getServerId()) == Video.DOWNLOADED) {
            menu = new String[] {context.getString(R.string.play_downloaded_audio)};

            builder.setItems(menu, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0:
                            mainActivity.playNewAudio(video);
                            dialog.dismiss();
                            break;
                    }
                }
            });

        }


        builder.create().show();
    }

    private void downloadAudio(Video video) {
        ((OazaApp) mainActivity.getApplication()).addToDownloadQueue(video);
        mainActivity.startDownloadService();
        notifyDataSetChanged();
        mainActivity.getMainFragment().getDownloadedFragment().notifyDatasetChanged();
        SuperToast.create(context, context.getString(R.string.added_to_download_queue), SuperToast.Duration.MEDIUM).show();
    }

    private void openAlbum(PhotoAlbum album) {
        mainActivity.getMainFragment().openAlbum(album);
    }

    @Override
    public int getCount() {
        return archive.size();
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
