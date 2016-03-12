package com.basmach.marshal.entities;

import android.os.Parcel;
import android.os.Parcelable;
import com.basmach.marshal.ui.utils.DateHelper;
import java.util.Date;

public class Cycle implements Parcelable{
    private long id;
    private String name;
    private int maximumPeople;
    private String description;
    private Date startDate;
    private Date endDate;

    public Cycle() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}
