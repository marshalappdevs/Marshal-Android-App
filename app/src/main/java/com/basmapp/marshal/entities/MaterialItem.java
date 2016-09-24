package com.basmapp.marshal.entities;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;
import android.os.Parcelable;

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

@TableName(name = DBConstants.T_MATERIAL_ITEM)
public class MaterialItem extends DBObject implements Parcelable {

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
    @SerializedName("baseUrl")
    @Column(name = DBConstants.COL_BASE_URL)
    private String baseUrl;

    @Expose
    @SerializedName("imageUrl")
    @Column(name = DBConstants.COL_IMAGE_URL)
    private String imageUrl;

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

    @ColumnGetter(columnName = DBConstants.COL_BASE_URL)
    public String getBaseUrl() {
        return baseUrl;
    }

    @ColumnSetter(columnName = DBConstants.COL_BASE_URL, type = TYPE_STRING)
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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
        if ((getUrl() != null && !getUrl().equals("")) &&
                (getTitle() != null && !getTitle().equals(""))) {
            statement.clearBindings();
            statement.bindLong(1, objectId);
            statement.bindString(2, getUrl());
            statement.bindString(3, getTitle());

            if (description == null)
                description = "";
            statement.bindString(4, description);

            if (baseUrl == null)
                baseUrl = "";
            statement.bindString(5, baseUrl);

            if (tags == null)
                tags = "";
            statement.bindString(6, tags);

            if (imageUrl == null)
                imageUrl = "";
            statement.bindString(7, imageUrl);

            return statement;
        } else {
            return null;
        }
    }

    public static String getSelectCourseMaterialsQuery(String courseCode) {
        String query;

        query = "SELECT * FROM " + DBConstants.T_MATERIAL_ITEM + " WHERE " + DBConstants.COL_TAGS +
                " LIKE '%" + courseCode + "%';";

        return query;
    }

    ///////////////////// Parcelable methods //////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Storing the Course data to Parcel object
     **/
    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeLong(id);
        dest.writeString(url);
        dest.writeString(tags);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(baseUrl);
        dest.writeString(imageUrl);
    }

    /**
     * Retrieving Student data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private MaterialItem(Parcel in) {
        this.id = in.readLong();
        this.url = in.readString();
        this.tags = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.baseUrl = in.readString();
        this.imageUrl = in.readString();
    }

    public static final Parcelable.Creator<MaterialItem> CREATOR = new Parcelable.Creator<MaterialItem>() {

        @Override
        public MaterialItem createFromParcel(Parcel source) {
            return new MaterialItem(source);
        }

        @Override
        public MaterialItem[] newArray(int size) {
            return new MaterialItem[size];
        }
    };

    public String getUpdateSql(long objectId) {
        String sql = "UPDATE " + DBConstants.T_MATERIAL_ITEM + " SET " +
                DBConstants.COL_URL + " = " + prepareStringForSql(url) + "," +
                DBConstants.COL_TITLE + " = " + prepareStringForSql(title) + "," +
                DBConstants.COL_DESCRIPTION + " = " + prepareStringForSql(description) + "," +
                DBConstants.COL_BASE_URL + " = " + prepareStringForSql(baseUrl) + "," +
                DBConstants.COL_TAGS + " = " + prepareStringForSql(tags) + "," +
                DBConstants.COL_IMAGE_URL + " = " + prepareStringForSql(imageUrl) + "," +
                DBConstants.COL_IS_UP_TO_DATE + " = 1" +
                " WHERE " + DBConstants.COL_ID + " = " + objectId + ";";

        return sql;
    }

    public String getInsertSql() {
        String sql = "INSERT INTO " + DBConstants.T_MATERIAL_ITEM + "(" +
                DBConstants.COL_URL + "," +
                DBConstants.COL_TITLE + "," +
                DBConstants.COL_DESCRIPTION + "," +
                DBConstants.COL_BASE_URL + "," +
                DBConstants.COL_TAGS + "," +
                DBConstants.COL_IMAGE_URL + "," +
                DBConstants.COL_IS_UP_TO_DATE + ")" +
                " VALUES (" + prepareStringForSql(url) + "," + prepareStringForSql(title) + "," +
                prepareStringForSql(description) + "," + prepareStringForSql(baseUrl)
                + "," + prepareStringForSql(tags) + "," + prepareStringForSql(imageUrl) + ",1);";

        return sql;
    }
}
