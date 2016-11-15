package com.basmapp.marshal.entities;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;
import android.os.Parcelable;

import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.localdb.annotations.Column;
import com.basmapp.marshal.localdb.annotations.PrimaryKey;
import com.basmapp.marshal.localdb.annotations.TableName;
import com.basmapp.marshal.util.DateHelper;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

@TableName(name = Cycle.TABLE_NAME)
public class Cycle extends DBObject implements Parcelable {

    public static final String TABLE_NAME = "t_cycle";

    public static final String COL_ID = "id";
    public static final String COL_COURSE_ID = "course_id";
    public static final String COL_NAME = "name";
    public static final String COL_MAX_PEOPLE = "max_people";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_START_DATE = "start_date";
    public static final String COL_END_DATE = "end_date";

    @PrimaryKey(columnName = COL_ID, isAutoIncrement = true)
    private long id;

    @Expose
    @SerializedName("CourseID")
    @Column(name = COL_COURSE_ID)
    private int courseID;

    @Expose
    @SerializedName("Name")
    @Column(name = COL_NAME)
    private String name;

    @Expose
    @SerializedName("MaximumPeople")
    @Column(name = COL_MAX_PEOPLE)
    private int maximumPeople;

    @Expose
    @SerializedName("Description")
    @Column(name = COL_DESCRIPTION)
    private String description;

    @Expose
    @SerializedName("StartDate")
    @Column(name = COL_START_DATE)
    private Date startDate;

    @Expose
    @SerializedName("EndDate")
    @Column(name = COL_END_DATE)
    private Date endDate;

    public Cycle(Context context) {
        super(context);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaximumPeople() {
        return maximumPeople;
    }

    public void setMaximumPeople(int maximumPeople) {
        this.maximumPeople = maximumPeople;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    ////////////////////// Methods ////////////////////////

    public String getStartDateAsString() {
        return DateHelper.dateToString(startDate);
    }

    public String getEndDateAsString() {
        return DateHelper.dateToString(endDate);
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
        parcel.writeString(name);
        parcel.writeInt(courseID);
        parcel.writeInt(maximumPeople);
        parcel.writeString(description);
        parcel.writeString(DateHelper.dateToString(startDate));
        parcel.writeString(DateHelper.dateToString(endDate));
    }

    /**
     * Retrieving Student data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private Cycle(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.courseID = in.readInt();
        this.maximumPeople = in.readInt();
        this.description = in.readString();
        this.startDate = DateHelper.stringToDate(in.readString());
        this.endDate = DateHelper.stringToDate(in.readString());
    }

    public static final Parcelable.Creator<Cycle> CREATOR = new Parcelable.Creator<Cycle>() {

        @Override
        public Cycle createFromParcel(Parcel source) {
            return new Cycle(source);
        }

        @Override
        public Cycle[] newArray(int size) {
            return new Cycle[size];
        }
    };

    public SQLiteStatement getStatement(SQLiteStatement statement, int courseId, long objectId) throws Exception {
        if (startDate != null && endDate != null && (startDate.compareTo(new Date()) > 0)) {
            statement.clearBindings();
            statement.bindLong(1, objectId);
            statement.bindLong(2, courseId);

            if (name == null)
                name = "";
            statement.bindString(3, getName());
            statement.bindLong(4, getMaximumPeople());

            if (description == null)
                description = "";
            statement.bindString(5, getDescription());
            statement.bindLong(6, startDate.getTime());
            statement.bindLong(7, endDate.getTime());

            return statement;
        } else return null;
    }
}
