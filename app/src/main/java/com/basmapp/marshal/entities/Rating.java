package com.basmapp.marshal.entities;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;
import android.os.Parcelable;

import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.localdb.annotations.Column;
import com.basmapp.marshal.localdb.annotations.PrimaryKey;
import com.basmapp.marshal.localdb.annotations.TableName;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

@TableName(name = Rating.TABLE_NAME)
public class Rating extends DBObject implements Parcelable {

    public static final String TABLE_NAME = "t_rating";

    public static final String COL_ID = "id";
    public static final String COL_USER_MAIL_ADDRESS = "user_mail_address";
    public static final String COL_COURSE_ID = "course_id";
    public static final String COL_RATING = "rating";
    public static final String COL_COMMENT = "comment";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_LAST_MODIFIED = "last_modified";

    public Rating(Context context) {
        super(context);
    }

    public Rating(Context context, Rating rating) {
        super(context);

        setId(rating.getId());
        setComment(rating.getComment());
        setRating(rating.getRating());
        setCreatedAt(rating.getCreatedAt());
        setUserMailAddress(rating.getUserMailAddress());
        setLastModified(rating.getLastModified());
        setCourseID(rating.getCourseID());
    }

    @PrimaryKey(columnName = COL_ID, isAutoIncrement = true)
    private long id;

    @Column(name = COL_USER_MAIL_ADDRESS)
    @Expose
    @SerializedName(value = "userMailAddress")
    String userMailAddress;

    @Column(name = COL_COURSE_ID)
    int courseID;

    @Column(name = COL_RATING)
    @Expose
    @SerializedName(value = "rating")
    double rating;

    @Column(name = COL_COMMENT)
    @Expose
    @SerializedName(value = "comment")
    String comment;

    @Column(name = COL_CREATED_AT)
    @Expose
    @SerializedName(value = "createdAt")
    Date createdAt;

    @Column(name = COL_LAST_MODIFIED)
    @Expose
    @SerializedName(value = "lastModified")
    Date lastModified;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserMailAddress() {
        return userMailAddress;
    }

    public void setUserMailAddress(String userMailAddress) {
        this.userMailAddress = userMailAddress;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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
        parcel.writeInt(courseID);
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
    private Rating(Parcel in) {
        this.id = in.readLong();
        this.userMailAddress = in.readString();
        this.courseID = in.readInt();
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

    public SQLiteStatement getStatement(SQLiteStatement statement, long objectId, int courseID) throws Exception {
        if (courseID != 0 &&
                getUserMailAddress() != null && !getUserMailAddress().equals("")
                && getCreatedAt() != null && getLastModified() != null) {
            statement.clearBindings();
            statement.bindLong(1, objectId);
            statement.bindString(2, getUserMailAddress());
            statement.bindLong(3, courseID);
            statement.bindDouble(4, getRating());
            statement.bindLong(5, createdAt.getTime());
            statement.bindLong(6, lastModified.getTime());
            if (comment == null)
                comment = "";
            statement.bindString(7, getComment());
            return statement;
        } else {
            return null;
        }
    }
}
