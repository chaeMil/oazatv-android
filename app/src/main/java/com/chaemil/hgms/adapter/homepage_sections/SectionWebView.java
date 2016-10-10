package com.chaemil.hgms.adapter.homepage_sections;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.utils.DimensUtils;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by chaemil on 5.9.16.
 */
public class SectionWebView extends BaseSection {

    private final Context context;
    private final MainActivity mainActivity;
    private final int displayWidth;
    private final String url;

    public SectionWebView(Context context, MainActivity mainActivity, String url) {
        super(R.layout.homepage_section_header,
                R.layout.homepage_section_footer,
                R.layout.section_webview);
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
        super.onBindHeaderViewHolder(holder);

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
        setFullSpan(holder);

        WebView webView = ((WebViewHolder) holder).webview;
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (view.getUrl().contains("openInBrowser")) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(view.getUrl())));
                    return true;
                } else {
                    return false;
                }
            }
        });
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
