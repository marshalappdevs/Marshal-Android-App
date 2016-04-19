package com.basmach.marshal.entities;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import com.basmach.marshal.R;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.DBObject;
import com.basmach.marshal.localdb.annotations.Column;
import com.basmach.marshal.localdb.annotations.ColumnGetter;
import com.basmach.marshal.localdb.annotations.ColumnSetter;
import com.basmach.marshal.localdb.annotations.EntityArraySetter;
import com.basmach.marshal.localdb.annotations.ForeignKeyEntityArray;
import com.basmach.marshal.localdb.annotations.PrimaryKey;
import com.basmach.marshal.localdb.annotations.PrimaryKeySetter;
import com.basmach.marshal.localdb.annotations.TableName;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

@TableName(name = DBConstants.T_COURSE)
public class Course extends DBObject implements Parcelable{

    // TODO RETROFIT SerializedName
    @PrimaryKey(columnName = DBConstants.COL_ID)
    private long id;

    @Expose
    @SerializedName("ID")
    @Column(name = DBConstants.COL_COURSE_ID)
    private String courseID;

    @Expose
    @SerializedName("Name")
    @Column(name = DBConstants.COL_NAME)
    private String name;

    @Expose
    @SerializedName("MinimumPeople")
    @Column(name = DBConstants.COL_MIN_PEOPLE)
    private int minimumPeople;

    @Expose
    @SerializedName("MaximumPeople")
    @Column(name = DBConstants.COL_MAX_PEOPLE)
    private int maximumPeople;

    @Expose
    @SerializedName("Description")
    @Column(name = DBConstants.COL_DESCRIPTION)
    private String description;

    @Column(name = DBConstants.COL_PREREQUISITES)
    private String prerequisites;

    @Expose
    @SerializedName("TargetPopulation")
    @Column(name = DBConstants.COL_TARGET_POPULATION)
    private String targetPopulation;

    @Expose
    @SerializedName("ProfessionalDomain")
    @Column(name = DBConstants.COL_PROFESSIONAL_DOMAIN)
    private String professionalDomain;

    @Expose
    @SerializedName("Syllabus")
    @Column(name = DBConstants.COL_SYLLABUS)
    private String syllabus;

    @Expose
    @SerializedName("DayTime")
    @Column(name = DBConstants.COL_DAYTIME)
    private String dayTime;

    @Expose
    @SerializedName("DurationInHours")
    @Column(name = DBConstants.COL_DURATION_IN_HOURS)
    private int durationInHours;

    @Expose
    @SerializedName("DurationInDays")
    @Column(name = DBConstants.COL_DURATION_IN_DAYS)
    private int durationInDays;

    @Expose
    @SerializedName("Comments")
    @Column(name = DBConstants.COL_COMMENTS)
    private String comments;

    @Expose
    @SerializedName("PassingGrade")
    @Column(name = DBConstants.COL_PASSING_GRADE)
    private int passingGrade;

    @Column(name = DBConstants.COL_PRICE)
    private long price;

    @Expose
    @SerializedName("cycleList")
    @ForeignKeyEntityArray(fkColumnName = DBConstants.COL_CYCLES, entityClass = Cycle.class)
    private ArrayList<Cycle> cycles = new ArrayList<>();

    @Column(name = DBConstants.COL_IMAGE_URL)
    private String imageUrl;

    @Expose
    @SerializedName("IsMooc")
    @Column(name = DBConstants.COL_IS_MOOC)
    private Boolean isMooc;

    public Course (Context context) {
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

    @ColumnGetter(columnName = DBConstants.COL_COURSE_ID)
    public String getCourseCode() {
        return courseID;
    }

    @ColumnSetter(columnName = DBConstants.COL_COURSE_ID, type = TYPE_STRING)
    public void setCourseCode(String courseCode) {
        this.courseID = courseCode;
    }

    @ColumnGetter(columnName = DBConstants.COL_NAME)
    public String getName() {
        return name;
    }

    @ColumnSetter(columnName = DBConstants.COL_NAME, type = TYPE_STRING)
    public void setName(String name) {
        this.name = name;
    }

    @ColumnGetter(columnName = DBConstants.COL_MIN_PEOPLE)
    public int getMinimumPeople() {
        return minimumPeople;
    }

    @ColumnSetter(columnName = DBConstants.COL_MIN_PEOPLE, type = TYPE_INT)
    public void setMinimumPeople(int minimumPeople) {
        this.minimumPeople = minimumPeople;
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

    @ColumnGetter(columnName = DBConstants.COL_PREREQUISITES)
    public String getPrerequisites() {
        return prerequisites;
    }

    @ColumnSetter(columnName = DBConstants.COL_PREREQUISITES, type = TYPE_STRING)
    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    @ColumnGetter(columnName = DBConstants.COL_TARGET_POPULATION)
    public String getTargetPopulation() {
        return targetPopulation;
    }

    @ColumnSetter(columnName = DBConstants.COL_TARGET_POPULATION, type = TYPE_STRING)
    public void setTargetPopulation(String targetPopulation) {
        this.targetPopulation = targetPopulation;
    }

    @ColumnGetter(columnName = DBConstants.COL_PROFESSIONAL_DOMAIN)
    public String getProfessionalDomain() {
        return professionalDomain;
    }

    @ColumnSetter(columnName = DBConstants.COL_PROFESSIONAL_DOMAIN, type = TYPE_STRING)
    public void setProfessionalDomain(String professionalDomain) {
        this.professionalDomain = professionalDomain;
    }

    @ColumnGetter(columnName = DBConstants.COL_SYLLABUS)
    public String getSyllabus() {
        return syllabus;
    }

    @ColumnSetter(columnName = DBConstants.COL_SYLLABUS, type = TYPE_STRING)
    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
    }

    @ColumnGetter(columnName = DBConstants.COL_DAYTIME)
    public String getDayTime() {
        return dayTime;
    }

    @ColumnSetter(columnName = DBConstants.COL_DAYTIME, type = TYPE_STRING)
    public void setDayTime(String dayTime) {
        this.dayTime = dayTime;
    }

    @ColumnGetter(columnName = DBConstants.COL_DURATION_IN_HOURS)
    public int getDurationInHours() {
        return durationInHours;
    }

    @ColumnSetter(columnName = DBConstants.COL_DURATION_IN_HOURS, type = TYPE_INT)
    public void setDurationInHours(int durationInHours) {
        this.durationInHours = durationInHours;
    }

    @ColumnGetter(columnName = DBConstants.COL_DURATION_IN_DAYS)
    public int getDurationInDays() {
        return durationInDays;
    }

    @ColumnSetter(columnName = DBConstants.COL_DURATION_IN_DAYS, type = TYPE_INT)
    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }

    @ColumnGetter(columnName = DBConstants.COL_COMMENTS)
    public String getComments() {
        return comments;
    }

    @ColumnSetter(columnName = DBConstants.COL_COMMENTS, type = TYPE_STRING)
    public void setComments(String comments) {
        this.comments = comments;
    }

    @ColumnGetter(columnName = DBConstants.COL_PASSING_GRADE)
    public int getPassingGrade() {
        return passingGrade;
    }

    @ColumnSetter(columnName = DBConstants.COL_PASSING_GRADE, type = TYPE_INT)
    public void setPassingGrade(int passingGrade) {
        this.passingGrade = passingGrade;
    }

    @ColumnGetter(columnName = DBConstants.COL_PRICE)
    public long getPrice() {
        return price;
    }

    @ColumnSetter(columnName = DBConstants.COL_PRICE, type = TYPE_LONG)
    public void setPrice(long price) {
        this.price = price;
    }

    @ColumnGetter(columnName = DBConstants.COL_CYCLES)
    public ArrayList<Cycle> getCycles() {
        return cycles;
    }

    @EntityArraySetter(fkColumnName = DBConstants.COL_CYCLES, entityClass = Cycle.class)
    public void setCycles(ArrayList<Cycle> cycles) {
        this.cycles = cycles;
    }

    @ColumnGetter(columnName = DBConstants.COL_IMAGE_URL)
    public String getImageUrl() {
        return imageUrl;
    }

    @ColumnSetter(columnName = DBConstants.COL_IMAGE_URL, type = TYPE_STRING)
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @ColumnGetter(columnName = DBConstants.COL_IS_MOOC)
    public Boolean getIsMooc() {
        return isMooc;
    }

    @ColumnSetter(columnName = DBConstants.COL_IS_MOOC, type = TYPE_BOOLEAN)
    public void setIsMooc(Boolean isMooc) {
        this.isMooc = isMooc;
    }

    /////////////////////////// methods ////////////////////////////

    public void addCycle(Cycle cycle) {
        cycles.add(cycle);
    }

    public void getPhotoViaPicasso(Context context, final ImageView imageView, Callback callback) {
        Picasso.with(context)
                .load(this.getImageUrl())
                .placeholder(R.drawable.ic_course_placeholder)
                .error(R.drawable.ic_course_error)
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
        dest.writeLong(price);
        dest.writeTypedList(cycles);
        dest.writeString(imageUrl);
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
        this.price = in.readLong();
        in.readTypedList(cycles, Cycle.CREATOR);
        this.imageUrl = in.readString();
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
