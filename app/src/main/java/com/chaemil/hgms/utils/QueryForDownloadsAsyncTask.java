package com.chaemil.hgms.utils;

/**
 * Created by chaemil on 30.6.16.
 */

import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.chaemil.hgms.model.Download;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class QueryForDownloadsAsyncTask extends AsyncTask<Query, Void, List<Download>> {

    private final DownloadManager downloadManager;
    private final WeakReference<Callback> weakCallback;

    public static QueryForDownloadsAsyncTask newInstance(DownloadManager downloadManager, Callback callback) {
        return new QueryForDownloadsAsyncTask(downloadManager, new WeakReference<>(callback));
    }

    QueryForDownloadsAsyncTask(DownloadManager downloadManager, WeakReference<Callback> weakCallback) {
        this.downloadManager = downloadManager;
        this.weakCallback = weakCallback;
    }

    @Override
    protected List<Download> doInBackground(@NonNull Query... params) {
        Cursor cursor = downloadManager.query(params[0]);
        List<Download> download = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                long videoId = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_EXTRA_DATA));
                int downloadStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                long batchId = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BATCH_ID));
                download.add(new Download(videoId, downloadStatus, batchId));
            }
        } finally {
            cursor.close();
        }
        return download;
    }

    @Override
    protected void onPostExecute(@NonNull List<Download> download) {
        super.onPostExecute(download);
        Callback callback = weakCallback.get();
        if (callback == null) {
            return;
        }
        callback.onQueryResult(download);
    }

    public interface Callback {
        void onQueryResult(List<Download> downloads);
    }
}