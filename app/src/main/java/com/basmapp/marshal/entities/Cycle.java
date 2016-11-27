package com.basmapp.marshal.entities;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.basmapp.marshal.R;
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
    public static final String COL_COURSE_ID = Course.COL_COURSE_ID;
    public static final String COL_NAME = "name";
    public static final String COL_MAX_PEOPLE = "max_people";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_START_DATE = "start_date";
    public static final String COL_END_DATE = "end_date";
    public static final String COL_GOOGLE_FORM_URL = "col_google_form_url";

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

    @Expose
    @SerializedName("GoogleFormsUrl")
    @Column(name = COL_GOOGLE_FORM_URL)
    private String googleFormUrl;

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

    public String getGoogleFormUrl() {
        return googleFormUrl;
    }

    public void setGoogleFormUrl(String googleFormUrl) {
        this.googleFormUrl = googleFormUrl;
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
        parcel.writeString(googleFormUrl);
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
        this.googleFormUrl = in.readString();
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

    public String toDatesRangeString(Context context) {
        return String.format(context.getString(R.string.course_cycle_format),
                DateHelper.dateToString(this.getStartDate()),
                DateHelper.dateToString(this.getEndDate()));
    }

    public boolean isRunningNow() {
        long now = new Date().getTime();
        return (now > this.getStartDate().getTime() || DateHelper.isSameDate(this.getStartDate().getTime(), now)) &&
                ((now < this.getEndDate().getTime()) || DateHelper.isSameDate(now, this.getEndDate().getTime()));
    }
}
