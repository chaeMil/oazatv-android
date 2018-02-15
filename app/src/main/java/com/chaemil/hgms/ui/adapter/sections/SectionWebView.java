package com.chaemil.hgms.ui.adapter.sections;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chaemil.hgms.OazaApp;
import com.chaemil.hgms.R;
import com.chaemil.hgms.ui.activity.MainActivity;
import com.chaemil.hgms.ui.adapter.holder.HeaderViewHolder;
import com.chaemil.hgms.utils.DimensUtils;

/**
 * Created by chaemil on 5.9.16.
 */
public class SectionWebView extends BaseSection {

    private final Context context;
    private final int displayWidth;
    private final String url;

    public SectionWebView(Context context, String url) {
        super(R.layout.homepage_section_header,
                R.layout.homepage_section_footer,
                R.layout.section_webview);
        this.context = context;
        MainActivity mainActivity = ((OazaApp) context.getApplicationContext()).getMainActivity();
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
        headerHolder.sectionName.setText(context.getString(R.string.app_name));
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
