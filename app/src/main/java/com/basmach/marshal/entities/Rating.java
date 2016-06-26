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
import com.basmach.marshal.utils.DateHelper;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Ido on 5/3/2016.
 */
@TableName(name = DBConstants.T_RATING)
public class Rating extends DBObject implements Parcelable{

    public Rating(Context context) {
        super(context);
    }

    @PrimaryKey(columnName = DBConstants.COL_ID)
    private long id;

    @Column(name = DBConstants.COL_USER_MAIL_ADDRESS)
    @Expose
    @SerializedName(value = "userMailAddress")
    String userMailAddress;

    @Column(name = DBConstants.COL_COURSE_CODE)
    @Expose
    @SerializedName(value = "courseCode")
    String courseCode;

    @Column(name = DBConstants.COL_RATING)
    @Expose
    @SerializedName(value = "rating")
    double rating;

    @Column(name = DBConstants.COL_COMMENT)
    @Expose
    @SerializedName(value = "comment")
    String comment;

    @Column(name = DBConstants.COL_CREATED_AT)
    @Expose
    @SerializedName(value = "createdAt")
    Date createdAt;

    @Column(name = DBConstants.COL_LAST_MODIFIED)
    @Expose
    @SerializedName(value = "lastModified")
    Date lastModified;

    @Expose
    @SerializedName(value = "plainMailAddress")
    String plainMailAddress;

    @ColumnGetter(columnName = DBConstants.COL_ID)
    public long getId() {
        return id;
    }

    @PrimaryKeySetter
    @ColumnSetter(columnName = DBConstants.COL_ID, type = TYPE_LONG)
    public void setId(long id) {
        this.id = id;
    }

    @ColumnGetter(columnName = DBConstants.COL_USER_MAIL_ADDRESS)
    public String getUserMailAddress() {
        return userMailAddress;
    }

    @ColumnSetter(columnName = DBConstants.COL_USER_MAIL_ADDRESS, type = TYPE_STRING)
    public void setUserMailAddress(String userMailAddress) {
        this.userMailAddress = userMailAddress;
    }

    @ColumnGetter(columnName = DBConstants.COL_COURSE_CODE)
    public String getCourseCode() {
        return courseCode;
    }

    @ColumnSetter(columnName = DBConstants.COL_COURSE_CODE, type = TYPE_STRING)
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    @ColumnGetter(columnName = DBConstants.COL_RATING)
    public double getRating() {
        return rating;
    }

    @ColumnSetter(columnName = DBConstants.COL_RATING, type = TYPE_DOUBLE)
    public void setRating(double rating) {
        this.rating = rating;
    }

    @ColumnGetter(columnName = DBConstants.COL_COMMENT)
    public String getComment() {
        return comment;
    }

    @ColumnSetter(columnName = DBConstants.COL_COMMENT, type = TYPE_STRING)
    public void setComment(String comment) {
        this.comment = comment;
    }

    @ColumnGetter(columnName = DBConstants.COL_CREATED_AT)
    public Date getCreatedAt() {
        return createdAt;
    }

    @ColumnSetter(columnName = DBConstants.COL_CREATED_AT, type = TYPE_DATE)
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @ColumnGetter(columnName = DBConstants.COL_LAST_MODIFIED)
    public Date getLastModified() {
        return lastModified;
    }

    @ColumnSetter(columnName = DBConstants.COL_LAST_MODIFIED, type = TYPE_DATE)
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public void setPlainMailAddress(String plainMailAddress) {
        this.plainMailAddress = plainMailAddress;
    }

    ////////////////////// Parcelable methods ////////////////////////
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Storing the Cycle data to Parcel object
     **/
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(userMailAddress);
        parcel.writeString(courseCode);
        parcel.writeDouble(rating);
        parcel.writeString(comment);
        parcel.writeLong(createdAt.getTime());
        parcel.writeLong(lastModified.getTime());
    }

    /**
     * Retrieving Student data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private Rating(Parcel in){
        this.id = in.readLong();
        this.userMailAddress = in.readString();
        this.courseCode = in.readString();
        this.rating = in.readDouble();
        this.comment = in.readString();
        this.createdAt = new Date(in.readLong());
        this.lastModified = new Date(in.readLong());
    }

    public static final Parcelable.Creator<Rating> CREATOR = new Parcelable.Creator<Rating>() {

        @Override
        public Rating createFromParcel(Parcel source) {
            return new Rating(source);
        }

        @Override
        public Rating[] newArray(int size) {
            return new Rating[size];
        }
    };

    public SQLiteStatement getStatement(SQLiteStatement statement, long objectId) throws Exception{
        if(getCourseCode() != null && !getCourseCode().equals("") &&
                getUserMailAddress() != null && !getUserMailAddress().equals("")
                && getCreatedAt() != null && getLastModified() != null) {
            statement.clearBindings();
            statement.bindLong(1, objectId);
            statement.bindString(2, getUserMailAddress());
            statement.bindString(3, getCourseCode());
            statement.bindDouble(4, getRating());
            statement.bindString(5, DateHelper.dateToString(getCreatedAt()));
            statement.bindString(6, DateHelper.dateToString(getLastModified()));
            if (comment == null)
                comment = "";
            statement.bindString(7, getComment());
            return statement;
        } else {
            return null;
        }
    }
}
