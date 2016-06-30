package com.chaemil.hgms.adapter;

/**
 * Created by chaemil on 30.6.16.
 */
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Download;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.SmartLog;
import com.chaemil.hgms.utils.StringUtils;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.ion.Ion;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Query;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.ViewHolder> {
    private final List<Download> downloads;
    private final Listener listener;
    private final Context context;
    private final MainActivity mainActivity;

    public DownloadsAdapter(Context context, MainActivity mainActivity, List<Download> downloads,
                            Listener listener) {
        this.downloads = downloads;
        this.mainActivity = mainActivity;
        this.listener = listener;
        this.context = context;
    }

    public void updateDownloads(List<Download> beardDownloads) {
        this.downloads.clear();
        this.downloads.addAll(beardDownloads);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.list_item_download, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Download download = downloads.get(position);

        final Video video = Video.findByServerId((int) download.getVideoServerId());
        if (video != null) {
            viewHolder.name.setText(video.getName());
            viewHolder.date.setText(StringUtils.formatDate(video.getDate(), context));

            switch (download.getDownloadStatusText()) {
                case DownloadManager.STATUS_PENDING:
                    viewHolder.status.setText(context.getString(R.string.download_pending));
                    viewHolder.pauseButton.setVisibility(View.VISIBLE);
                    viewHolder.pauseButton.setImageDrawable(context.getResources()
                            .getDrawable(R.drawable.pause));
                    break;
                case DownloadManager.STATUS_RUNNING:
                    viewHolder.status.setText(context.getString(R.string.download_running));
                    viewHolder.pauseButton.setVisibility(View.VISIBLE);
                    viewHolder.pauseButton.setImageDrawable(context.getResources()
                            .getDrawable(R.drawable.pause));
                    break;
                case DownloadManager.STATUS_PAUSED:
                    viewHolder.status.setText(context.getString(R.string.download_paused));
                    viewHolder.pauseButton.setVisibility(View.VISIBLE);
                    viewHolder.pauseButton.setImageDrawable(context.getResources()
                            .getDrawable(R.drawable.ic_continue_download));
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    long fileSize = video.getDownloadedAudioSize(context);
                    String formattedSize = StringUtils.getStringSizeLengthFile(fileSize);
                    viewHolder.status.setText(formattedSize);
                    viewHolder.pauseButton.setVisibility(View.GONE);
                    break;
            }


            Ion.with(context).load(video.getThumbFile()).intoImageView(viewHolder.thumb);
        }

        viewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (download.getDownloadStatusText() == DownloadManager.STATUS_SUCCESSFUL) {
                    mainActivity.playNewAudio(video, true);
                } else {
                    SuperToast.create(context,
                            context.getString(R.string.not_downloaded_yet),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewHolder.pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(download);
            }
        });

        viewHolder.contextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contextDialog(video);
            }
        });
    }

    @Override
    public int getItemCount() {
        return downloads.size();
    }

    public interface Listener {
        void onItemClick(Download download);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final View root;
        private final TextView name;
        private final TextView date;
        private final TextView status;
        private final ImageView thumb;
        private final ImageView pauseButton;
        private final ImageView contextButton;
        private final ImageView cancelButton;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            name = (TextView) itemView.findViewById(R.id.name);
            date = (TextView) itemView.findViewById(R.id.date);
            status = (TextView) itemView.findViewById(R.id.status);
            thumb = (ImageView) itemView.findViewById(R.id.thumb);
            pauseButton = (ImageView) itemView.findViewById(R.id.pause);
            contextButton = (ImageView) itemView.findViewById(R.id.context_menu);
            cancelButton = (ImageView) itemView.findViewById(R.id.cancel_download);
        }
    }

    private void contextDialog(final Video video) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String[] menu = new String[] {context.getString(R.string.delete_downloaded_audio)};

        builder.setItems(menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case 0:
                        createDeleteDialog(video).show();
                        break;
                }
            }
        });


        builder.create().show();
    }

    private AlertDialog createDeleteDialog(final Video video) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:

                        DownloadManager downloadManager = DownloadManagerBuilder.from(context).build();
                        Cursor cursor = downloadManager.query(new Query().setFilterByExtraData(String.valueOf(video.getServerId())));
                        ArrayList<Integer> idsToDelete = new ArrayList<>();
                        try {
                            while (cursor.moveToNext()) {
                                long videoId = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_EXTRA_DATA));
                                if (videoId == video.getServerId()) {
                                    idsToDelete.add(cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BATCH_ID)));
                                }
                            }
                        } finally {
                            cursor.close();
                            for (Integer integer : idsToDelete) {
                                downloadManager.removeBatches(integer);
                            }
                        }

                        AdapterUtils.deleteAudio(context, mainActivity, video, dialog);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.are_you_shure))
                .setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                .setNegativeButton(context.getString(R.string.no),
                        dialogClickListener);

        return builder.create();
    }

}