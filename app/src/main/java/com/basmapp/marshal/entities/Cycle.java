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
import com.basmapp.marshal.utils.DateHelper;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

@TableName(name = DBConstants.T_CYCLE)
public class Cycle extends DBObject implements Parcelable {

    @PrimaryKey(columnName = DBConstants.COL_ID)
    private long id;

    @Expose
    @SerializedName("CourseID")
    @Column(name = DBConstants.COL_COURSE_CODE)
    private String courseCode;

    @Expose
    @SerializedName("Name")
    @Column(name = DBConstants.COL_NAME)
    private String name;

    @Expose
    @SerializedName("MaximumPeople")
    @Column(name = DBConstants.COL_MAX_PEOPLE)
    private int maximumPeople;

    @Expose
    @SerializedName("Description")
    @Column(name = DBConstants.COL_DESCRIPTION)
    private String description;

    @Expose
    @SerializedName("StartDate")
    @Column(name = DBConstants.COL_START_DATE)
    private Date startDate;

    @Expose
    @SerializedName("EndDate")
    @Column(name = DBConstants.COL_END_DATE)
    private Date endDate;

    public Cycle(Context context) {
        super(context);
    }

    @ColumnGetter(columnName = DBConstants.COL_ID)
    public long getId() {
        return id;
    }

    @PrimaryKeySetter
    @ColumnSetter(columnName = DBConstants.COL_ID, type = TYPE_LONG)
    public void setId(long id) {
        this.id = id;
    }

    @ColumnGetter(columnName = DBConstants.COL_COURSE_CODE)
    public String getCourseCode() {
        return courseCode;
    }

    @ColumnSetter(columnName = DBConstants.COL_COURSE_CODE, type = TYPE_STRING)
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    @ColumnGetter(columnName = DBConstants.COL_NAME)
    public String getName() {
        return name;
    }

    @ColumnSetter(columnName = DBConstants.COL_NAME, type = TYPE_STRING)
    public void setName(String name) {
        this.name = name;
    }

    @ColumnGetter(columnName = DBConstants.COL_MAX_PEOPLE)
    public int getMaximumPeople() {
        return maximumPeople;
    }

    @ColumnSetter(columnName = DBConstants.COL_MAX_PEOPLE, type = TYPE_INT)
    public void setMaximumPeople(int maximumPeople) {
        this.maximumPeople = maximumPeople;
    }

    @ColumnGetter(columnName = DBConstants.COL_DESCRIPTION)
    public String getDescription() {
        return description;
    }

    @ColumnSetter(columnName = DBConstants.COL_DESCRIPTION, type = TYPE_STRING)
    public void setDescription(String description) {
        this.description = description;
    }

    @ColumnGetter(columnName = DBConstants.COL_START_DATE)
    public Date getStartDate() {
        return startDate;
    }

    @ColumnSetter(columnName = DBConstants.COL_START_DATE, type = TYPE_DATE)
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @ColumnGetter(columnName = DBConstants.COL_END_DATE)
    public Date getEndDate() {
        return endDate;
    }

    @ColumnSetter(columnName = DBConstants.COL_END_DATE, type = TYPE_DATE)
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
    private Cycle(Parcel in){
        this.id = in.readLong();
        this.name = in.readString();
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

    public SQLiteStatement getStatement(SQLiteStatement statement,String courseId, long objectId) throws Exception {
        if (startDate != null && endDate != null && (startDate.compareTo(new Date()) > 0)) {
            statement.clearBindings();
            statement.bindLong(1, objectId);
            statement.bindString(2, courseId);

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
