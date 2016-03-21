package com.basmach.marshal.localdb;

/**
 * Created by Ido on 3/20/2016.
 */
public class DBConstants {

    // Tables
    public static final String T_MATERIAL_ITEM = "material_item";

    // Columns
    public static final String COL_ID = "id";
    public static final String COL_URL = "url";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_CANNONICIAL_URL = "connonical_url";
    public static final String COL_IMAGE_URL = "image_url";

    // 'Create Table' commands
    public static final String CREATE_MATERIAL_ITEM = "CREATE TABLE "+ T_MATERIAL_ITEM + " (" +
            COL_URL + " TEXT PRIMARY KEY, " +
            COL_TITLE + " TEXT UNIQUE, " +
            COL_DESCRIPTION + " TEXT" +
            COL_CANNONICIAL_URL + " TEXT" +
            COL_IMAGE_URL + " TEXT);";

    // Database Drop command
    public static final String DROP_T_MATERIAL_ITEM = "DROP TABLE IF EXISTS " + T_MATERIAL_ITEM + ";";
}
