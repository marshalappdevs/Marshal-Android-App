package com.basmapp.marshal.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.MalshabItem;
import com.basmapp.marshal.entities.MaterialItem;
import com.basmapp.marshal.interfaces.ContentProviderCallBack;
import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.localdb.interfaces.BackgroundTaskCallBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContentProvider {

    private static ContentProvider mInstance = new ContentProvider();

    private Set<String> coursesCategories;
    private HashMap<String,ArrayList<Course>> sCoursesListsMap = new HashMap<>();
    private ArrayList<Course> sCourses;
    private ArrayList<Course> sViewPagerCourses;
    private ArrayList<Course> sMeetups;
    private ArrayList<Course> sSubscribedCourses;
    private ArrayList<MaterialItem> sMaterialItems;
    private ArrayList<MalshabItem> sMalshabItems;

    public static ContentProvider getInstance() {
        return mInstance;
    }

    public Set<String> getCoursesCategories(Context context) {
        if (coursesCategories == null) {
            coursesCategories = PreferenceManager.getDefaultSharedPreferences(context)
                    .getStringSet(Constants.PREF_CATEGORIES, null);
        }

        return coursesCategories;
    }
    public void initAllData(Context context) {

        // Get All Courses ---> sCourses
        initCoursesLists(context);

        // Get All Courses ---> sCourses
        initCourses(context, null);

        // Get All Meetups ---> sMeetups
        initMeetups(context, null);

        // Get Subscribed Courses ---> sSubscribedCourses
        initSubscribedCourses(context, null);

        // Get ViewPager Courses ---> sViewPagerCourses
        initViewPagerCourses(context, null);

        // Get All Materials ---> sMaterials
        initMaterialItems(context, null);

        // Get All MalshabItems ---> sMalshabItems
        initMalshabItems(context, null);
    }

    private void initCourseListByCategory(final Context context, final String category, final ContentProviderCallBack callback) {
        Course.findByColumnInBackground(false, DBConstants.COL_CATEGORY, category, null,
                context, Course.class, new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        if (data != null) {
                            ArrayList<Course> coursesResult = (ArrayList)data;
                            sCoursesListsMap.put(category,coursesResult);
                            if (callback != null) {
                                callback.onDataReady(coursesResult, category);
                            }
                        } else {
                            if (callback != null)
                                callback.onError(new Exception("data is null"));
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (error == null) error = "Query error";
                        callback.onError(new Exception(error));
                    }
                });
    }

    private void initCoursesLists(Context context) {
        Set<String> categories = getCoursesCategories(context);
        for (String category : categories) {
            initCourseListByCategory(context, category.split(";")[0], null);
        }
    }

    // Init data methods
    private void initCourses(Context context, final ContentProviderCallBack callback) {
        Course.findByColumnInBackground(false, DBConstants.COL_IS_MEETUP, false, null,
                context, Course.class, new BackgroundTaskCallBack() {
            @Override
            public void onSuccess(String result, List<Object> data) {
                if (data != null) {
                    sCourses = (ArrayList) data;
                    if (callback != null) {
                        callback.onDataReady(sCourses, null);
                    }
                } else {
                    if (callback != null)
                        callback.onError(new Exception("data is null"));
                }
            }

            @Override
            public void onError(String error) {
                if (error == null) error = "Query error";
                callback.onError(new Exception(error));
            }
        });
    }

    private void initMeetups(Context context, final ContentProviderCallBack callback) {
        Course.findByColumnInBackground(false, DBConstants.COL_IS_MEETUP, true, null,
                context, Course.class, new BackgroundTaskCallBack() {
            @Override
            public void onSuccess(String result, List<Object> data) {
                if (data != null) {
                    sMeetups = (ArrayList) data;
                    if (callback != null) {
                        callback.onDataReady(sMeetups, null);
                    }
                } else {
                    if (callback != null)
                        callback.onError(new Exception("data is null"));
                }
            }

            @Override
            public void onError(String error) {
                if (error == null) error = "Query error";
                callback.onError(new Exception(error));
            }
        });
    }

    private void initMaterialItems(Context context, final ContentProviderCallBack callback) {
        MaterialItem.findAllInBackground(null, MaterialItem.class, context, false,
                new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        if (data != null) {
                            sMaterialItems = (ArrayList) data;
                            if (callback != null) {
                                callback.onDataReady(sMaterialItems, null);
                            }
                        } else {
                            if (callback != null)
                                callback.onError(new Exception("data is null"));
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (error == null) error = "Query error";
                        callback.onError(new Exception(error));
                    }
                });
    }

    private void initMalshabItems(Context context, final ContentProviderCallBack callback) {
        MalshabItem.findAllInBackground(null, MalshabItem.class, context, false, new BackgroundTaskCallBack() {
            @Override
            public void onSuccess(String result, List<Object> data) {
                if (data != null) {
                    sMalshabItems = (ArrayList) data;
                    if (callback != null) {
                        callback.onDataReady(sMalshabItems, null);
                    }
                } else {
                    if (callback != null)
                        callback.onError(new Exception("data is null"));
                }
            }

            @Override
            public void onError(String error) {
                if (error == null) error = "Query error";
                callback.onError(new Exception(error));
            }
        });
    }

    private void initSubscribedCourses(Context context, final ContentProviderCallBack callback) {
        Course.findByColumnInBackground(false, DBConstants.COL_IS_USER_SUBSCRIBE, true, null,
                context, Course.class, new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        if (data != null) {
                            sSubscribedCourses = (ArrayList) data;
                            if (callback != null) {
                                callback.onDataReady(sSubscribedCourses, null);
                            }
                        } else {
                            if (callback != null)
                                callback.onError(new Exception("data is null"));
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (error == null) error = "Query error";
                        callback.onError(new Exception(error));
                    }
                });
    }

    private void initViewPagerCourses(Context context, final ContentProviderCallBack callback) {
        Course.rawQueryInBackground(Course.getCloestCoursesSqlQuery(5, true), context,
                Course.class, false, new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        if (data != null) {
                            sViewPagerCourses = (ArrayList) data;
                            if (callback != null) {
                                callback.onDataReady(sViewPagerCourses, null);
                            }
                        } else {
                            if (callback != null)
                                callback.onError(new Exception("data is null"));
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (error == null) error = "Query error";
                        callback.onError(new Exception(error));
                    }
                });
    }

    // Get data methods
    public void getCoursesListByCategory(Context context, String category, ContentProviderCallBack callback) {
        if (sCoursesListsMap.get(category) != null) {
            callback.onDataReady(sCoursesListsMap.get(category), category);
        } else {
            initCourseListByCategory(context, category, callback);
        }
    }

    public void getCourses(Context context, ContentProviderCallBack callBack) {
        if (sCourses != null) callBack.onDataReady(sCourses, null);
        else initCourses(context, callBack);
    }

    public void getMeetups(Context context, ContentProviderCallBack callBack) {
        if (sMeetups != null) callBack.onDataReady(sMeetups, null);
        else initMeetups(context, callBack);
    }

    public void getMaterialItems(Context context, ContentProviderCallBack callBack) {
        if (sMaterialItems != null) callBack.onDataReady(sMaterialItems, null);
        else initMaterialItems(context, callBack);
    }

    public void getMalshabItems(Context context, ContentProviderCallBack callBack) {
        if (sMalshabItems != null) callBack.onDataReady(sMalshabItems, null);
        else initMalshabItems(context, callBack);
    }

    public void getSubscribedCourses(Context context, ContentProviderCallBack callBack) {
        if (sSubscribedCourses != null) callBack.onDataReady(sSubscribedCourses, null);
        else initSubscribedCourses(context, callBack);
    }

    public void getViewPagerCourses(Context context, ContentProviderCallBack callBack) {
        if (sViewPagerCourses != null && sViewPagerCourses.size() > 0) callBack.onDataReady(sViewPagerCourses, null);
        else initViewPagerCourses(context, callBack);
    }

    public void releaseAllData() {
        sCourses = null;
        sViewPagerCourses = null;
        sMeetups = null;
        sSubscribedCourses = null;
        sMaterialItems = null;
        sMalshabItems = null;
    }

    public void sendBroadcast(Context context, String action) {
        context.sendBroadcast(new Intent(action));
    }

    private void sendBroadcast(Context context, Intent intent) {
        context.sendBroadcast(intent);
    }

    public void notifyCourseRatingUpdated(Context context, Course course) {
        Intent intent = new Intent(Actions.COURSE_RATING_UPDATED);
        intent.putExtra(Extras.COURSE, course);
        sendBroadcast(context, intent);
    }

    public void notifyCourseSubscriptionUpdated(Context context, final Course course) {

        initSubscribedCourses(context, null);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                sCoursesListsMap.get(course.getCategory()).get(Utils.getCoursePositionInList(
                        sCoursesListsMap.get(course.getCategory()), course)).setIsUserSubscribe(course.getIsUserSubscribe());

                if (sCourses != null) sCourses.get(Utils.getCoursePositionInList(
                        sCoursesListsMap.get(course.getCategory()), course)).setIsUserSubscribe(course.getIsUserSubscribe());
                return null;
            }
        }.execute();

        Intent intent = new Intent(Actions.COURSE_SUBSCRIPTION_UPDATED);
        intent.putExtra(Extras.COURSE, course);
        sendBroadcast(context, intent);
    }

    public static class Actions {
        private static final String baseString = "com.basmapp.marshal.ACTION_";

        public static final String COURSE_RATING_UPDATED = baseString + "COURSE_RATING_UPDATED";
        public static final String COURSE_SUBSCRIPTION_UPDATED = baseString + "COURSE_SUBSCRIPTION_UPDATED";
    }

    public static class Extras {

        public static final String COURSE = "extra_course";
    }

    public static class Utils {

        public static int getCoursePositionInList(ArrayList<Course> courses, Course course) {

            if (courses != null) {
                for (int currPosition = 0; currPosition < courses.size(); currPosition++) {
                    if (courses.get(currPosition).getId().equals(course.getId()))
                        return currPosition;
                }
            }

            return -1;
        }
    }
}
