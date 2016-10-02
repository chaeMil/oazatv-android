package com.chaemil.hgms.adapter.homepage_sections;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.utils.OSUtils;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by chaemil on 2.10.16.
 */

public class SectionAppVersion extends StatelessSection {

    private final Context context;
    private final MainActivity mainActivity;

    public SectionAppVersion(Context context, MainActivity mainActivity) {
        super(R.layout.section_app_version);
        this.context = context;
        this.mainActivity = mainActivity;
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
        headerHolder.sectionName.setText(context.getString(R.string.app_name));
        headerHolder.sectionIcon.setImageDrawable(context.getResources()
                .getDrawable(R.mipmap.ic_launcher));
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).upgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OSUtils.openPlayStore(context);
            }
        });
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        final Button upgradeButton;

        ViewHolder(View itemView) {
            super(itemView);
            this.upgradeButton = (Button) itemView.findViewById(R.id.upgrade_button);
        }
    }
}