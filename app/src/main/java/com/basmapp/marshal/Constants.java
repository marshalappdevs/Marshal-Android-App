package com.basmapp.marshal;

import android.content.Intent;

public class Constants {
    public static final String LOG_TAG = "Marshal";

    // Actions
    public static final String ACTION_COURSE_SUBSCRIPTION_STATE_CHANGED =
            "com.basmapp.marshal.ACTION_COURSE_SUBSCRIPTION_STATE_CHANGED";

    // Preferences Keys
    public static final String PREF_SHOW_WARM_WELCOME = "pref_show_warm_welcome";
    public static final String PREF_IS_FIRST_RUN = "pref_is_first_run";
    public static final String PREF_IS_UPDATE_SERVICE_SUCCESS_ONCE = "is_update_service_success_once";
    public static final String PREF_IS_THERE_UPDATES_TO_SHOW = "is_there_new_updates_to_show";
    public static final String PREF_LAST_UPDATE_TIMESTAMP = "last_update_timestamp";
    public static final String PREF_GCM_CHANNELS = "gcm_channels";
    public static final String PREF_THEME = "THEME";
    public static final String PREF_PRIMARY_COLOR = "PRIMARY-COLOR";
    public static final String PREF_ACCENT_COLOR = "ACCENT-COLOR";
    public static final String PREF_REVERT_THEME = "REVERT-THEME";
    public static final String PREF_PRIMARY_COLOR_CODE = "PRIMARY_COLOR_CODE";
    public static final String PREF_PRIMARY_COLOR_NAME = "PRIMARY_COLOR_NAME";
    public static final String PREF_ACCENT_COLOR_CODE = "ACCENT_COLOR_CODE";
    public static final String PREF_ACCENT_COLOR_NAME = "ACCENT_COLOR_NAME";
    public static final String PREF_LANGUAGE = "LANG";
    public static final String PREF_VERSION = "version";
    public static final String PREF_CLEAR_CACHE = "clear-cache";
    public static final String PREF_CLEAR_TAP_TARGETS = "clear-tap-targets";
    public static final String PREF_CLEAR_HISTORY = "clear-history";
    public static final String PREF_CCT = "CCT";
    public static final String PREF_NOTIFICATIONS_RINGTONE = "notifications_new_message_ringtone";
    public static final String PREF_NOTIFICATIONS_COLOR = "notify_light_color";
    public static final String PREF_IS_DEVICE_REGISTERED = "is_device_registered_to_gcm";
    public static final String PREF_MUST_UPDATE = "must_update";
    public static final String SHOW_FILTER_TAP_TARGET = "show_filter_tap_target";
    public static final String SHOW_CYCLE_FAB_TAP_TARGET = "show_cycle_fab_tap_target";

    // Extras Keys
    public static final String EXTRA_COURSE = "course_extra";
    public static final String EXTRA_COURSE_CODE = "EXTRA_COURSE_CODE";
    public static final String EXTRA_COURSE_NAME = "EXTRA_COURSE_NAME";
    public static final String EXTRA_COURSES_LIST = "extra_courses_list";
    public static final String EXTRA_COURSE_TYPE = "course_type";
    public static final String EXTRA_SEARCH_QUERY = "search_query";
    public static final String EXTRA_ALL_COURSES = "all_courses";
    public static final String EXTRA_LAST_VIEWPAGER_POSITION = "extra_last_viewpager_position";
    public static final String EXTRA_PROGRESS_PERCENT = "progress_percent";
    public static final String EXTRA_COURSE_MATERIALS_LIST = "EXTRA_COURSE_MATERIALS_LIST";
    public static final String EXTRA_IS_RUN_FOR_COURSE = "EXTRA_IS_RUN_FOR_COURSE";
    public static final String EXTRA_RATING_AMOUNT = "extra_rating_amount";
    public static final String EXTRA_RATING_AVERAGE = "extra_rating_average";
    public static final String EXTRA_RATING_BAR_STARS = "extra_rating_bar_stars";
    public static final String EXTRA_GCM_CHANNELS = "extra_gcm_channels";
    public static final String EXTRA_COURSE_POSITION_IN_LIST = "extra_course_position_in_list";

    // Fragment Tags
    public static final String FRAGMENT_PRIMARY_COLOR_PICKER = "primary_color_picker";
    public static final String FRAGMENT_ACCENT_COLOR_PICKER = "accent_color_picker";
}
