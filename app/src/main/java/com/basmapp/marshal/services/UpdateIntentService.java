package com.basmapp.marshal.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.preference.PreferenceManager;
import android.util.Log;

import com.basmapp.marshal.ApplicationMarshal;
import com.basmapp.marshal.BuildConfig;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.entities.GcmRegistration;
import com.basmapp.marshal.entities.MalshabItem;
import com.basmapp.marshal.entities.MaterialItem;
import com.basmapp.marshal.entities.Rating;
import com.basmapp.marshal.entities.Settings;
import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.LocalDBHelper;
import com.basmapp.marshal.util.AuthUtil;
import com.basmapp.marshal.util.MarshalServiceProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UpdateIntentService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_CHECK_FOR_UPDATE = "com.basmapp.marshal.utils.action.CHECK_FOR_UPDATE";
    public static final String ACTION_UPDATE_DATA = "com.basmapp.marshal.utils.action.UPDATE_DATA";
    public static final String ACTION_UPDATE_DATA_PROGRESS_CHANGED = "com.basmapp.marshal.utils.action.UPDATE_DATA_PROGRESS_CHANGED";

    public static final String RESULT_CHECK_FOR_UPDATE = "result_check_for_update";
    public static final String RESULT_UPDATE_DATA = "result_update_data";

    private static final String LOG_TAG = "UPDATE_SERVICE";

    public static boolean isRunning = false;

    private static String token = null;

    public UpdateIntentService() {
        super("UpdateIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startCheckForUpdate(Context context) {
        Log.i("LIFE_CYCLE", "UpdateService - startCheckForUpdate");
        isRunning = true;
        Intent intent = new Intent(context, UpdateIntentService.class);
        intent.setAction(ACTION_CHECK_FOR_UPDATE);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startUpdateData(Context context) {
        Intent intent = new Intent(context, UpdateIntentService.class);
        intent.setAction(ACTION_UPDATE_DATA);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_FOR_UPDATE.equals(action)) {
                handleActionCheckForUpdate();
            } else if (ACTION_UPDATE_DATA.equals(action)) {
                handleActionUpdateData();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCheckForUpdate() {
        try {
            token = AuthUtil.getApiToken();
            Settings settings = MarshalServiceProvider.getInstance(token).getSettings().execute().body();

            long appLastUpdateTimeStamp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getLong(Constants.PREF_LAST_UPDATE_TIMESTAMP, 0);

            if(settings.getLastUpdateAt().compareTo(new Date(appLastUpdateTimeStamp)) > 0) {
                Log.i("CHECK FOR UPDATES", "NEED UPDATE -- " + settings.getLastUpdateAt().toString() + " | " + new Date(appLastUpdateTimeStamp).toString());
                sendCheckForUpdateResult(true);
                UpdateIntentService.startUpdateData(UpdateIntentService.this);
            } else {
                Log.i("CHECK FOR UPDATES", "NOT NEED UPDATE -- " + settings.getLastUpdateAt().toString() + " | " + new Date(appLastUpdateTimeStamp).toString());
                sendCheckForUpdateResult(false);
            }

            if(settings.getMinVersion() != 0 &&
                    BuildConfig.VERSION_CODE < settings.getMinVersion()) {
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit().putBoolean(Constants.PREF_MUST_UPDATE, true).apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendCheckForUpdateResult(false);
        }
    }

//    private boolean authenticate() {
//        AuthRequest authRequest = new AuthRequest();
//        try {
//            Response<String> authResponse = MarshalServiceProvider.getInstance(null).auth(authRequest).execute();
//            if (authResponse.isSuccessful()) {
//                token = authResponse.body();
//                Log.i("AUTH", token);
//                return true;
//            } else {
//                Log.e("AUTH", " RESPONSE ERROR");
//                return false;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e("AUTH", " FAILED");
//            return false;
//        }
//    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void sendCheckForUpdateResult(boolean result) {
        isRunning = result;
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_CHECK_FOR_UPDATE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RESULT_CHECK_FOR_UPDATE, result);
        sendBroadcast(broadcastIntent);
    }

    private void handleActionUpdateData() {
        try {
            if (token == null || token.equals("")) {
                token = AuthUtil.getApiToken();
            }

            updateData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateData() {

        isRunning = true;
        boolean result = false;
        SQLiteDatabase database = LocalDBHelper.getDatabaseWritableInstance(this);

        try {
            List<Course> newCourses = MarshalServiceProvider.getInstance(token).getAllCourses().execute().body();
            List<MaterialItem> newMaterials = MarshalServiceProvider.getInstance(token).getAllMaterials().execute().body();
//            List<Rating> newRatings = MarshalServiceProvider.getInstance(token).getAllRatings().execute().body();
            List<MalshabItem> newMalshabItems = MarshalServiceProvider.getInstance(token).getAllMalshabItems().execute().body();

            List<Course> tempNewCourses = new ArrayList<>(newCourses);
            List<MaterialItem> tempNewMaterials = new ArrayList<>(newMaterials);
//            List<Rating> tempNewRatings = new ArrayList<>(newRatings);
            List<MalshabItem> tempNewMalshabItems = new ArrayList<>(newMalshabItems);

            database.beginTransaction();

            // Clear database
            database.execSQL("DELETE FROM " + DBConstants.T_CYCLE);
            database.execSQL("DELETE FROM " + DBConstants.T_RATING);
//            database.execSQL("DELETE FROM " + DBConstants.T_COURSE);

            //////////////////////// Insert Materials /////////////////////////////////

            // Set all objects to be NOT Up To Date
            database.compileStatement(DBConstants.getSetAllItemsNotUpToDateStatement(DBConstants.T_MATERIAL_ITEM))
                    .executeUpdateDelete();

            // Update
            for (MaterialItem materialItem : tempNewMaterials) {
                List<MaterialItem> dbResult = null;
                try {
                    dbResult = (List) MaterialItem.getAllByColumn(DBConstants.COL_URL, materialItem.getUrl(),
                            DBConstants.COL_ID, this, MaterialItem.class);

                    if (dbResult != null && dbResult.size() > 0) {
                        database.compileStatement(materialItem.getUpdateSql(dbResult.get(0).getId())).execute();
                        newMaterials.remove(materialItem);
                    }
                } catch (Exception e) {
                    Log.e("UPDATE","FAILED TO UPDATE MATERIAL_ITEM");
                    e.printStackTrace();
                }
            }

            // Delete
            database.compileStatement(DBConstants.getDeleteNotUpToDateStatement(DBConstants.T_MATERIAL_ITEM))
                    .executeUpdateDelete();

            // Insert
            for (MaterialItem materialItem : newMaterials) {
                long id = database.compileStatement(materialItem.getInsertSql()).executeInsert();
//                Log.i("UPDATE", "MATERIAL_ITEM_ID : " + id);
            }

            //////////////////////// Insert MalshabItems /////////////////////////////////

            // Set all objects to be NOT Up To Date
            database.compileStatement(DBConstants.getSetAllItemsNotUpToDateStatement(DBConstants.T_MALSHAB_ITEM))
                    .executeUpdateDelete();

            // Update
            for (MalshabItem malshabItem : tempNewMalshabItems) {
                List<MalshabItem> dbResult = null;
                try {
                    dbResult = (List) MalshabItem.getAllByColumn(DBConstants.COL_URL, malshabItem.getUrl(),
                            DBConstants.COL_ID, this, MalshabItem.class);

                    if (dbResult != null && dbResult.size() > 0) {
                        database.compileStatement(malshabItem.getUpdateSql(dbResult.get(0).getId())).execute();
                        newMalshabItems.remove(malshabItem);
                    }
                } catch (Exception e) {
                    Log.e("UPDATE","FAILED TO UPDATE MALSHAB_ITEM");
                    e.printStackTrace();
                }
            }

            // Delete
            database.compileStatement(DBConstants.getDeleteNotUpToDateStatement(DBConstants.T_MALSHAB_ITEM))
                    .executeUpdateDelete();

            // Insert
            for (MalshabItem malshabItem : newMalshabItems) {
                long id = database.compileStatement(malshabItem.getInsertSql()).executeInsert();
//                Log.i("UPDATE", "MALSHAB_ITEM_ID : " + id);
            }

//            //////////////////////// Insert Ratings /////////////////////////////////
//
//            // Set all objects to be NOT Up To Date
//            database.compileStatement(DBConstants.getSetAllItemsNotUpToDateStatement(DBConstants.T_RATING))
//                    .executeUpdateDelete();
//
//            // Update
//            try {
//                List<Rating> dbResult = null;
//                dbResult = (List) Rating.getAll(DBConstants.COL_ID, this, Rating.class);
//                int position = 0;
//                if (dbResult != null && dbResult.size() > 0) {
//                    for (Rating rating : tempNewRatings) {
//                        database.compileStatement(rating.getUpdateSql(dbResult.get(position).getId())).execute();
//                        position++;
//                        newRatings.remove(rating);
//                    }
//                }
//            } catch (Exception e) {
//                Log.e("UPDATE","FAILED TO UPDATE RATING_ITEM");
//                e.printStackTrace();
//            }
//
//            // Delete
//            database.compileStatement(DBConstants.getDeleteNotUpToDateStatement(DBConstants.T_RATING))
//                    .executeUpdateDelete();
//
//            // Insert
//            for (Rating rating : newRatings) {
//                long id = database.compileStatement(rating.getInsertSql()).executeInsert();
////                Log.i("UPDATE", "RATING_ITEM_ID : " + id);
//            }

            //////////////////////// Insert Courses  /////////////////////////////////

            int cycleId = 1;
            int ratingId = 1;

            // Set all Courses to be NOT Up To Date
            database.compileStatement(DBConstants.getSetAllItemsNotUpToDateStatement(DBConstants.T_COURSE))
                    .executeUpdateDelete();

            // Update
            for (Course course : tempNewCourses) {
                List<Course> dbResult = null;
                try {
                    dbResult = (List) Course.getAllByColumn(DBConstants.COL_COURSE_CODE, course.getCourseCode(),
                            DBConstants.COL_ID, this, Course.class);

                    if (dbResult != null && dbResult.size() > 0) {
                        //TODO: Take care to the course Cycles
                        /////////////////////////// CYCLES //////////////////////////////
                        if(course.getCycles() != null && course.getCycles().size() > 0) {
                            //////////////////////// Insert Course Cycles  /////////////////////////////////
                            String cycleSql = "INSERT INTO " + DBConstants.T_CYCLE + " VALUES " +
                                    "(?,?,?,?,?,?,?);";

                            SQLiteStatement cycleStatement = database.compileStatement(cycleSql);

                            for (int cycleIndex = 0; cycleIndex < course.getCycles().size(); cycleIndex++) {
                                Cycle cycle = course.getCycles().get(cycleIndex);

                                SQLiteStatement currCycleStatement =
                                        cycle.getStatement(cycleStatement, course.getCourseCode(), cycleId);
                                if (currCycleStatement != null) {
                                    long insertCycleId = currCycleStatement.executeInsert();
                                    if (insertCycleId == -1)
                                        throw new Exception("Failed to insert cycle");
                                    cycle.setId(insertCycleId);
                                    cycleId++;
                                } else {
                                    course.getCycles().remove(cycleIndex);
                                    cycleIndex--;
                                }
                            }
                        }
                        /////////////////////////// END CYCLES //////////////////////////////
                        //TODO: Take care to the course Ratings
                        /////////////////////////// RATINGS //////////////////////////////
                        if(course.getRatings() != null && course.getRatings().size() > 0) {
                            //////////////////////// Insert Course Ratings  /////////////////////////////////
                            String ratingSql = "INSERT INTO " + DBConstants.T_RATING + " VALUES " +
                                    "(?,?,?,?,?,?,?);";

                            SQLiteStatement ratingStatement = database.compileStatement(ratingSql);

                            for (int ratingIndex = 0; ratingIndex < course.getRatings().size(); ratingIndex++) {
                                Rating rating = course.getRatings().get(ratingIndex);
                                rating.setCourseCode(course.getCourseCode());
                                SQLiteStatement currRatingStatement =
                                        rating.getStatement(ratingStatement, ratingId, course.getCourseCode());
                                if (currRatingStatement != null) {
                                    long insertRatingId = currRatingStatement.executeInsert();
                                    if (insertRatingId == -1)
                                        throw new Exception("Failed to insert rating");
                                    rating.setId(insertRatingId);
                                    ratingId++;
                                } else {
                                    course.getRatings().remove(ratingIndex);
                                    ratingIndex--;
                                }
                            }
                        }
                        /////////////////////////// END RATINGS //////////////////////////////
                        database.compileStatement(course.getUpdateSql(dbResult.get(0).getId())).execute();
                        newCourses.remove(course);
                    }
                } catch (Exception e) {
                    Log.e("UPDATE","FAILED TO UPDATE COURSE_ITEM");
                    e.printStackTrace();
                }
            }

            // Delete
            database.compileStatement(DBConstants.getDeleteNotUpToDateStatement(DBConstants.T_COURSE))
                    .executeUpdateDelete();

            // Insert
            for (Course course : newCourses) {
                //TODO: Take care to the course Cycles
                /////////////////////////// CYCLES //////////////////////////////
                if(course.getCycles() != null && course.getCycles().size() > 0) {
                    //////////////////////// Insert Course Cycles  /////////////////////////////////
                    String cycleSql = "INSERT INTO " + DBConstants.T_CYCLE + " VALUES " +
                            "(?,?,?,?,?,?,?);";

                    SQLiteStatement cycleStatement = database.compileStatement(cycleSql);

                    for (int cycleIndex = 0; cycleIndex < course.getCycles().size(); cycleIndex++) {
                        Cycle cycle = course.getCycles().get(cycleIndex);

                        SQLiteStatement currCycleStatement =
                                cycle.getStatement(cycleStatement, course.getCourseCode(), cycleId);
                        if (currCycleStatement != null) {
                            long insertCycleId = currCycleStatement.executeInsert();
                            if (insertCycleId == -1)
                                throw new Exception("Failed to insert cycle");
                            cycle.setId(insertCycleId);
                            cycleId++;
                        } else {
                            course.getCycles().remove(cycleIndex);
                            cycleIndex--;
                        }
                    }
                }
                /////////////////////////// END CYCLES //////////////////////////////
                //TODO: Take care to the course Ratings
                /////////////////////////// RATINGS //////////////////////////////
                if(course.getRatings() != null && course.getRatings().size() > 0) {
                    //////////////////////// Insert Course Ratings  /////////////////////////////////
                    String ratingSql = "INSERT INTO " + DBConstants.T_RATING + " VALUES " +
                            "(?,?,?,?,?,?,?);";

                    SQLiteStatement ratingStatement = database.compileStatement(ratingSql);

                    for (int ratingIndex = 0; ratingIndex < course.getRatings().size(); ratingIndex++) {
                        Rating rating = course.getRatings().get(ratingIndex);
                        rating.setCourseCode(course.getCourseCode());
                        SQLiteStatement currRatingStatement =
                                rating.getStatement(ratingStatement, ratingId, course.getCourseCode());
                        if (currRatingStatement != null) {
                            long insertRatingId = currRatingStatement.executeInsert();
                            if (insertRatingId == -1)
                                throw new Exception("Failed to insert rating");
                            rating.setId(insertRatingId);
                            ratingId++;
                        } else {
                            course.getRatings().remove(ratingIndex);
                            ratingIndex--;
                        }
                    }
                }
                /////////////////////////// END RATINGS //////////////////////////////
                long id = database.compileStatement(course.getInsertSql()).executeInsert();
//                Log.i("UPDATE", "COURSE_ITEM_ID : " + id);
            }

            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.PREF_IS_UPDATE_SERVICE_SUCCESS_ONCE, true).apply();
            ApplicationMarshal.setLastUpdatedNow(this);
            database.setTransactionSuccessful();

            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (database.inTransaction()) {
                database.endTransaction();
            }

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_UPDATE_DATA);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(RESULT_UPDATE_DATA, result);
            sendBroadcast(broadcastIntent);

            isRunning = false;

            Log.i("UPDATE_SERVICE","result=" + String.valueOf(result));
        }
    }

    private void updateDataOld() {

//        isRunning = true;
//        boolean result = false;
//        SQLiteDatabase database = LocalDBHelper.getDatabaseWritableInstance(this);
//
//        try {
//            List<Course> newCourses = MarshalServiceProvider.getInstance(token).getAllCourses().execute().body();
//            List<MaterialItem> newMaterials = MarshalServiceProvider.getInstance(token).getAllMaterials().execute().body();
//            List<Rating> newRatings = MarshalServiceProvider.getInstance(token).getAllRatings().execute().body();
//            List<MalshabItem> newMalshabItems = MarshalServiceProvider.getInstance(token).getAllMalshabItems().execute().body();
//
//            database.beginTransaction();
//
//            // Clear database
//            database.execSQL("DELETE FROM " + DBConstants.T_CYCLE);
//            database.execSQL("DELETE FROM " + DBConstants.T_RATING);
//            database.execSQL("DELETE FROM " + DBConstants.T_COURSE);
//            database.execSQL("DELETE FROM " + DBConstants.T_MATERIAL_ITEM);
//            database.execSQL("DELETE FROM " + DBConstants.T_MALSHAB_ITEM);
//
//            String sql;
//            SQLiteStatement statement;
//
//            //////////////////////// Insert Materials /////////////////////////////////
//
//            sql = "INSERT INTO " + DBConstants.T_MATERIAL_ITEM + " VALUES " +
//                    "(?,?,?,?,?,?,?);";
//            statement = database.compileStatement(sql);
//            for (int index = 0; index < newMaterials.size(); index++) {
//                MaterialItem currItem = newMaterials.get(index);
//                SQLiteStatement currStatement = currItem.getStatement(statement, index + 1);
//                if (currStatement != null) {
//                    long insertId = currStatement.executeInsert();
//                    if (insertId == -1)
//                        throw new Exception("Failed to insert material item ---> " + currItem.getUrl());
//                }
//            }
//
//            //////////////////////// Insert MalshabItems /////////////////////////////////
//
//            sql = "INSERT INTO " + DBConstants.T_MALSHAB_ITEM + " VALUES " +
//                    "(?,?,?,?);";
//            statement = database.compileStatement(sql);
//            for (int index = 0; index < newMalshabItems.size(); index++) {
//                MalshabItem currItem = newMalshabItems.get(index);
//                SQLiteStatement currStatement = currItem.getStatement(statement, index + 1);
//                if (currStatement != null) {
//                    long insertId = currStatement.executeInsert();
//                    if (insertId == -1)
//                        throw new Exception("Failed to insert malshab item ---> " + currItem.getUrl());
//                }
//            }
//
//            //////////////////////// Insert Ratings /////////////////////////////////
//
//            sql = "INSERT INTO " + DBConstants.T_RATING + " VALUES " +
//                    "(?,?,?,?,?,?,?);";
//            statement = database.compileStatement(sql);
//            for (int index = 0; index < newRatings.size(); index++) {
//                Rating currItem = newRatings.get(index);
//                SQLiteStatement currStatement = currItem.getStatement(statement, index + 1);
//                if (currStatement != null) {
//                    long insertId = currStatement.executeInsert();
////                    if (insertId == -1)
////                        throw new Exception("Failed to insert rating");
//                }
//            }
//
//            //////////////////////// Insert Courses  /////////////////////////////////
//
//            sql = "INSERT INTO " + DBConstants.T_COURSE + " VALUES " +
//                    "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
//
//            statement = database.compileStatement(sql);
//
//            int cycleId = 1;
//
//            for (int index = 0; index < newCourses.size(); index++) {
//                Course course = newCourses.get(index);
//
//                if(course.getCycles() != null && course.getCycles().size() > 0) {
//                    //////////////////////// Insert Course Cycles  /////////////////////////////////
//                    String cycleSql = "INSERT INTO " + DBConstants.T_CYCLE + " VALUES " +
//                            "(?,?,?,?,?,?,?);";
//
//                    SQLiteStatement cycleStatement = database.compileStatement(cycleSql);
//
//                    for (int cycleIndex = 0; cycleIndex < course.getCycles().size(); cycleIndex++) {
//                        Cycle cycle = course.getCycles().get(cycleIndex);
//
//                        SQLiteStatement currCycleStatement =
//                                cycle.getStatement(cycleStatement, course.getCourseCode(), cycleId);
//                        if (currCycleStatement != null) {
//                            long insertCycleId = currCycleStatement.executeInsert();
//                            if (insertCycleId == -1)
//                                throw new Exception("Failed to insert cycle");
//                            cycle.setId(insertCycleId);
//                            cycleId++;
//                        } else {
//                            course.getCycles().remove(cycleIndex);
//                            cycleIndex--;
//                        }
//                    }
//                }
//
//                SQLiteStatement currStatement = course.getStatement(statement, index + 1);
//                if (currStatement != null) {
//                    long insertId = currStatement.executeInsert();
//                    if (insertId == -1)
//                        throw new Exception("Failed to insert course " + course.getCourseCode());
//                }
//            }
//
//            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.PREF_IS_UPDATE_SERVICE_SUCCESS_ONCE, true).apply();
//            ApplicationMarshal.setLastUpdatedNow(this);
//            database.setTransactionSuccessful();
//
//            result = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            result = false;
//        } finally {
//            if (database.inTransaction()) {
//                database.endTransaction();
//            }
//
//            Intent broadcastIntent = new Intent();
//            broadcastIntent.setAction(ACTION_UPDATE_DATA);
//            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
//            broadcastIntent.putExtra(RESULT_UPDATE_DATA, result);
//            sendBroadcast(broadcastIntent);
//
//            isRunning = false;
//
//            Log.i("UPDATE_SERVICE","result=" + String.valueOf(result));
//        }
    }

    private void updateDataNoTransaction() {
        //        boolean proccess_result = false;
//
//        int itemPercentWeight = 0;
//        int progressPercents = 0;
//
//        try {
//            List<Cycle> currentCycles = (List) Cycle.getAll(DBConstants.COL_ID, UpdateIntentService.this,
//                    Cycle.class);
//            List<Course> currentCourses = (List) Course.getAll(DBConstants.COL_ID, UpdateIntentService.this,
//                    Course.class);
//            List<MaterialItem> currentMaterials = (List) MaterialItem.getAll(DBConstants.COL_URL, UpdateIntentService.this,
//                    MaterialItem.class);
//            List<Rating> currentRatings = (List) Rating.getAll(DBConstants.COL_COURSE_CODE, UpdateIntentService.this,
//                    Rating.class);
//            List<MalshabItem> currentMalshabItems = (List) MalshabItem.getAll(DBConstants.COL_TITLE, UpdateIntentService.this,
//                    MalshabItem.class);
//
//            List<Course> newCourses = MarshalServiceProvider.getInstance().getAllCourses().execute().body();
//            List<MaterialItem> newMaterials = MarshalServiceProvider.getInstance().getAllMaterials().execute().body();
//            List<Rating> newRatings = MarshalServiceProvider.getInstance().getAllRatings().execute().body();
//            List<MalshabItem> newMalshabItems = MarshalServiceProvider.getInstance().getAllMalshabItems().execute().body();
//
//            itemPercentWeight = 100 / (currentCourses.size() + currentCycles.size() +
//                    currentMaterials.size() + currentRatings.size() +
//                    newCourses.size() + newMaterials.size() + newRatings.size() + newMalshabItems.size());
//
//            for (MalshabItem malshabItem : currentMalshabItems) {
//                try {
//                    malshabItem.delete();
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, "malshabItem delete failed");
//                    e.printStackTrace();
//                }
//
//                progressPercents += itemPercentWeight;
//                publishProgress(progressPercents);
//            }
//
//            for (MaterialItem materialItem : currentMaterials) {
//                try {
//                    materialItem.delete();
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, "materialItem delete failed");
//                    e.printStackTrace();
//                }
//
//                progressPercents += itemPercentWeight;
//                publishProgress(progressPercents);
//            }
//
//            for (Rating rating : currentRatings) {
//                try {
//                    rating.delete();
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, "rating delete failed");
//                    e.printStackTrace();
//                }
//
//                progressPercents += itemPercentWeight;
//                publishProgress(progressPercents);
//            }
//
//            for (Course course : currentCourses) {
//                try {
//                    course.delete();
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, "course delete failed");
//                    e.printStackTrace();
//                }
//
//                progressPercents += itemPercentWeight;
//                publishProgress(progressPercents);
//            }
//
//            Log.i(LOG_TAG, "old courses deleted successfully");
//
//            for (Cycle cycle : currentCycles) {
//                try {
//                    cycle.delete();
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, "cycle delete failed");
//                    e.printStackTrace();
//                }
//
//                progressPercents += itemPercentWeight;
//                publishProgress(progressPercents);
//            }
//
//            Log.i(LOG_TAG, "old cycles deleted successfully");
//
//            for (Course course : newCourses) {
//                if (course.getCycles() != null) {
//
//                    int listLength = course.getCycles().size();
//
//                    for (int position = 0; position < listLength; position++) {
//                        try {
//                            Cycle currentCycle = course.getCycles().get(position);
//
//                            if (currentCycle.getStartDate().compareTo(new Date()) > 0) {
//                                currentCycle.Ctor(UpdateIntentService.this);
//                                currentCycle.create();
//                            } else {
//                                course.getCycles().remove(currentCycle);
//                                listLength = course.getCycles().size();
//                                position--;
//                            }
//                        } catch (Exception e) {
//                            Log.e(LOG_TAG, "cycle creation failed");
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                try {
//                    course.Ctor(UpdateIntentService.this);
//                    course.setImageUrl(MarshalServiceProvider.IMAGES_URL + course.getCourseCode());
//                    course.create();
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, "course creation failed");
//                    e.printStackTrace();
//                }
//
//                progressPercents += itemPercentWeight;
//                publishProgress(progressPercents);
//            }
//
//            Log.i(LOG_TAG, "new courses created successfully");
//
//            for (final MaterialItem materialItem : newMaterials) {
//
//                try {
//                    materialItem.Ctor(UpdateIntentService.this);
//                    materialItem.create();
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, "rating creation failed");
//                    e.printStackTrace();
//                }
//
//                progressPercents += itemPercentWeight;
//                publishProgress(progressPercents);
//            }
//
//            Log.i(LOG_TAG, "new materials created successfully");
//
//            for (final Rating rating : newRatings) {
//
//                try {
//                    rating.Ctor(UpdateIntentService.this);
//                    rating.create();
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, "rating creation failed");
//                    e.printStackTrace();
//                }
//
//                progressPercents += itemPercentWeight;
//                publishProgress(progressPercents);
//            }
//
//            Log.i(LOG_TAG, "new ratings created successfully");
//
//            for (MalshabItem malshabItem : newMalshabItems) {
//
//                try {
//                    malshabItem.Ctor(UpdateIntentService.this);
//                    malshabItem.create();
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, "malshabItem creation failed");
//                    e.printStackTrace();
//                }
//
//                progressPercents += itemPercentWeight;
//                publishProgress(progressPercents);
//            }
//
//            Log.i(LOG_TAG, "new malshabItem created successfully");
//
//            proccess_result = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if(proccess_result) {
//            ApplicationMarshal.setLastUpdatedNow(this);
//        }
//
//        try {
//            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.PREF_IS_UPDATE_SERVICE_SUCCESS_ONCE, true).apply();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction(ACTION_UPDATE_DATA);
//        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
//        broadcastIntent.putExtra(RESULT_UPDATE_DATA, proccess_result);
//        sendBroadcast(broadcastIntent);
    }

    public void publishProgress(int progress) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_UPDATE_DATA_PROGRESS_CHANGED);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(Constants.EXTRA_PROGRESS_PERCENT, progress);
        sendBroadcast(broadcastIntent);
    }

}
