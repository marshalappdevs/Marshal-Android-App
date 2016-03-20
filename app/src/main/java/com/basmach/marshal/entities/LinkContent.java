package com.basmach.marshal.entities;

/**
 * Created by Ido on 3/19/2016.
 */
public class LinkContent {

    private String title = null;
    private String description = null;
    private String siteName = null;
    private String imageUrl = null;

    public LinkContent() {}

    public LinkContent(String title, String description, String siteName, String imageUrl) {
        this.title = title;
        this.description = description;
        this.siteName = siteName;
        this.imageUrl = imageUrl;
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

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
