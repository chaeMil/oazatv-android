package com.chaemil.hgms.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.model.Category;
import com.chaemil.hgms.model.Video;
import com.chaemil.hgms.utils.AdapterUtils;
import com.chaemil.hgms.view.VideoThumbImageView;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

/**
 * Created by chaemil on 20.4.16.
 */
public class CategoriesAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<Category> categories;
    private MainActivity mainActivity;

    public CategoriesAdapter(Context context, ArrayList<Category> categories, MainActivity mainActivity) {
        this.context = context;
        this.categories = categories;
        this.mainActivity = mainActivity;
    }

    @Override
    public int getGroupCount() {
        return categories.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return categories.get(groupPosition).getVideos().size();
    }

    @Override
    public Category getGroup(int groupPosition) {
        return categories.get(groupPosition);
    }

    @Override
    public Video getChild(int groupPosition, int childPosition) {
        return categories.get(groupPosition).getVideos().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Category category = getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.categories_group, null);
        }

        RelativeLayout background = (RelativeLayout) convertView.findViewById(R.id.background);
        TextView categoryNameView = (TextView) convertView.findViewById(R.id.category_name);
        ImageView indicator = (ImageView) convertView.findViewById(R.id.indicator);

        background.setBackgroundColor(Color.parseColor(category.getColor()));
        categoryNameView.setText(category.getName() + " (" + category.getVideos().size() + ")");
        if (isExpanded) {
            indicator.setImageDrawable(context.getResources().getDrawable(R.drawable.up_arrow));
        } else {
            indicator.setImageDrawable(context.getResources().getDrawable(R.drawable.down_arrow));
        }

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ChildViewHolder holder;
        final Video video = getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.archive_item, null);

            holder = new ChildViewHolder();
            holder.mainView = (RelativeLayout) convertView.findViewById(R.id.main_view);
            holder.thumb = (VideoThumbImageView) convertView.findViewById(R.id.thumb);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.views = (TextView) convertView.findViewById(R.id.views);
            holder.more = (ImageButton) convertView.findViewById(R.id.context_menu);

            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.playNewVideo(video);
            }
        });
        holder.name.setText(video.getName());
        holder.date.setText(video.getDate());
        holder.views.setText(video.getViews() + " " + context.getString(R.string.views));
        holder.more.setVisibility(View.VISIBLE);
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdapterUtils.contextDialog(context, mainActivity, CategoriesAdapter.this, video);
            }
        });
        Ion.with(context).load(video.getThumbFile()).intoImageView(holder.thumb);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public final class ChildViewHolder {
        public RelativeLayout mainView;
        public VideoThumbImageView thumb;
        public TextView name;
        public TextView date;
        public TextView views;
        public ImageButton more;
    }
}