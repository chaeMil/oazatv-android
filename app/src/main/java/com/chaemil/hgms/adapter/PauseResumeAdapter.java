package com.chaemil.hgms.adapter;

/**
 * Created by chaemil on 30.6.16.
 */
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.model.Download;

import java.util.List;
import java.util.Locale;

public class PauseResumeAdapter extends RecyclerView.Adapter<PauseResumeAdapter.ViewHolder> {
    private final List<Download> downloads;
    private final Listener listener;

    public PauseResumeAdapter(List<Download> downloads, Listener listener) {
        this.downloads = downloads;
        this.listener = listener;
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
        viewHolder.titleTextView.setText(String.valueOf(download.getVideoId()));
        String text = String.format(Locale.getDefault(),
                "%1$s : %2$s\nBatch %3$d",
                download.getDownloadStatusText(),
                download.getVideoId(),
                download.getBatchId());
        viewHolder.locationTextView.setText(text);
        viewHolder.root.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        listener.onItemClick(download);
                    }
                }
        );
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
        private final TextView titleTextView;
        private final TextView locationTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            titleTextView = (TextView) itemView.findViewById(R.id.download_title_text);
            locationTextView = (TextView) itemView.findViewById(R.id.download_location_text);
        }
    }
}