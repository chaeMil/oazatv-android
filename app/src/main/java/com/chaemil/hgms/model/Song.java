package com.chaemil.hgms.model;

import com.google.gson.annotations.Expose;

/**
 * Created by chaemil on 1.11.16.
 */
public class Song {

    @Expose
    private int id;
    @Expose
    private String tag;
    @Expose
    private String name;
    @Expose
    private String author;
    @Expose
    private String body;

    public Song() {
    }

    public Song(int id, String tag, String name, String author, String body) {
        this.id = id;
        this.tag = tag;
        this.name = name;
        this.author = author;
        this.body = body;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
