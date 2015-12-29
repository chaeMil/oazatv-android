package com.chaemil.hgms.utils;

import android.support.v7.widget.RecyclerView;

/**
 * Created by chaemil on 29.12.15.
 */
public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {
    private int toolbarOffset = 0;
    private int toolbarHeight;

    public HidingScrollListener(int toolbarHeight) {
        this.toolbarHeight = toolbarHeight;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        clipToolbarOffset();
        onMoved(toolbarOffset);

        if((toolbarOffset < toolbarHeight && dy>0) || (toolbarOffset >0 && dy<0)) {
            toolbarOffset += dy;
        }
    }

    private void clipToolbarOffset() {
        if(toolbarOffset > toolbarHeight) {
            toolbarOffset = toolbarHeight;
        } else if(toolbarOffset < 0) {
            toolbarOffset = 0;
        }
    }

    public abstract void onHide();
    public abstract void onShow();
    public abstract void onMoved(int distance);
}