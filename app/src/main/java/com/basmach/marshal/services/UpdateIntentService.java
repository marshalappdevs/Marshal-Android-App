package com.basmach.marshal.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.entities.Rating;
import com.basmach.marshal.interfaces.MaterialLinkPreviewCallback;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.ui.MainActivity;
import com.basmach.marshal.utils.LinksDataProvider;
import com.basmach.marshal.utils.MarshalServiceProvider;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UpdateIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_CHECK_FOR_UPDATE = "com.basmach.marshal.utils.action.CHECK_FOR_UPDATE";
    public static final String ACTION_UPDATE_DATA = "com.basmach.marshal.utils.action.UPDATE_DATA";
    public static final String ACTION_UPDATE_DATA_PROGRESS_CHANGED = "com.basmach.marshal.utils.action.UPDATE_DATA_PROGRESS_CHANGED";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.basmach.marshal.utils.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.basmach.marshal.utils.extra.PARAM2";

    public static final String RESULT_CHECK_FOR_UPDATE = "result_check_for_update";
    public static final String RESULT_UPDATE_DATA = "result_update_data";
    public static final String EXTRA_PROGRESS_PERCENT = "progress_percent";

    private static final String LOG_TAG = "UPDATE_SERVICE";


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
    public static void startCheckForUpdate(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UpdateIntentService.class);
        intent.setAction(ACTION_CHECK_FOR_UPDATE);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startUpdateData(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UpdateIntentService.class);
        intent.setAction(ACTION_UPDATE_DATA);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_FOR_UPDATE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionCheckForUpdate(param1, param2);
            } else if (ACTION_UPDATE_DATA.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionUpdateData(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCheckForUpdate(String param1, String param2) {
        // TODO: Handle action Foo
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent broadcastIntent = new Intent();
//                broadcastIntent.setAction(ACTION_CHECK_FOR_UPDATE);
//                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
//                broadcastIntent.putExtra(RESULT_CHECK_FOR_UPDATE, false);
//                sendBroadcast(broadcastIntent);
//            }
//        }, 1000);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_CHECK_FOR_UPDATE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RESULT_CHECK_FOR_UPDATE, true);
        sendBroadcast(broadcastIntent);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateData(String param1, String param2) {
        // TODO: Handle action Baz
        boolean proccess_result = false;

        int itemPercentWeight = 0;
        int progressPercents = 0;

        try {
            List<Cycle> currentCycles = (List) Cycle.getAll(DBConstants.COL_ID, UpdateIntentService.this,
                    Cycle.class);
            List<Course> currentCourses = (List) Course.getAll(DBConstants.COL_ID, UpdateIntentService.this,
                    Course.class);
            List<MaterialItem> currentMaterials = (List) MaterialItem.getAll(DBConstants.COL_URL, UpdateIntentService.this,
                    MaterialItem.class);
            List<Rating> currentRatings = (List) Rating.getAll(DBConstants.COL_COURSE_CODE, UpdateIntentService.this,
                    Rating.class);

            List<Course> newCourses = MarshalServiceProvider.getInstance().getAllCourses().execute().body();
            List<MaterialItem> newMaterials = MarshalServiceProvider.getInstance().getAllMaterials().execute().body();
            List<Rating> newRatings = MarshalServiceProvider.getInstance().getAllRatings().execute().body();

            itemPercentWeight = 100 / (currentCourses.size() + currentCycles.size() +
                    currentMaterials.size() + currentRatings.size() +
                    newCourses.size() + newMaterials.size() + newRatings.size());

            for (MaterialItem materialItem : currentMaterials) {
                try {
                    materialItem.delete();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "materialItem delete failed");
                    e.printStackTrace();
                }

                progressPercents += itemPercentWeight;
                publishProgress(progressPercents);
            }

            for (Rating rating : currentRatings) {
                try {
                    rating.delete();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "rating delete failed");
                    e.printStackTrace();
                }

                progressPercents += itemPercentWeight;
                publishProgress(progressPercents);
            }

            for (Course course : currentCourses) {
                try {
                    course.delete();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "course delete failed");
                    e.printStackTrace();
                }

                progressPercents += itemPercentWeight;
                publishProgress(progressPercents);
            }

            Log.i(LOG_TAG, "old courses deleted successfully");

            for (Cycle cycle : currentCycles) {
                try {
                    cycle.delete();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "cycle delete failed");
                    e.printStackTrace();
                }

                progressPercents += itemPercentWeight;
                publishProgress(progressPercents);
            }

            Log.i(LOG_TAG, "old cycles deleted successfully");

            for (Course course : newCourses) {

                if (course.getCycles() != null) {
                    for (Cycle cycle : course.getCycles()) {
                        try {
                            cycle.Ctor(UpdateIntentService.this);
                            cycle.create();
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "cycle creation failed");
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    course.Ctor(UpdateIntentService.this);
                    course.setImageUrl(MarshalServiceProvider.IMAGES_URL + course.getCourseCode());
                    course.create();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "course creation failed");
                    e.printStackTrace();
                }

                progressPercents += itemPercentWeight;
                publishProgress(progressPercents);
            }

            Log.i(LOG_TAG, "new courses created successfully");

            for (final MaterialItem materialItem : newMaterials) {

                try {
                    materialItem.Ctor(UpdateIntentService.this);
                    materialItem.create();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "rating creation failed");
                    e.printStackTrace();
                }

                progressPercents += itemPercentWeight;
                publishProgress(progressPercents);
            }


            Log.i(LOG_TAG, "new materials created successfully");

            for (final Rating rating : newRatings) {

                try {
                    rating.Ctor(UpdateIntentService.this);
                    rating.create();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "rating creation failed");
                    e.printStackTrace();
                }

                progressPercents += itemPercentWeight;
                publishProgress(progressPercents);
            }

            Log.i(LOG_TAG, "new ratings created successfully");

            for(Course course : newCourses) {
                double ratingsSum = 0;
                int amount = 0;

                for (Rating rating : newRatings) {
                    if(rating.getCourseCode().equals(course.getCourseCode())) {
                        amount++;
                        ratingsSum += rating.getRating();

                        if (MainActivity.userEmailAddress != null &&
                                rating.getUserMailAddress().equals(MainActivity.userEmailAddress)) {
                            course.setUserRating(rating);
                        }
                    }
                }

                double average = ratingsSum / amount;

                course.setRatingsAmount(amount);
                course.setRatingAverage(average);
                course.save();
            }

            Log.i(LOG_TAG, "new ratings save in courses to courses successfully");

            proccess_result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_UPDATE_DATA);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RESULT_UPDATE_DATA, proccess_result);
        sendBroadcast(broadcastIntent);
    }

    public void publishProgress(int progress) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_UPDATE_DATA_PROGRESS_CHANGED);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(EXTRA_PROGRESS_PERCENT, progress);
        sendBroadcast(broadcastIntent);
    }
}
