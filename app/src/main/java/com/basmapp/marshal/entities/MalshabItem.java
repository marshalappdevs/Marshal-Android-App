package com.basmapp.marshal.entities;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;

import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.localdb.annotations.Column;
import com.basmapp.marshal.localdb.annotations.PrimaryKey;
import com.basmapp.marshal.localdb.annotations.TableName;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@TableName(name = DBConstants.T_MALSHAB_ITEM)
public class MalshabItem extends DBObject {

    public MalshabItem(Context context) {
        super(context);
    }

    @Override
    protected boolean isPrimaryKeyAutoIncrement() {
        return false;
    }

    @Expose
    @SerializedName("_id")
    @PrimaryKey(columnName = DBConstants.COL_ID, isAutoIncrement = false)
    private String id;

    @Column(name = DBConstants.COL_URL)
    @Expose
    @SerializedName("url")
    private String url;

    @Column(name = DBConstants.COL_TITLE)
    @Expose
    @SerializedName("title")
    private String title;

    @Column(name = DBConstants.COL_IMAGE_URL)
    @Expose
    @SerializedName("imageUrl")
    private String imageUrl;

    @Column(name = DBConstants.COL_ORDER)
    @Expose
    @SerializedName("order")
    private int order;

    @Column(name = DBConstants.COL_IS_UP_TO_DATE)
    private boolean isUpToDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean getIsUpToDate() {
        return isUpToDate;
    }

    public void setIsUpToDate(boolean isUpToDate) {
        this.isUpToDate = isUpToDate;
    }
}
