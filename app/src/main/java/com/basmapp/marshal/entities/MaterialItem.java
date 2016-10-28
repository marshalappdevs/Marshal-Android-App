package com.basmapp.marshal.entities;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;
import android.os.Parcelable;

import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.localdb.annotations.Column;
import com.basmapp.marshal.localdb.annotations.PrimaryKey;
import com.basmapp.marshal.localdb.annotations.TableName;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@TableName(name = DBConstants.T_MATERIAL_ITEM)
public class MaterialItem extends DBObject implements Parcelable {

    @PrimaryKey(columnName = DBConstants.COL_ID, isAutoIncrement = true)
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

    @Override
    protected boolean isPrimaryKeyAutoIncrement() {
        return true;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

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

    public static String getSelectCourseMaterialsQuery(int courseID) {
        String query;

        query = "SELECT * FROM " + DBConstants.T_MATERIAL_ITEM + " WHERE " + DBConstants.COL_TAGS +
                " LIKE '%" + String.valueOf(courseID) + "%';";

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
