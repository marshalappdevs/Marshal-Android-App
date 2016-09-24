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

import java.sql.PreparedStatement;

@TableName(name = DBConstants.T_MALSHAB_ITEM)
public class MalshabItem extends DBObject {

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

    @Column(name = DBConstants.COL_IS_UP_TO_DATE)
    private boolean isUpToDate;

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

    @ColumnGetter(columnName = DBConstants.COL_IS_UP_TO_DATE)
    public boolean isUpToDate() {
        return isUpToDate;
    }

    @ColumnSetter(columnName = DBConstants.COL_IS_UP_TO_DATE, type = TYPE_BOOLEAN)
    public void setIsUpToDate(boolean isUpToDate) {
        this.isUpToDate = isUpToDate;
    }

    public String getUpdateSql(long objectId) {
        String sql = "UPDATE " + DBConstants.T_MALSHAB_ITEM + " SET " +
                DBConstants.COL_URL + " = " + prepareStringForSql(url) + "," +
                DBConstants.COL_TITLE + " = " + prepareStringForSql(title) + "," +
                DBConstants.COL_IMAGE_URL + " = " + prepareStringForSql(imageUrl) + "," +
                DBConstants.COL_IS_UP_TO_DATE + " = 1" +
                " WHERE " + DBConstants.COL_ID + " = " + objectId + ";";

        return sql;
    }

    public String getInsertSql() {
        String sql = "INSERT INTO " + DBConstants.T_MALSHAB_ITEM + "(" +
                DBConstants.COL_URL + "," +
                DBConstants.COL_TITLE + "," +
                DBConstants.COL_IMAGE_URL + "," +
                DBConstants.COL_IS_UP_TO_DATE + ")" +
                " VALUES (" + prepareStringForSql(url) +
                "," + prepareStringForSql(title) +
                "," + prepareStringForSql(imageUrl) + ",1);";

        return sql;
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

    public boolean compare(MalshabItem malshabItem) {
        boolean result = false;

        if (url != null && malshabItem.getUrl() != null &&
                !url.equals(malshabItem.getUrl())) {
            result = true;
        } else if (title != null && malshabItem.getTitle() != null &&
                !title.equals(malshabItem.getTitle())) {
            result = true;
        } else if (imageUrl != null && malshabItem.getImageUrl() != null &&
                !imageUrl.equals(malshabItem.getImageUrl())) {
            result = true;
        }

        return result;
    }
}
