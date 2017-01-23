package com.chaemil.hgms.adapter;

/**
 * Created by chaemil on 30.6.16.
 */
import android.content.Context;
import android.os.AsyncTask;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Download;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.utils.FileUtils;
import com.chaemil.hgms.utils.StringUtils;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.ion.Ion;
import com.novoda.downloadmanager.lib.DownloadManager;

import java.util.List;

public class DownloadsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Download> downloads;
    private final Context context;

    public DownloadsAdapter(Context context, List<Download> downloads) {
        this.downloads = downloads;
        this.context = context;
    }

    public void updateDownloads(List<Download> beardDownloads) {
        this.downloads.clear();
        this.downloads.addAll(beardDownloads);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_HEADER) {
            View v = LayoutInflater.from (parent.getContext()).inflate (R.layout.header_item_download, parent, false);
            return new HeaderViewHolder (v);
        } else if(viewType == TYPE_ITEM) {
            View v = LayoutInflater.from (parent.getContext()).inflate (R.layout.list_item_download, parent, false);
            return new DownloadItemHolder (v);
        }
        return null;
    }

    @Override
    public int getItemViewType (int position) {
        if(isPositionHeader (position)) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionHeader (int position) {
        return position == 0;
    }

    @Override
    public int getItemCount () {
        return downloads.size () + 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        if (holder instanceof HeaderViewHolder) {

            StaggeredGridLayoutManager.LayoutParams layoutParams =
                    (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);

            HeaderViewHolder headerItemHolder = (HeaderViewHolder) holder;
            headerItemHolder.freeSpace.setText(FileUtils.readableAvailableExternalMemorySize() + " "
                    + context.getString(R.string.space_free));
            headerItemHolder.usedHeader.setText(FileUtils.readableAppSize(context));

            setupGraphs((HeaderViewHolder) holder);

        }


        if (holder instanceof DownloadItemHolder) {

            final Download download = downloads.get(position - 1);

            DownloadItemHolder downloadItemHolder = (DownloadItemHolder) holder;
            final Video video = Video.findByServerId((int) download.getVideoServerId());
            if (video != null) {
                downloadItemHolder.name.setText(video.getName());
                downloadItemHolder.date.setText(StringUtils.formatDate(video.getDate(), context));

                Ion.with(context)
                        .load(video.getThumbFile())
                        .intoImageView(downloadItemHolder.thumb);

                downloadItemHolder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (download.getDownloadStatusText() == DownloadManager.STATUS_SUCCESSFUL) {
                            MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
                            mainActivity.playNewAudio(video);
                        } else {
                            SuperToast.create(context,
                                    context.getString(R.string.not_downloaded_yet),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                downloadItemHolder.contextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contextDialog(video);
                    }
                });

                setupTime(downloadItemHolder, video);

                switch (download.getDownloadStatusText()) {
                    case DownloadManager.STATUS_PENDING:
                        downloadItemHolder.status.setText(context.getString(R.string.download_pending));
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        downloadItemHolder.status.setText(context.getString(R.string.download_running));
                        break;
                    case DownloadManager.STATUS_PAUSED:
                        downloadItemHolder.status.setText(context.getString(R.string.download_paused));
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        long fileSize = video.getDownloadedAudioSize(context);
                        String formattedSize = StringUtils.getStringSizeLengthFile(fileSize);
                        downloadItemHolder.status.setText(formattedSize);
                        break;
                }
            }
        }


    }

    private void setupGraphs(final HeaderViewHolder holder) {

        new AsyncTask<Void, Void, Void>() {

            public float oazaSpaceGraphPercent;
            public float otherAppsGraphPercent;

            @Override
            protected Void doInBackground( Void... voids ) {

                otherAppsGraphPercent = (100.0f / (float) FileUtils.getTotalExternalMemorySize())
                        * ((float) FileUtils.getTotalExternalMemorySize() - (float) FileUtils.getAvailableExternalMemorySize());

                oazaSpaceGraphPercent = (100.0f / (float) FileUtils.getTotalExternalMemorySize())
                        * (float) FileUtils.appSize(context);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                PercentRelativeLayout.LayoutParams otherAppsParams = (PercentRelativeLayout.LayoutParams)
                        holder.otherAppsGraph.getLayoutParams();
                PercentLayoutHelper.PercentLayoutInfo otherAppsGraphInfo = otherAppsParams.getPercentLayoutInfo();
                otherAppsGraphInfo.widthPercent = otherAppsGraphPercent / 100.0f;
                holder.otherAppsGraph.requestLayout();


                PercentRelativeLayout.LayoutParams oazaSpaceParams = (PercentRelativeLayout.LayoutParams)
                        holder.oazaSpaceGraph.getLayoutParams();
                PercentLayoutHelper.PercentLayoutInfo oazaSpaceGraphInfo = oazaSpaceParams.getPercentLayoutInfo();
                oazaSpaceGraphInfo.leftMarginPercent = otherAppsGraphPercent / 100.f;
                oazaSpaceGraphInfo.widthPercent = oazaSpaceGraphPercent / 100.0f;
                holder.oazaSpaceGraph.requestLayout();

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView usedHeader;
        private final TextView freeSpace;
        private final View otherAppsGraph;
        private final View oazaSpaceGraph;

        public HeaderViewHolder (View itemView) {
            super (itemView);
            usedHeader = (TextView) itemView.findViewById(R.id.oaza_app_size);
            freeSpace = (TextView) itemView.findViewById(R.id.free_space);
            otherAppsGraph = itemView.findViewById(R.id.other_apps_graph);
            oazaSpaceGraph = itemView.findViewById(R.id.oaza_space_graph);
        }
    }

    private void getSpace() {
        Log.d("externalMemory", FileUtils.readableTotalExternalMemorySize());
        Log.d("externalMemoryFree", FileUtils.readableAvailableExternalMemorySize());
        Log.d("appSize", FileUtils.readableFolderSize(context.getExternalFilesDir("")));
    }

    private void setupTime(final DownloadItemHolder holder, final Video video) {
        holder.viewProgress.setVisibility(View.GONE);
        holder.time.setVisibility(View.GONE);

        new AsyncTask<Void, Void, Void>() {

            public Video savedVideo;

            @Override
            protected Void doInBackground( Void... voids ) {
                savedVideo = Video.findByServerId(video.getServerId());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                holder.viewProgress.setVisibility(View.VISIBLE);
                if (savedVideo != null && savedVideo.equals(video)) {
                    holder.viewProgress.setMax(video.getDuration());
                    holder.viewProgress.setProgress(savedVideo.getCurrentTime() / 1000);
                } else {
                    holder.viewProgress.setMax(100);
                    holder.viewProgress.setProgress(0);
                }

                holder.time.setVisibility(View.VISIBLE);
                holder.time.setText(StringUtils.getDurationString(video.getDuration()));
            }
        }.execute();
    }

    private class DownloadItemHolder extends RecyclerView.ViewHolder {

        private final View root;
        private final TextView name;
        private final TextView date;
        private final TextView status;
        private final ImageView thumb;
        private final ImageView contextButton;
        public final ProgressBar viewProgress;
        public final TextView time;

        public DownloadItemHolder(View itemView) {
            super(itemView);
            root = itemView;
            name = (TextView) itemView.findViewById(R.id.name);
            date = (TextView) itemView.findViewById(R.id.date);
            status = (TextView) itemView.findViewById(R.id.status);
            thumb = (ImageView) itemView.findViewById(R.id.thumb);
            contextButton = (ImageView) itemView.findViewById(R.id.context_menu);
            viewProgress = (ProgressBar) itemView.findViewById(R.id.view_progress);
            time = (TextView) itemView.findViewById(R.id.video_time);
        }
    }

    private void contextDialog(final Video video) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);

        String[] menu = new String[] {context.getString(R.string.delete_downloaded_audio).toUpperCase()};

        builder.title(video.getName())
                .items(menu)
                .theme(Theme.LIGHT)
                .itemsColor(context.getResources().getColor(R.color.colorPrimary))
                .itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (which) {
                    case 0:
                        AdapterUtils.createDeleteDialog(context, video).show();
                        break;
                }
            }
        });


        builder.show();
    }

}