package com.basmapp.marshal.services;

import android.app.IntentService;
import android.app.PendingIntent;
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
import com.basmapp.marshal.entities.MalshabItem;
import com.basmapp.marshal.entities.MaterialItem;
import com.basmapp.marshal.entities.Rating;
import com.basmapp.marshal.entities.Settings;
import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.LocalDBHelper;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.ui.utils.NotificationUtils;
import com.basmapp.marshal.util.AuthUtil;
import com.basmapp.marshal.util.MarshalServiceProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

            if (settings.getLastUpdateAt().compareTo(new Date(appLastUpdateTimeStamp)) > 0) {
                Log.i("CHECK FOR UPDATES", "NEED UPDATE -- " + settings.getLastUpdateAt().toString() + " | " + new Date(appLastUpdateTimeStamp).toString());
                sendCheckForUpdateResult(true);
                UpdateIntentService.startUpdateData(UpdateIntentService.this);
            } else {
                Log.i("CHECK FOR UPDATES", "NOT NEED UPDATE -- " + settings.getLastUpdateAt().toString() + " | " + new Date(appLastUpdateTimeStamp).toString());
                sendCheckForUpdateResult(false);
            }

            if (settings.getMinVersion() != 0 &&
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

        List<Course> newCourses = new ArrayList<>();
        List<MaterialItem> newMaterials;
        List<MalshabItem> newMalshabItems;
        try {
            newCourses = MarshalServiceProvider.getInstance(token).getAllCourses().execute().body();
            newMaterials = MarshalServiceProvider.getInstance(token).getAllMaterials().execute().body();
            newMalshabItems = MarshalServiceProvider.getInstance(token).getAllMalshabItems().execute().body();

            List<Course> tempNewCourses = new ArrayList<>(newCourses);
            List<MaterialItem> tempNewMaterials = new ArrayList<>(newMaterials);
            List<MalshabItem> tempNewMalshabItems = new ArrayList<>(newMalshabItems);

            database.beginTransaction();

            // Clear database
            database.execSQL("DELETE FROM " + DBConstants.T_CYCLE);
            database.execSQL("DELETE FROM " + DBConstants.T_RATING);

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
                    Log.e("UPDATE", "FAILED TO UPDATE MATERIAL_ITEM");
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
                    Log.e("UPDATE", "FAILED TO UPDATE MALSHAB_ITEM");
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
                        if (course.getCycles() != null && course.getCycles().size() > 0) {
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
                        if (course.getRatings() != null && course.getRatings().size() > 0) {
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
                    Log.e("UPDATE", "FAILED TO UPDATE COURSE_ITEM");
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
                if (course.getCycles() != null && course.getCycles().size() > 0) {
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
                if (course.getRatings() != null && course.getRatings().size() > 0) {
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

            Log.i("UPDATE_SERVICE", "result=" + String.valueOf(result));

            // Notify new courses
            if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.PREF_IS_FIRST_RUN, true)) {
                if (newCourses.size() > 0) {
                    notifyNewCourses(newCourses);
                }
            }
        }
    }

    private void notifyNewCourses(List<Course> newCourses) {
        List<Course> courses = getCoursesMatchUserChannels(newCourses);

        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        String message = "New " + courses.get(0).getName() + " course!";
        new NotificationUtils.GeneratePictureStyleNotification(this, message,
                courses.get(0).getImageUrl(), notifyPendingIntent).execute();
        if (courses.size() > 1) {
            String restCoursesMessage = "We have " + (courses.size()) + " new courses!\n" +
                    "Tap and take a look!";
            new NotificationUtils(this).notify(restCoursesMessage, notifyPendingIntent);
        }
    }

    private List<Course> getCoursesMatchUserChannels(List<Course> newCourses) {
        Set<String> fcmChannels = PreferenceManager.getDefaultSharedPreferences(this)
                .getStringSet(Constants.PREF_GCM_CHANNELS, null);
        if (fcmChannels == null) return newCourses;
        else {
            List<Course> filteredCourses = new ArrayList<>();
            for (Course course : newCourses) {
                if (fcmChannels.contains(course.getCategory()))
                    filteredCourses.add(course);
            }

            return filteredCourses;
        }
    }

    public void publishProgress(int progress) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_UPDATE_DATA_PROGRESS_CHANGED);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(Constants.EXTRA_PROGRESS_PERCENT, progress);
        sendBroadcast(broadcastIntent);
    }

}
