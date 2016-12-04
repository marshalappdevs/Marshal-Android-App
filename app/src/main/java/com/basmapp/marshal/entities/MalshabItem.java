package com.basmapp.marshal.entities;

import android.content.Context;

import com.simplite.orm.DBObject;
import com.simplite.orm.annotations.Column;
import com.simplite.orm.annotations.Entity;
import com.simplite.orm.annotations.PrimaryKey;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = MalshabItem.TABLE_NAME)
public class MalshabItem extends DBObject {

    public static final String TABLE_NAME = "t_malshab_item";

    public static final String COL_ID = "id";
    public static final String COL_URL = "url";
    public static final String COL_TITLE = "title";
    public static final String COL_IMAGE_URL = "image_url";
    public static final String COL_ORDER = "item_order";
    public static final String COL_IS_UP_TO_DATE = "is_up_to_date";

    public MalshabItem(Context context) {
        super(context);
    }

    @Expose
    @SerializedName("_id")
    @PrimaryKey(columnName = COL_ID)
    private String id;

    @Column(name = COL_URL, options = {OPTION_UNIQUE})
    @Expose
    @SerializedName("url")
    private String url;

    @Column(name = COL_TITLE)
    @Expose
    @SerializedName("title")
    private String title;

    @Column(name = COL_IMAGE_URL)
    @Expose
    @SerializedName("imageUrl")
    private String imageUrl;

    @Column(name = COL_ORDER)
    @Expose
    @SerializedName("order")
    private int order;

    @Column(name = COL_IS_UP_TO_DATE)
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
