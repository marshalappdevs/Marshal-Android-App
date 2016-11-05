package com.basmapp.marshal.localdb;

public class DBConstants {

    // Tables
    public static final String T_MATERIAL_ITEM = "t_material_item";
    public static final String T_COURSE = "t_course";
    public static final String T_CYCLE = "t_cycle";
    public static final String T_RATING = "t_rating";
    public static final String T_MALSHAB_ITEM = "t_malshab_item";

    // Columns
    public static final String COL_ID = "id";
    public static final String COL_URL = "url";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_BASE_URL = "base_url";
    public static final String COL_IMAGE_URL = "image_url";
    public static final String COL_NAME = "name";
    public static final String COL_MIN_PEOPLE = "min_people";
    public static final String COL_MAX_PEOPLE = "max_people";
    public static final String COL_PREREQUISITES = "prerequisites";
    public static final String COL_PROFESSIONAL_DOMAIN = "professional_domain";
    public static final String COL_SYLLABUS = "syllabus";
    public static final String COL_DAYTIME = "daytime";
    public static final String COL_DURATION_IN_HOURS = "duration_in_hours";
    public static final String COL_DURATION_IN_DAYS = "duration_in_days";
    public static final String COL_COMMENTS = "comments";
    public static final String COL_PASSING_GRADE = "passing_grade";
    public static final String COL_IS_MOOC = "is_mooc";
    public static final String COL_START_DATE = "start_date";
    public static final String COL_END_DATE = "end_date";
    public static final String COL_TAGS = "tags";
    public static final String COL_USER_MAIL_ADDRESS = "user_mail_address";
    public static final String COL_RATING = "rating";
    public static final String COL_COMMENT = "comment";
    public static final String COL_COURSE_CODE = "course_code";
    public static final String COL_COURSE_ID = "course_id";
    public static final String COL_LAST_MODIFIED = "last_modified";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_CATEGORY = "category";
    public static final String COL_IS_MEETUP = "is_meetup";
    public static final String COL_IS_UP_TO_DATE = "is_up_to_date";
    public static final String COL_IS_USER_SUBSCRIBE = "is_user_subscribe";
    public static final String COL_ORDER = "col_order";
    public static final String COL_GOOGLE_FORM_URL = "col_google_form_url";

    // 'Create Table' commands
    static final String CREATE_T_MALSHAB_ITEM = "CREATE TABLE " + T_MALSHAB_ITEM + " (" +
            COL_ID + " INTEGER PRIMARY KEY, " +
            COL_URL + " TEXT UNIQUE, " +
            COL_TITLE + " TEXT, " +
            COL_IMAGE_URL + " TEXT, " +
            COL_ORDER + " INTEGER, " +
            COL_IS_UP_TO_DATE + " INTEGER);";

    static final String CREATE_T_MATERIAL_ITEM = "CREATE TABLE " + T_MATERIAL_ITEM + " (" +
            COL_ID + " INTEGER PRIMARY KEY, " +
            COL_URL + " TEXT UNIQUE, " +
            COL_TITLE + " TEXT, " +
            COL_DESCRIPTION + " TEXT," +
            COL_BASE_URL + " TEXT," +
            COL_TAGS + " TEXT," +
            COL_IMAGE_URL + " TEXT ," +
            COL_IS_UP_TO_DATE + " INTEGER);";

    static final String CREATE_T_RATING = "CREATE TABLE " + T_RATING + " (" +
            COL_ID + " INTEGER PRIMARY KEY, " +
            COL_USER_MAIL_ADDRESS + " TEXT, " +
            COL_COURSE_ID + " INTEGER, " +
            COL_RATING + " REAL," +
            COL_CREATED_AT + " INTEGER," +
            COL_LAST_MODIFIED + " INTEGER," +
            COL_COMMENT + " TEXT);";

    static final String CREATE_T_CYCLE = "CREATE TABLE " + T_CYCLE + " (" +
            COL_ID + " INTEGER PRIMARY KEY, " +
            COL_COURSE_ID + " INTEGER, " +
            COL_NAME + " TEXT, " +
            COL_MAX_PEOPLE + " INTEGER," +
            COL_DESCRIPTION + " TEXT," +
            COL_START_DATE + " INTEGER," +
            COL_END_DATE + " INTEGER);";

    static final String CREATE_T_COURSE = "CREATE TABLE " + T_COURSE + " (" +
            COL_ID + " TEXT PRIMARY KEY, " +
            COL_COURSE_ID + " INTEGER, " +
            COL_COURSE_CODE + " TEXT UNIQUE, " +
            COL_NAME + " TEXT UNIQUE, " +
            COL_MIN_PEOPLE + " INTEGER, " +
            COL_MAX_PEOPLE + " INTEGER, " +
            COL_DESCRIPTION + " TEXT, " +
            COL_PREREQUISITES + " TEXT, " +
            COL_PROFESSIONAL_DOMAIN + " TEXT, " +
            COL_SYLLABUS + " TEXT, " +
            COL_DAYTIME + " TEXT, " +
            COL_DURATION_IN_HOURS + " INTEGER, " +
            COL_DURATION_IN_DAYS + " INTEGER, " +
            COL_COMMENTS + " TEXT, " +
            COL_PASSING_GRADE + " INTEGER, " +
            COL_IS_MOOC + " INTEGER, " +
            COL_IS_MEETUP + " INTEGER, " +
            COL_CATEGORY + " TEXT, " +
            COL_IMAGE_URL + " TEXT," +
            COL_GOOGLE_FORM_URL + " TEXT," +
            COL_IS_USER_SUBSCRIBE + " INTEGER, " +
            COL_IS_UP_TO_DATE + " INTEGER);";

    // Database Drop command
    static final String DROP_T_MATERIAL_ITEM = "DROP TABLE IF EXISTS " + T_MATERIAL_ITEM + ";";
    static final String DROP_T_COURSE = "DROP TABLE IF EXISTS " + T_COURSE + ";";
    static final String DROP_T_CYCLE = "DROP TABLE IF EXISTS " + T_CYCLE + ";";
    static final String DROP_T_RATING = "DROP TABLE IF EXISTS " + T_RATING + ";";
    static final String DROP_T_MALSHAB_ITEM = "DROP TABLE IF EXISTS " + T_MALSHAB_ITEM + ";";

    /////////////////////////////////////////////////////////////////////////////////////////
    public static String getDeleteNotUpToDateStatement(String tableName) {
        return "DELETE FROM " + tableName +
                " WHERE " + DBConstants.COL_IS_UP_TO_DATE + " = 0;";
    }

    public static String getSetAllItemsNotUpToDateStatement(String tableName) {
        return "UPDATE " + tableName +
                " SET " + DBConstants.COL_IS_UP_TO_DATE + " = 0;";
    }
}
