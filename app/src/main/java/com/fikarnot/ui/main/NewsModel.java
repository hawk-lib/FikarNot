package com.fikarnot.ui.main;

class NewsModel {

    private String title;
    private String description;
    private String newsLink;

    public NewsModel(String title, String description, String newslink) {
        this.title = title;
        this.description = description;
        this.newsLink = newslink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNewslink() {
        return newsLink;
    }

    public void setNewsLink(String newslink) {
        this.newsLink = newslink;
    }
}
