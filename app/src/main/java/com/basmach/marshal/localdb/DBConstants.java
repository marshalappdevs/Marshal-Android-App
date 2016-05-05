package com.basmach.marshal.localdb;

public class DBConstants {

    // Tables
    public static final String T_MATERIAL_ITEM = "t_material_item";
    public static final String T_COURSE = "t_course";
    public static final String T_CYCLE = "t_cycle";
    public static final String T_RATING = "t_rating";

    // Columns
    public static final String COL_ID = "id";
    public static final String COL_URL = "url";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_CANNONICIAL_URL = "cannonical_url";
    public static final String COL_IMAGE_URL = "image_url";
    public static final String COL_COURSE_ID = "course_id";
    public static final String COL_NAME = "name";
    public static final String COL_MIN_PEOPLE = "min_people";
    public static final String COL_MAX_PEOPLE = "max_people";
    public static final String COL_PREREQUISITES = "prerequisites";
    public static final String COL_TARGET_POPULATION = "target_population";
    public static final String COL_PROFESSIONAL_DOMAIN = "professional_domain";
    public static final String COL_SYLLABUS = "syllabus";
    public static final String COL_DAYTIME = "daytime";
    public static final String COL_DURATION_IN_HOURS = "duration_in_hours";
    public static final String COL_DURATION_IN_DAYS = "duration_in_days";
    public static final String COL_COMMENTS = "comments";
    public static final String COL_PASSING_GRADE = "passing_grade";
    public static final String COL_PRICE = "price";
    public static final String COL_CYCLES = "cycles";
    public static final String COL_IS_MOOC = "is_mooc";
    public static final String COL_START_DATE = "start_date";
    public static final String COL_END_DATE = "end_date";
    public static final String COL_TAGS = "tags";
    public static final String COL_USER_MAIL_ADDRESS = "user_mail_address";
    public static final String COL_RATING = "rating";
    public static final String COL_COMMENT = "comment";
    public static final String COL_COURSE_CODE = "course_code";
    public static final String COL_IS_GET_LINK_DATA_EXECUTED = "is_get_link_data_executed";
    public static final String COL_RATING_AVERAGE = "rating_average";
    public static final String COL_RATINGS_AMOUNT = "ratings_amount";
    public static final String COL_USER_RATING_ID = "user_rating_id";

    // 'Create Table' commands
    public static final String CREATE_T_MATERIAL_ITEM = "CREATE TABLE "+ T_MATERIAL_ITEM + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_URL + " TEXT UNIQUE, " +
            COL_TITLE + " TEXT, " +
            COL_DESCRIPTION + " TEXT," +
            COL_CANNONICIAL_URL + " TEXT," +
            COL_TAGS + " TEXT," +
            COL_IS_GET_LINK_DATA_EXECUTED + " INTEGER," +
            COL_IMAGE_URL + " TEXT);";

    public static final String CREATE_T_CYCLE = "CREATE TABLE "+ T_CYCLE + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME + " TEXT, " +
            COL_MAX_PEOPLE + " INTEGER," +
            COL_DESCRIPTION + " TEXT," +
            COL_START_DATE + " TEXT," +
            COL_END_DATE + " TEXT);";

    public static final String CREATE_T_RATING = "CREATE TABLE "+ T_RATING + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_USER_MAIL_ADDRESS + " TEXT, " +
            COL_COURSE_CODE + " TEXT, " +
            COL_RATING + " REAL," +
            COL_COMMENT + " TEXT);";

    public static final String CREATE_T_COURSE = "CREATE TABLE "+ T_COURSE + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_COURSE_ID + " TEXT UNIQUE, " +
            COL_NAME + " TEXT UNIQUE, " +
            COL_MIN_PEOPLE + " INTEGER, " +
            COL_MAX_PEOPLE + " INTEGER, " +
            COL_DESCRIPTION + " TEXT, " +
            COL_PREREQUISITES + " TEXT, " +
            COL_TARGET_POPULATION + " TEXT, " +
            COL_PROFESSIONAL_DOMAIN + " TEXT, " +
            COL_SYLLABUS + " TEXT, " +
            COL_DAYTIME + " TEXT, " +
            COL_DURATION_IN_HOURS + " INTEGER, " +
            COL_DURATION_IN_DAYS + " INTEGER, " +
            COL_COMMENTS + " TEXT, " +
            COL_PASSING_GRADE + " INTEGER, " +
            COL_PRICE + " REAL, " +
            COL_CYCLES + " TEXT, " +
            COL_IS_MOOC + " INTEGER, " +
            COL_RATING_AVERAGE + " REAL, " +
            COL_RATINGS_AMOUNT + " INTEGER, " +
            COL_USER_RATING_ID + " INTEGER, " +
            COL_IMAGE_URL + " TEXT);";

    // Database Drop command
    public static final String DROP_T_MATERIAL_ITEM = "DROP TABLE IF EXISTS " + T_MATERIAL_ITEM + ";";
    public static final String DROP_T_COURSE = "DROP TABLE IF EXISTS " + T_COURSE + ";";
    public static final String DROP_T_CYCLE = "DROP TABLE IF EXISTS " + T_CYCLE + ";";
    public static final String DROP_T_RATING = "DROP TABLE IF EXISTS " + T_RATING + ";";
}
