package com.basmach.marshal.entities;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class Course implements Parcelable {

    private long id;
    private String courseID;
    private String name;
    private int minimumPeople;
    private int maximumPeople;
    private String description;
    private String prerequisites;
    private String targetPopulation;
    private String professionalDomain;
    private String syllabus;
    private String dayTime;
    private int durationInHours;
    private int durationInDays;
    private String comments;
    private int passingGrade;
    private double price;
    private ArrayList<Cycle> cycles = new ArrayList<>();
    private String photoUrl;
    private Boolean isMooc;

    public Course () {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseID;
    }

    public void setCourseCode(String courseCode) {
        this.courseID = courseCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinimumPeople() {
        return minimumPeople;
    }

    public void setMinimumPeople(int minimumPeople) {
        this.minimumPeople = minimumPeople;
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

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public String getTargetPopulation() {
        return targetPopulation;
    }

    public void setTargetPopulation(String targetPopulation) {
        this.targetPopulation = targetPopulation;
    }

    public String getProfessionalDomain() {
        return professionalDomain;
    }

    public void setProfessionalDomain(String professionalDomain) {
        this.professionalDomain = professionalDomain;
    }

    public String getSyllabus() {
        return syllabus;
    }

    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
    }

    public String getDayTime() {
        return dayTime;
    }

    public void setDayTime(String dayTime) {
        this.dayTime = dayTime;
    }

    public int getDurationInHours() {
        return durationInHours;
    }

    public void setDurationInHours(int durationInHours) {
        this.durationInHours = durationInHours;
    }

    public int getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getPassingGrade() {
        return passingGrade;
    }

    public void setPassingGrade(int passingGrade) {
        this.passingGrade = passingGrade;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ArrayList getCycles() {
        return cycles;
    }

    public void setCycles(ArrayList cycles) {
        this.cycles = cycles;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /////////////////////////// methods ////////////////////////////

    public void addCycle(Cycle cycle) {
        cycles.add(cycle);
    }

    public Boolean getIsMooc() {
        return isMooc;
    }

    public void setIsMooc(Boolean isMooc) {
        this.isMooc = isMooc;
    }

    public void getPhotoViaPicasso(Context context, final ImageView imageView, Callback callback) {
        Picasso.with(context).load(this.getPhotoUrl())
                .into(imageView, callback);
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
        dest.writeString(courseID);
        dest.writeString(name);
        dest.writeInt(minimumPeople);
        dest.writeInt(maximumPeople);
        dest.writeString(description);
        dest.writeString(prerequisites);
        dest.writeString(targetPopulation);
        dest.writeString(professionalDomain);
        dest.writeString(syllabus);
        dest.writeString(dayTime);
        dest.writeInt(durationInHours);
        dest.writeInt(durationInDays);
        dest.writeString(comments);
        dest.writeInt(passingGrade);
        dest.writeDouble(price);
        dest.writeTypedList(cycles);
        dest.writeString(photoUrl);
        dest.writeInt((isMooc) ? 1 : 0);
    }

    /**
     * Retrieving Student data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private Course(Parcel in){
        this.id = in.readLong();
        this.courseID = in.readString();
        this.name = in.readString();
        this.minimumPeople = in.readInt();
        this.maximumPeople = in.readInt();
        this.description = in.readString();
        this.prerequisites = in.readString();
        this.targetPopulation = in.readString();
        this.professionalDomain = in.readString();
        this.syllabus = in.readString();
        this.dayTime = in.readString();
        this.durationInHours = in.readInt();
        this.durationInDays = in.readInt();
        this.comments = in.readString();
        this.passingGrade = in.readInt();
        this.price = in.readDouble();
        in.readTypedList(cycles, Cycle.CREATOR);
        this.photoUrl = in.readString();
        this.isMooc = (in.readInt() != 0);
    }

    public static final Parcelable.Creator<Course> CREATOR = new Parcelable.Creator<Course>() {

        @Override
        public Course createFromParcel(Parcel source) {
            return new Course(source);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };
}
