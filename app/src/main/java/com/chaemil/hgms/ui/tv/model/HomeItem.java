package com.chaemil.hgms.ui.tv.model;

/**
 * Created by Michal Mlejnek on 01/03/2018.
 */

public class HomeItem {
    protected String title;
    protected String color;

    public HomeItem(String title, String color) {
        this.title = title;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
