package com.basmach.marshal.entities;

import com.leocardz.link.preview.library.SourceContent;

public class MaterialItem {

    private String url;
    private String[] tags;
    private SourceContent sourceContent;
    private LinkContent linkContent;

    // Constructors
    public MaterialItem() {}

    public MaterialItem(String url) {
        this.url = url;
    }

    public MaterialItem(String url, String[] tags) {
        this.url = url;
        this.tags = tags;
    }

    // Getters and Setters
    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SourceContent getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(SourceContent sourceContent) {
        this.sourceContent = sourceContent;
    }

    public LinkContent getLinkContent() {
        return linkContent;
    }

    public void setLinkContent(LinkContent linkContent) {
        this.linkContent = linkContent;
    }
}
