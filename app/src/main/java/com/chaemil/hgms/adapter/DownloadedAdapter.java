package com.chaemil.hgms.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.SmartLog;
import com.chaemil.hgms.utils.StringUtils;
import com.chaemil.hgms.view.VideoThumbImageView;
import com.koushikdutta.ion.Ion;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chaemil on 8.1.16.
 */
public class DownloadedAdapter extends ArrayAdapter<Video> {
    private static final long TIMER_DELAY = 5 * 1000;
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
        final ViewHolder holder;

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
            holder.progress = (CircularProgressBar) convertView.findViewById(R.id.progress);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Video video = videos.get(position);
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (video.isDownloaded()) {
                    mainActivity.playNewAudio(video);
                }
            }
        });
        holder.name.setText(video.getName());
        holder.date.setText(video.getDate());
        holder.size.setText(StringUtils.getStringSizeLengthFile(Video.getDownloadedAudioSize(context, video)));
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (video.isDownloaded()) {
                    contextDialog(video);
                }
            }
        });

        File thumbFile = new File(String.valueOf(context.getExternalFilesDir(null)) + "/" + video.getHash() + ".jpg");

        Ion.with(context).load(thumbFile).intoImageView(holder.thumb);

        SmartLog.Log(SmartLog.LogLevel.DEBUG, "downloadStatus",
                String.valueOf(Video.getDownloadStatus(((OazaApp) context.getApplicationContext()), video.getServerId())));

        switch (Video.getDownloadStatus(((OazaApp) context.getApplicationContext()), video.getServerId())) {
            case Video.CURRENTLY_DOWNLOADING:
                holder.progress.setVisibility(View.VISIBLE);
                holder.thumb.setAlpha(0.5f);
                holder.timer = new Timer();
                holder.timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        updatePercent(holder);
                    }
                }, 0, TIMER_DELAY);
                break;
            case Video.IN_DOWNLOAD_QUEUE:
                holder.progress.setVisibility(View.GONE);
                holder.thumb.setAlpha(0.5f);
                if (holder.timer != null) {
                    holder.timer.cancel();
                }
                holder.timer = null;
                break;
            case Video.DOWNLOADED:
                holder.progress.setVisibility(View.GONE);
                holder.thumb.setAlpha(1.0f);
                if (holder.timer != null) {
                    holder.timer.cancel();
                }
                holder.timer = null;
                break;
        }


        return convertView;
    }

    private void updatePercent(final ViewHolder holder) {
        if (((OazaApp) context.getApplicationContext()).getDownloadService() != null) {
            final long percent = ((OazaApp) context.getApplicationContext()).getDownloadService().getCurrentDownloadProgress();
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    holder.progress.setProgress(percent);
                }
            });
        }
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
        public ImageButton more;
        public CircularProgressBar progress;
        public Timer timer;
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
