package com.chaemil.hgms.ui.tv.model;

import android.support.v17.leanback.widget.ArrayObjectAdapter;

/**
 * Created by Michal Mlejnek on 22/02/2018.
 */

public class MovieRow {

    private int page;
    private int id;
    private ArrayObjectAdapter adapter;
    private String title;

    public MovieRow() {
    }

    public int getPage() {
        return page;
    }

    public MovieRow setPage(int page) {
        this.page = page;
        return this;
    }

    public int getId() {
        return id;
    }

    public MovieRow setId(int id) {
        this.id = id;
        return this;
    }

    public ArrayObjectAdapter getAdapter() {
        return adapter;
    }

    public MovieRow setAdapter(ArrayObjectAdapter adapter) {
        this.adapter = adapter;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MovieRow setTitle(String title) {
        this.title = title;
        return this;
    }
}
