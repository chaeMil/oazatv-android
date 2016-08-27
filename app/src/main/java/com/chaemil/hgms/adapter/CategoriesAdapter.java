package com.chaemil.hgms.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chaemil.hgms.R;
import com.chaemil.hgms.activity.MainActivity;
import com.chaemil.hgms.fragment.CategoryFragment;
import com.chaemil.hgms.model.Category;

import java.util.ArrayList;

/**
 * Created by chaemil on 20.4.16.
 */
public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.Holder> {

    private Context context;
    private ArrayList<Category> categories;
    private MainActivity mainActivity;

    public CategoriesAdapter(Context context, ArrayList<Category> categories, MainActivity mainActivity) {
        this.context = context;
        this.categories = categories;
        this.mainActivity = mainActivity;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories_group, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        final Category category = categories.get(position);
        holder.background.setBackgroundColor(Color.parseColor(category.getColor()));
        holder.background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategoryFragment categoryFragment = new CategoryFragment();
                Bundle args = new Bundle();
                args.putParcelable(CategoryFragment.CATEGORY, category);
                categoryFragment.setArguments(args);

                FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.category_fragment, categoryFragment);
                transaction.addToBackStack(CategoryFragment.TAG);
                transaction.commit();
            }
        });
        holder.name.setText(category.getName());
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final CardView background;

        public Holder(View itemView) {
            super(itemView);
            this.background = (CardView) itemView.findViewById(R.id.background);
            this.name = (TextView) itemView.findViewById(R.id.category_name);
        }
    }
}