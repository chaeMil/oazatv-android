package com.chaemil.hgms.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class VideoThumbImageView extends ImageView {

    public VideoThumbImageView(Context context) {
        super(context);
    }

    public VideoThumbImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoThumbImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, (int) (width * 0.5625));
    }
}
