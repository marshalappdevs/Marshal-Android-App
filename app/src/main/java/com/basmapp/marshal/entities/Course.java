package com.basmapp.marshal.entities;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.localdb.annotations.Column;
import com.basmapp.marshal.localdb.annotations.ForeignKeyEntityArray;
import com.basmapp.marshal.localdb.annotations.PrimaryKey;
import com.basmapp.marshal.localdb.annotations.TableName;
import com.bumptech.glide.Glide;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

@TableName(name = DBConstants.T_COURSE)
public class Course extends DBObject implements Parcelable {

    public static final String CATEGORY_SOFTWARE = "software";
    public static final String CATEGORY_CYBER = "cyber";
    public static final String CATEGORY_IT = "it";
    public static final String CATEGORY_TOOLS = "tools";
    public static final String CATEGORY_SYSTEM = "system";

    // TODO RETROFIT SerializedName
    @Expose
    @SerializedName("_id")
    @PrimaryKey(columnName = DBConstants.COL_ID, isAutoIncrement = false)
    private String id;

    @Expose
    @SerializedName("ID")
    @Column(name = DBConstants.COL_COURSE_ID)
    private int courseID;

    @Expose
    @SerializedName("CourseCode")
    @Column(name = DBConstants.COL_COURSE_CODE)
    private String courseCode;

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

    @Expose
    @SerializedName("cycleList")
    @ForeignKeyEntityArray(valueColumnName = DBConstants.COL_COURSE_ID,
            fkColumnName = DBConstants.COL_COURSE_ID, entityClass = Cycle.class)
    private ArrayList<Cycle> cycles = new ArrayList<>();

    @Expose
    @SerializedName("Ratings")
    @ForeignKeyEntityArray(valueColumnName = DBConstants.COL_COURSE_ID,
            fkColumnName = DBConstants.COL_COURSE_ID, entityClass = Rating.class)
    private ArrayList<Rating> ratings = new ArrayList<>();

    @Expose
    @SerializedName("PictureUrl")
    @Column(name = DBConstants.COL_IMAGE_URL)
    private String imageUrl;

    @Expose
    @SerializedName("IsMooc")
    @Column(name = DBConstants.COL_IS_MOOC)
    private boolean isMooc;

    @Expose
    @SerializedName("IsMeetup")
    @Column(name = DBConstants.COL_IS_MEETUP)
    private boolean isMeetup;

    @Expose
    @SerializedName("Category")
    @Column(name = DBConstants.COL_CATEGORY)
    private String category;

    @Column(name = DBConstants.COL_IS_USER_SUBSCRIBE)
    private boolean isUserSubscribe;

    public Course(Context context) {
        super(context);
    }

    @Override
    protected boolean isPrimaryKeyAutoIncrement() {
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
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

    public ArrayList<Cycle> getCycles() {
        return cycles;
    }

    public void setCycles(ArrayList<Cycle> cycles) {
        this.cycles = cycles;
    }

    public ArrayList<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(ArrayList<Rating> ratings) {
        this.ratings = ratings;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getIsMooc() {
        return isMooc;
    }

    public void setIsMooc(boolean isMooc) {
        this.isMooc = isMooc;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getIsMeetup() {
        return isMeetup;
    }

    public void setIsMeetup(boolean isMeetup) {
        this.isMeetup = isMeetup;
    }

    public boolean getIsUserSubscribe() {
        return this.isUserSubscribe;
    }

    public void setIsUserSubscribe(boolean isUserSubscribe) {
        this.isUserSubscribe = isUserSubscribe;
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
        dest.writeInt(courseID);
        dest.writeString(courseCode);
        dest.writeString(name);
        dest.writeInt(minimumPeople);
        dest.writeInt(maximumPeople);
        dest.writeString(description);
        dest.writeString(prerequisites);
        dest.writeString(professionalDomain);
        dest.writeString(syllabus);
        dest.writeString(dayTime);
        dest.writeInt(durationInHours);
        dest.writeInt(durationInDays);
        dest.writeString(comments);
        dest.writeInt(passingGrade);
        dest.writeTypedList(cycles);
        dest.writeString(imageUrl);
        dest.writeString(category);
        dest.writeInt((isMooc) ? 1 : 0);
        dest.writeInt((isMeetup) ? 1 : 0);
        dest.writeInt((isUserSubscribe) ? 1 : 0);
    }

    /**
     * Retrieving Student data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private Course(Parcel in) {
        this.id = in.readString();
        this.courseID = in.readInt();
        this.courseCode = in.readString();
        this.name = in.readString();
        this.minimumPeople = in.readInt();
        this.maximumPeople = in.readInt();
        this.description = in.readString();
        this.prerequisites = in.readString();
        this.professionalDomain = in.readString();
        this.syllabus = in.readString();
        this.dayTime = in.readString();
        this.durationInHours = in.readInt();
        this.durationInDays = in.readInt();
        this.comments = in.readString();
        this.passingGrade = in.readInt();
        in.readTypedList(cycles, Cycle.CREATOR);
        this.imageUrl = in.readString();
        this.category = in.readString();
        this.isMooc = (in.readInt() != 0);
        this.isMeetup = (in.readInt() != 0);
        this.isUserSubscribe = (in.readInt() != 0);
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

    // ************************* END OF PARCELABLE **************************** //

    public static String getCloestCoursesSqlQuery(int count, boolean filterByNowTimestamp) {
        String query = "select * from " + DBConstants.T_COURSE
                + " where " + DBConstants.COL_COURSE_ID + " IN " +
                "(select distinct " + DBConstants.COL_COURSE_ID + " from " + DBConstants.T_CYCLE;

        if (filterByNowTimestamp)
            query += " where " + DBConstants.COL_START_DATE + " >= " + String.valueOf(new Date().getTime());

        query += " order by " + DBConstants.COL_START_DATE + " ASC limit " + String.valueOf(count) + ");\n";

        return query;
    }

    public Cycle getFirstCycle() {
        if (cycles != null && cycles.size() > 0) {
            Cycle firstCycle = cycles.get(0);

            for (Cycle cycle : cycles) {
                try {
                    if (firstCycle.getStartDate() != null && cycle.getStartDate() != null) {
                        if (firstCycle.getStartDate().compareTo(cycle.getStartDate()) > 0) {
                            firstCycle = cycle;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return firstCycle;
        } else {
            return null;
        }
    }

    public String getInsertSql() {
        String sql = null;
        String values = " VALUES (";
        String pkColumn = "";

        if (!isPrimaryKeyAutoIncrement()) {
            pkColumn = DBConstants.COL_ID + ",";
            values = values + "'" + id + "',";
        }

        try {
            values = values + courseID +
                    "," + prepareStringForSql(courseCode) +
                    "," + prepareStringForSql(name) +
                    "," + minimumPeople +
                    "," + maximumPeople +
                    "," + prepareStringForSql(description) +
                    "," + prepareStringForSql(prerequisites) +
                    "," + prepareStringForSql(professionalDomain) +
                    "," + prepareStringForSql(syllabus) +
                    "," + prepareStringForSql(dayTime) +
                    "," + durationInHours +
                    "," + durationInDays +
                    "," + prepareStringForSql(comments) +
                    "," + passingGrade +
                    "," + (isMooc ? 1 : 0) +
                    "," + (isMeetup ? 1 : 0) +
                    "," + prepareStringForSql(category) +
                    "," + prepareStringForSql(imageUrl) + ",1);";

            sql = "INSERT INTO " + DBConstants.T_COURSE + "(" +
                    pkColumn +
                    DBConstants.COL_COURSE_ID + "," +
                    DBConstants.COL_COURSE_CODE + "," +
                    DBConstants.COL_NAME + "," +
                    DBConstants.COL_MIN_PEOPLE + "," +
                    DBConstants.COL_MAX_PEOPLE + "," +
                    DBConstants.COL_DESCRIPTION + "," +
                    DBConstants.COL_PREREQUISITES + "," +
                    DBConstants.COL_PROFESSIONAL_DOMAIN + "," +
                    DBConstants.COL_SYLLABUS + "," +
                    DBConstants.COL_DAYTIME + "," +
                    DBConstants.COL_DURATION_IN_HOURS + "," +
                    DBConstants.COL_DURATION_IN_DAYS + "," +
                    DBConstants.COL_COMMENTS + "," +
                    DBConstants.COL_PASSING_GRADE + "," +
                    DBConstants.COL_IS_MOOC + "," +
                    DBConstants.COL_IS_MEETUP + "," +
                    DBConstants.COL_CATEGORY + "," +
                    DBConstants.COL_IMAGE_URL + "," +
                    DBConstants.COL_IS_UP_TO_DATE + ")" + values;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sql;
    }

    public String getUpdateSql(Object objectId) {
        String sql = null;
        String pkUpdate = "";

        if (!isPrimaryKeyAutoIncrement()) {
            pkUpdate = DBConstants.COL_ID + " = '" + id + "',";
        }

        try {
            sql = "UPDATE " + DBConstants.T_COURSE + " SET " + pkUpdate +
                    DBConstants.COL_COURSE_ID + " = " + courseID + "," +
                    DBConstants.COL_COURSE_CODE + " = " + prepareStringForSql(courseCode) + "," +
                    DBConstants.COL_NAME + " = " + prepareStringForSql(name) + "," +
                    DBConstants.COL_MIN_PEOPLE + " = " + minimumPeople + "," +
                    DBConstants.COL_MAX_PEOPLE + " = " + maximumPeople + "," +
                    DBConstants.COL_DESCRIPTION + " = " + prepareStringForSql(description) + "," +
                    DBConstants.COL_PREREQUISITES + " = " + prepareStringForSql(prerequisites) + "," +
                    DBConstants.COL_PROFESSIONAL_DOMAIN + " = " + prepareStringForSql(professionalDomain) + "," +
                    DBConstants.COL_SYLLABUS + " = " + prepareStringForSql(syllabus) + "," +
                    DBConstants.COL_DAYTIME + " = " + prepareStringForSql(dayTime) + "," +
                    DBConstants.COL_DURATION_IN_HOURS + " = " + durationInHours + "," +
                    DBConstants.COL_DURATION_IN_DAYS + " = " + durationInDays + "," +
                    DBConstants.COL_COMMENTS + " = " + prepareStringForSql(comments) + "," +
                    DBConstants.COL_PASSING_GRADE + " = " + passingGrade + "," +
                    DBConstants.COL_IS_MOOC + " = " + (isMooc ? 1 : 0) + "," +
                    DBConstants.COL_IS_MEETUP + " = " + (isMeetup ? 1 : 0) + "," +
                    DBConstants.COL_CATEGORY + " = " + prepareStringForSql(category) + "," +
                    DBConstants.COL_IMAGE_URL + " = " + prepareStringForSql(imageUrl) + "," +
                    DBConstants.COL_IS_UP_TO_DATE + " = 1" +
                    " WHERE " + DBConstants.COL_ID + " = '" + objectId + "';";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sql;
    }
}
