package com.basmapp.marshal.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.basmapp.marshal.ApplicationMarshal;
import com.basmapp.marshal.BuildConfig;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.entities.FaqItem;
import com.basmapp.marshal.entities.MalshabItem;
import com.basmapp.marshal.entities.MaterialItem;
import com.basmapp.marshal.entities.Rating;
import com.basmapp.marshal.entities.Settings;
import com.basmapp.marshal.ui.CourseActivity;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.util.NotificationUtils;
import com.basmapp.marshal.util.AuthUtil;
import com.basmapp.marshal.util.MarshalServiceProvider;
import com.simplite.orm.DatabaseHelper;
import com.simplite.orm.entities.Condition;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
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

    public static boolean isRunning = false;

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
            String jwt = AuthUtil.getApiToken();
            Settings settings = MarshalServiceProvider.getInstance(jwt).getSettings().execute().body();

            // Fetch channels
            Set<String> channels = new HashSet<>(settings.getChannels());
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putStringSet(Constants.PREF_FCM_CHANNELS_ENTRIES, channels).apply();

            // Fetch categories
            Set<String> categories = new HashSet<>(settings.getCategories());
            PreferenceManager.getDefaultSharedPreferences(this).edit().
                    putStringSet(Constants.PREF_CATEGORIES, categories).apply();

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
        updateData();
    }

    private void updateData() {

        isRunning = true;
        boolean result = false;
        SQLiteDatabase database = DatabaseHelper.getWritableDatabaseInstance(this);

        List<Course> newCourses = new ArrayList<>();
        List<MaterialItem> newMaterials;
        List<MalshabItem> newMalshabItems;
        List<FaqItem> newFaqItems;

        database.beginTransaction();
        try {
            String jwt = AuthUtil.getApiToken();
            newCourses = MarshalServiceProvider.getInstance(jwt).getAllCourses().execute().body();
            newMaterials = MarshalServiceProvider.getInstance(jwt).getAllMaterials().execute().body();
            newMalshabItems = MarshalServiceProvider.getInstance(jwt).getAllMalshabItems().execute().body();
            newFaqItems = MarshalServiceProvider.getInstance(jwt).getAllFaqItems().execute().body();

            List<Course> tempNewCourses = new ArrayList<>(newCourses);
            List<MaterialItem> tempNewMaterials = new ArrayList<>(newMaterials);
            List<MalshabItem> tempNewMalshabItems = new ArrayList<>(newMalshabItems);
            List<FaqItem> tempNewFaqItems = new ArrayList<>(newFaqItems);

            Condition conditionSetNotUpToDate = new Condition();
            conditionSetNotUpToDate.setOperator(Condition.Operator.EQUAL);
            conditionSetNotUpToDate.setValue(false);

            // Clear database
            database.execSQL("DELETE FROM " + Cycle.TABLE_NAME);
            database.execSQL("DELETE FROM " + Rating.TABLE_NAME);

            //////////////////////// Insert FAQ /////////////////////////////////

            // Set all objects to be NOT Up To Date
            conditionSetNotUpToDate.setColumn(FaqItem.COL_IS_UP_TO_DATE);
            database.compileStatement(FaqItem.getUpdateCommand(FaqItem.TABLE_NAME,
                    new Condition[]{conditionSetNotUpToDate}, null))
                    .executeUpdateDelete();

            // Update
            for (FaqItem faqItem : tempNewFaqItems) {
                FaqItem dbResult;
                try {
                    dbResult = (FaqItem) FaqItem.findOne(FaqItem.COL_QUESTION, faqItem.getQuestion(), this, FaqItem.class);

                    if (dbResult != null && dbResult.getId() != null) {
                        newFaqItems.remove(faqItem);
                        faqItem.setIsUpToDate(true);
                        database.compileStatement(faqItem.getUpdateCommand(this, null, null)).execute();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Delete
            database.compileStatement(FaqItem.getDeleteCommand(FaqItem.TABLE_NAME,
                    new Condition[]{new Condition(FaqItem.COL_IS_UP_TO_DATE, false, Condition.Operator.EQUAL)}))
                    .executeUpdateDelete();

            // Insert
            for (FaqItem faqItem : newFaqItems) {
                database.compileStatement(faqItem.getInsertCommand(this)).executeInsert();
            }

            //////////////////////// Insert Materials /////////////////////////////////

            // Set all objects to be NOT Up To Date
            conditionSetNotUpToDate.setColumn(MaterialItem.COL_IS_UP_TO_DATE);
            database.compileStatement(MaterialItem.getUpdateCommand(MaterialItem.TABLE_NAME,
                    new Condition[]{conditionSetNotUpToDate}, null))
                    .executeUpdateDelete();

            // Update
            for (MaterialItem materialItem : tempNewMaterials) {
                MaterialItem dbResult;
                try {
                    dbResult = (MaterialItem) MaterialItem.findOne(MaterialItem.COL_URL, materialItem.getUrl(),
                            this, MaterialItem.class);

                    if (dbResult != null && dbResult.getId() != null) {
                        newMaterials.remove(materialItem);
                        materialItem.setIsUpToDate(true);
                        database.compileStatement(materialItem.getUpdateCommand(this, null, null)).execute();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Delete
            database.compileStatement(MaterialItem.getDeleteCommand(MaterialItem.TABLE_NAME,
                    new Condition[]{new Condition(MaterialItem.COL_IS_UP_TO_DATE, false, Condition.Operator.EQUAL)}))
                    .executeUpdateDelete();

            // Insert
            for (MaterialItem materialItem : newMaterials) {
                database.compileStatement(materialItem.getInsertCommand(this)).executeInsert();
            }

            //////////////////////// Insert MalshabItems /////////////////////////////////

            // Set all objects to be NOT Up To Date
            conditionSetNotUpToDate.setColumn(MalshabItem.COL_IS_UP_TO_DATE);
            database.compileStatement(MalshabItem.getUpdateCommand(MalshabItem.TABLE_NAME,
                    new Condition[]{conditionSetNotUpToDate}, null))
                    .executeUpdateDelete();

            // Update
            for (MalshabItem malshabItem : tempNewMalshabItems) {
                MalshabItem dbResult;
                try {
                    dbResult = (MalshabItem) MalshabItem.findOne(MalshabItem.COL_URL, malshabItem.getUrl(),
                            this, MalshabItem.class);

                    if (dbResult != null && dbResult.getId() != null) {
                        newMalshabItems.remove(malshabItem);
                        malshabItem.setIsUpToDate(true);
                        database.compileStatement(malshabItem.getUpdateCommand(this, null, null)).execute();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Delete
            database.compileStatement(MalshabItem.getDeleteCommand(MalshabItem.TABLE_NAME,
                    new Condition[]{new Condition(MalshabItem.COL_IS_UP_TO_DATE, false, Condition.Operator.EQUAL)}))
                    .executeUpdateDelete();

            // Insert
            for (MalshabItem malshabItem : newMalshabItems) {
                database.compileStatement(malshabItem.getInsertCommand(this)).executeInsert();
            }

            //////////////////////// Insert Courses  /////////////////////////////////

            // Set all Courses to be NOT Up To Date
            conditionSetNotUpToDate.setColumn(Course.COL_IS_UP_TO_DATE);
            database.compileStatement(Course.getUpdateCommand(Course.TABLE_NAME,
                    new Condition[]{conditionSetNotUpToDate}, null))
                    .executeUpdateDelete();

            // Update
            for (Course course : tempNewCourses) {
                Course dbResult;
                try {
                    dbResult = (Course) Course.findOne(Course.COL_COURSE_ID, course.getCourseID(),
                            this, Course.class);

                    if (dbResult != null && dbResult.getId() != null) {
                        //TODO: Take care to the course Cycles
                        /////////////////////////// CYCLES //////////////////////////////
                        if (course.getCycles() != null && course.getCycles().size() > 0) {
                            //////////////////////// Insert Course Cycles  /////////////////////////////////
                            for (int cycleIndex = 0; cycleIndex < course.getCycles().size(); cycleIndex++) {
                                Cycle cycle = course.getCycles().get(cycleIndex);
                                cycle.setCourseID(course.getCourseID());
                                if (cycle.getStartDate() != null && cycle.getEndDate() != null &&
                                        (cycle.getEndDate().getTime() > new Date().getTime())) {
                                    long insertCycleId = database.compileStatement(cycle.getInsertCommand(this)).executeInsert();
                                    if (insertCycleId == -1)
                                        throw new Exception("Failed to insert cycle");
                                    cycle.setId(insertCycleId);
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
                            for (int ratingIndex = 0; ratingIndex < course.getRatings().size(); ratingIndex++) {
                                Rating rating = course.getRatings().get(ratingIndex);
                                rating.setCourseID(course.getCourseID());
                                if (rating.getCourseID() != 0 &&
                                        rating.getUserMailAddress() != null && !rating.getUserMailAddress().equals("")
                                        && rating.getCreatedAt() != null && rating.getLastModified() != null) {
                                    long insertRatingId = database.compileStatement(rating.getInsertCommand(this)).executeInsert();
                                    if (insertRatingId == -1)
                                        throw new Exception("Failed to insert rating");
                                    rating.setId(insertRatingId);
                                } else {
                                    course.getRatings().remove(ratingIndex);
                                    ratingIndex--;
                                }
                            }
                        }
                        /////////////////////////// END RATINGS //////////////////////////////
                        course.setIsUpToDate(true);
                        database.compileStatement(course.getUpdateCommand(this, null, null)).execute();
                        newCourses.remove(course);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Delete
            database.compileStatement(Course.getDeleteCommand(Course.TABLE_NAME,
                    new Condition[]{new Condition(Course.COL_IS_UP_TO_DATE, false, Condition.Operator.EQUAL)}))
                    .executeUpdateDelete();

            // Insert
            for (Course course : newCourses) {
                //TODO: Take care to the course Cycles
                /////////////////////////// CYCLES //////////////////////////////
                if (course.getCycles() != null && course.getCycles().size() > 0) {
                    //////////////////////// Insert Course Cycles  /////////////////////////////////
                    for (int cycleIndex = 0; cycleIndex < course.getCycles().size(); cycleIndex++) {
                        Cycle cycle = course.getCycles().get(cycleIndex);
                        cycle.setCourseID(course.getCourseID());
                        if (cycle.getStartDate() != null && cycle.getEndDate() != null &&
                                (cycle.getEndDate().getTime() > new Date().getTime())) {
                            long insertCycleId = database.compileStatement(cycle.getInsertCommand(this)).executeInsert();
                            if (insertCycleId == -1)
                                throw new Exception("Failed to insert cycle");
                            cycle.setId(insertCycleId);
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
                    for (int ratingIndex = 0; ratingIndex < course.getRatings().size(); ratingIndex++) {
                        Rating rating = course.getRatings().get(ratingIndex);
                        rating.setCourseID(course.getCourseID());
                        if (rating.getCourseID() != 0 &&
                                rating.getUserMailAddress() != null && !rating.getUserMailAddress().equals("")
                                && rating.getCreatedAt() != null && rating.getLastModified() != null) {
                            long insertRatingId = database.compileStatement(rating.getInsertCommand(this)).executeInsert();
                            if (insertRatingId == -1)
                                throw new Exception("Failed to insert rating");
                            rating.setId(insertRatingId);
                        } else {
                            course.getRatings().remove(ratingIndex);
                            ratingIndex--;
                        }
                    }
                }
                /////////////////////////// END RATINGS //////////////////////////////
                long id = database.compileStatement(course.getInsertCommand(this)).executeInsert();
            }

            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.PREF_IS_UPDATE_SERVICE_SUCCESS_ONCE, true).apply();
            ApplicationMarshal.setLastUpdatedNow(this);
            database.setTransactionSuccessful();

            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            database.endTransaction();

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_UPDATE_DATA);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(RESULT_UPDATE_DATA, result);
            sendBroadcast(broadcastIntent);

            isRunning = false;

            // Notify new courses
            if (result && !PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.PREF_IS_FIRST_RUN, true)) {
                if (newCourses.size() > 0) {
                    notifyNewCourses(newCourses);
                }
            }
        }
    }

    private void notifyNewCourses(List<Course> newCourses) {
        List<Course> courses = getCoursesMatchUserChannels(newCourses);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent defaultPendingIntent = pendingIntent;
        try {
            Course course = (Course) Course.findOne(Course.COL_COURSE_ID,
                    newCourses.get(0).getCourseID(), this, Course.class);

            Intent courseActivityIntent = new Intent(this, CourseActivity.class);
            courseActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            courseActivityIntent.putExtra(Constants.EXTRA_COURSE, course);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(courseActivityIntent);
            // Gets a PendingIntent containing the entire back stack
            pendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        } catch (Exception e) {
            e.printStackTrace();
        }
        String message = "New " + courses.get(0).getName() + " course!";
        new NotificationUtils.GeneratePictureStyleNotification(this, message,
                courses.get(0).getImageUrl(), pendingIntent).execute();
        if (courses.size() > 1) {
            String restCoursesMessage = "We have " + (courses.size()) + " new courses!\n" +
                    "Tap and take a look!";
            new NotificationUtils(this).notify(restCoursesMessage, defaultPendingIntent);
        }
    }

    private List<Course> getCoursesMatchUserChannels(List<Course> newCourses) {
        Set<String> fcmChannels = PreferenceManager.getDefaultSharedPreferences(this)
                .getStringSet(Constants.PREF_FCM_CHANNELS, null);
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
