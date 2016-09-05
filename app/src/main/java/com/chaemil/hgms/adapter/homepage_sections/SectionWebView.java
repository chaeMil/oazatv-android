package com.chaemil.hgms.adapter.homepage_sections;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.utils.DimensUtils;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by chaemil on 5.9.16.
 */
public class SectionWebView extends StatelessSection {

    private final Context context;
    private final MainActivity mainActivity;
    private final int displayWidth;
    private final String url;

    public SectionWebView(Context context, MainActivity mainActivity, String url) {
        super(R.layout.homepage_section_header, R.layout.homepage_section_footer, R.layout.section_webview);
        this.context = context;
        this.mainActivity = mainActivity;
        this.displayWidth = DimensUtils.getDisplayHeight(mainActivity);
        this.url = url;
    }

    @Override
    public int getContentItemsTotal() {
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        headerHolder.sectionName.setText("WebView test");
        headerHolder.sectionIcon.setImageDrawable(context.getResources()
                .getDrawable(R.mipmap.ic_launcher));
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new WebViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        WebView webView = ((WebViewHolder) holder).webview;
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }

    private class WebViewHolder extends RecyclerView.ViewHolder {
        private final WebView webview;

        public WebViewHolder(View itemView) {
            super(itemView);
            this.webview = (WebView) itemView.findViewById(R.id.webview);
        }
    }
}
