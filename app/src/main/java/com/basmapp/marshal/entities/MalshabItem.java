package com.basmapp.marshal.entities;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;

import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.localdb.annotations.Column;
import com.basmapp.marshal.localdb.annotations.ColumnGetter;
import com.basmapp.marshal.localdb.annotations.ColumnSetter;
import com.basmapp.marshal.localdb.annotations.PrimaryKey;
import com.basmapp.marshal.localdb.annotations.PrimaryKeySetter;
import com.basmapp.marshal.localdb.annotations.TableName;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@TableName(name = DBConstants.T_MALSHAB_ITEM)
public class MalshabItem extends DBObject{

    public MalshabItem(Context context) {
        super(context);
    }

    @PrimaryKey(columnName = DBConstants.COL_ID)
    private long id;

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

    @ColumnGetter(columnName = DBConstants.COL_ID)
    public long getId() {
        return id;
    }

    @PrimaryKeySetter
    @ColumnSetter(columnName = DBConstants.COL_ID, type = TYPE_LONG)
    public void setId(long id) {
        this.id = id;
    }

    @ColumnGetter(columnName = DBConstants.COL_URL)
    public String getUrl() {
        return url;
    }

    @ColumnSetter(columnName = DBConstants.COL_URL, type = TYPE_STRING)
    public void setUrl(String url) {
        this.url = url;
    }

    @ColumnGetter(columnName = DBConstants.COL_TITLE)
    public String getTitle() {
        return title;
    }

    @ColumnSetter(columnName = DBConstants.COL_TITLE, type = TYPE_STRING)
    public void setTitle(String title) {
        this.title = title;
    }

    @ColumnGetter(columnName = DBConstants.COL_IMAGE_URL)
    public String getImageUrl() {
        return imageUrl;
    }

    @ColumnSetter(columnName = DBConstants.COL_IMAGE_URL, type = TYPE_STRING)
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public SQLiteStatement getStatement(SQLiteStatement statement, long objectId) throws Exception {
        if (getUrl() != null && !getUrl().equals("") && getTitle() != null && !getTitle().equals("")) {
            statement.clearBindings();
            statement.bindLong(1, objectId);
            statement.bindString(2, getUrl());
            statement.bindString(3, getTitle());

            if (imageUrl == null)
                imageUrl = "";
            statement.bindString(4, getImageUrl());

            return statement;
        } else {
            return null;
        }
    }
}