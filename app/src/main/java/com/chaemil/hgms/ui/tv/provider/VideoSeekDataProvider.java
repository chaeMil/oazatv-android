package com.chaemil.hgms.ui.tv.provider;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v17.leanback.widget.PlaybackSeekDataProvider;

import com.chaemil.hgms.R;
import com.chaemil.hgms.utils.BitmapUtils;

/**
 * Created by Michal Mlejnek on 01/03/2018.
 */

public class VideoSeekDataProvider extends PlaybackSeekDataProvider {
    //TODO not really implemented, needs backend API first

    private final Context context;

    public VideoSeekDataProvider(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void getThumbnail(int index, ResultCallback callback) {
        Bitmap bitmap = BitmapUtils.drawableToBitmap(context.getDrawable(R.drawable.white_logo));
        callback.onThumbnailLoaded(bitmap, index);
    }

    @Override
    public long[] getSeekPositions() {
        return super.getSeekPositions();
    }

    @Override
    public void reset() {
        super.reset();
    }
}
