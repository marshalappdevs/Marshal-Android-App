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

    @Expose
    @SerializedName("_id")
    @PrimaryKey(columnName = DBConstants.COL_ID, isAutoIncrement = false)
    private String id;

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

    @Column(name = DBConstants.COL_IS_UP_TO_DATE)
    private boolean isUpToDate;

    // Constructors
    public MaterialItem(Context context) {
        super(context);
    }

    @Override
    protected boolean isPrimaryKeyAutoIncrement() {
        return false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public boolean getIsUpToDate() {
        return isUpToDate;
    }

    public void setIsUpToDate(boolean isUpToDate) {
        this.isUpToDate = isUpToDate;
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
        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(tags);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(baseUrl);
        dest.writeString(imageUrl);
        dest.writeInt(isUpToDate ? 1 : 0);
    }

    /**
     * Retrieving Student data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private MaterialItem(Parcel in) {
        this.id = in.readString();
        this.url = in.readString();
        this.tags = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.baseUrl = in.readString();
        this.imageUrl = in.readString();
        this.isUpToDate = in.readInt() != 0;
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
}
