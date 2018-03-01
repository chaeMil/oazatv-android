package com.chaemil.hgms.ui.tv.model;

/**
 * Created by Michal Mlejnek on 01/03/2018.
 */

public class ExternalLinkItem extends MenuItem {
    private String link;
    public ExternalLinkItem(String title, String color, String link) {
        super(title, color);
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
