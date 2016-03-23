package com.basmach.marshal.entities;

import android.content.Context;

import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.DBObject;
import com.basmach.marshal.localdb.annotations.Column;
import com.basmach.marshal.localdb.annotations.ColumnGetter;
import com.basmach.marshal.localdb.annotations.ColumnSetter;
import com.basmach.marshal.localdb.annotations.TableName;
import com.basmach.marshal.ui.utils.MaterialsRecyclerAdapter;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;

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
    private boolean isGetLinkDataExecute;

    // Constructors
    public MaterialItem(Context context) {
        super(context);
    }

    // Getters and Setters


    public boolean isGetLinkDataExecute() {
        return isGetLinkDataExecute;
    }

    public void setGetLinkDataExecute(boolean getLinkDataExecute) {
        isGetLinkDataExecute = getLinkDataExecute;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    @ColumnGetter(columnName = DBConstants.COL_URL)
    public String getUrl() {
        return url;
    }

    @ColumnSetter(columnName = DBConstants.COL_URL, type = TYPE_STRING)
    public void setUrl(String url) {
        this.url = url;
    }

    public SourceContent getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(SourceContent sourceContent) {
        this.sourceContent = sourceContent;
    }

    @ColumnGetter(columnName = DBConstants.COL_TITLE)
    public String getTitle() {
        return title;
    }

    @ColumnSetter(columnName = DBConstants.COL_TITLE, type = TYPE_STRING)
    public void setTitle(String title) {
        this.title = title;
    }

    @ColumnGetter(columnName = DBConstants.COL_DESCRIPTION)
    public String getDescription() {
        return description;
    }

    @ColumnSetter(columnName = DBConstants.COL_DESCRIPTION, type = TYPE_STRING)
    public void setDescription(String description) {
        this.description = description;
    }

    @ColumnGetter(columnName = DBConstants.COL_CANNONICIAL_URL)
    public String getCannonicalUrl() {
        return cannonicalUrl;
    }

    @ColumnSetter(columnName = DBConstants.COL_CANNONICIAL_URL, type = TYPE_STRING)
    public void setCannonicalUrl(String cannonicalUrl) {
        this.cannonicalUrl = cannonicalUrl;
    }

    @ColumnGetter(columnName = DBConstants.COL_IMAGE_URL)
    public String getImageUrl() {
        return imageUrl;
    }

    @ColumnSetter(columnName = DBConstants.COL_IMAGE_URL, type = TYPE_STRING)
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    ///////////////////////////////////////////

    public void getLinkData(TextCrawler textCrawler, MaterialsRecyclerAdapter adapter) {

        isGetLinkDataExecute = true;
        textCrawler.makePreview(new LinkPreviewCallback() {
            @Override
            public void onPre() {

            }

            @Override
            public void onPos(SourceContent sourceContent, boolean b) {
                if (sourceContent != null) {
                    setSourceContent(sourceContent);
                    setTitle(sourceContent.getTitle());
                    setDescription(sourceContent.getDescription());
                    setCannonicalUrl(sourceContent.getCannonicalUrl());
                    if (sourceContent.getImages().size() > 0) {
                        setImageUrl(sourceContent.getImages().get(0));
                    }
                }
            }
        }, getUrl());
    }
}
