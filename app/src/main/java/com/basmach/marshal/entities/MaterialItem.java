package com.basmach.marshal.entities;

import android.content.Context;

import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.DBObject;
import com.basmach.marshal.localdb.annotations.Column;
import com.basmach.marshal.localdb.annotations.TableName;
import com.leocardz.link.preview.library.SourceContent;

@TableName(name = DBConstants.T_MATERIAL_ITEM)
public class MaterialItem extends DBObject{

    @Column(name = DBConstants.COL_URL)
    private String url;

    private String[] tags;

    @Column(name = DBConstants.COL_TITLE)
    private String title;

    @Column(name = DBConstants.COL_DESCRIPTION)
    private String description;

    @Column(name = DBConstants.COL_CANNONICIAL_URL)
    private String cannonicalUrl;

    @Column(name = DBConstants.COL_IMAGE_URL)
    private String imageUrl;

    private SourceContent sourceContent;
//    private LinkContent linkContent;

    // Constructors
    public MaterialItem(Context context) {
        super(context);
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

    public String getCannonicalUrl() {
        return cannonicalUrl;
    }

    public void setCannonicalUrl(String cannonicalUrl) {
        this.cannonicalUrl = cannonicalUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    //    public LinkContent getLinkContent() {
//        return linkContent;
//    }
//
//    public void setLinkContent(LinkContent linkContent) {
//        this.linkContent = linkContent;
//    }
}
