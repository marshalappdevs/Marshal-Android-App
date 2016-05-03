package com.basmach.marshal.entities;

import android.content.Context;

import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.DBObject;
import com.basmach.marshal.localdb.annotations.Column;
import com.basmach.marshal.localdb.annotations.ColumnGetter;
import com.basmach.marshal.localdb.annotations.ColumnSetter;
import com.basmach.marshal.localdb.annotations.PrimaryKey;
import com.basmach.marshal.localdb.annotations.PrimaryKeySetter;
import com.basmach.marshal.localdb.annotations.TableName;
import com.basmach.marshal.ui.adapters.MaterialsRecyclerAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;

@TableName(name = DBConstants.T_MATERIAL_ITEM)
public class MaterialItem extends DBObject{

    @PrimaryKey(columnName = DBConstants.COL_ID)
    private long id;

    @Expose
    @SerializedName("url")
    @Column(name = DBConstants.COL_URL)
    private String url;

    @Expose
    @SerializedName("hashTags")
    @Column(name = DBConstants.COL_TAGS)
    private String tags;

    @Expose
    @SerializedName("title")
    @Column(name = DBConstants.COL_TITLE)
    private String title;

    @Expose
    @SerializedName("description")
    @Column(name = DBConstants.COL_DESCRIPTION)
    private String description;

    @Expose
    @SerializedName("mainUrl")
    @Column(name = DBConstants.COL_CANNONICIAL_URL)
    private String cannonicalUrl;

    @Expose
    @SerializedName("imageUrl")
    @Column(name = DBConstants.COL_IMAGE_URL)
    private String imageUrl;

    private SourceContent sourceContent;

    @Column(name = DBConstants.COL_IS_GET_LINK_DATA_EXECUTED)
    private boolean isGetLinkDataExecuted = true;

    // Constructors
    public MaterialItem(Context context) {
        super(context);
    }

    // Getters and Setters
    @ColumnGetter(columnName = DBConstants.COL_ID)
    public long getId() {
        return id;
    }

    @PrimaryKeySetter
    @ColumnSetter(columnName = DBConstants.COL_ID, type = TYPE_LONG)
    public void setId(long id) {
        this.id = id;
    }

    @ColumnGetter(columnName = DBConstants.COL_IS_GET_LINK_DATA_EXECUTED)
    public boolean getIsGetLinkDataExecute() {
        return this.isGetLinkDataExecuted;
    }

    @ColumnSetter(columnName = DBConstants.COL_IS_GET_LINK_DATA_EXECUTED, type = TYPE_BOOLEAN)
    public void setGetLinkDataExecuted(boolean isGetLinkDataExecuted) {
        this.isGetLinkDataExecuted = isGetLinkDataExecuted;
    }

    @ColumnGetter(columnName = DBConstants.COL_TAGS)
    public String getTags() {
        return tags;
    }

    @ColumnSetter(columnName = DBConstants.COL_TAGS, type = TYPE_STRING)
    public void setTags(String tags) {
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

    public void getLinkData(TextCrawler textCrawler, LinkPreviewCallback callback) {

        textCrawler.makePreview(callback, getUrl(), 1);
    }

    public void getLinkData(int position, TextCrawler textCrawler, LinkPreviewCallback callback) {

        textCrawler.makePreview(callback, getUrl(), 1);
    }
}
