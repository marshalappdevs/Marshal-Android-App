package com.basmach.marshal.entities;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;
import android.os.Parcelable;

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
public class MaterialItem extends DBObject implements Parcelable{

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


    public SQLiteStatement getStatement(SQLiteStatement statement, long objectId) throws Exception{
        if ((getUrl() != null && !getUrl().equals("")) &&
                (getTitle() != null && !getTitle().equals(""))) {
            statement.clearBindings();
            statement.bindLong(1, objectId);
            statement.bindString(2, getUrl());
            statement.bindString(3, getTitle());

            if (description == null)
                description = "";
            statement.bindString(4, description);

            if (cannonicalUrl == null)
                cannonicalUrl = "";
            statement.bindString(5, cannonicalUrl);

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
        dest.writeString(cannonicalUrl);
        dest.writeString(imageUrl);
    }

    /**
     * Retrieving Student data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private MaterialItem(Parcel in){
        this.id = in.readLong();
        this.url = in.readString();
        this.tags = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.cannonicalUrl = in.readString();
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
}
