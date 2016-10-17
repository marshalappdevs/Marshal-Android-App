package com.basmapp.marshal.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.ui.adapters.CoursesRecyclerAdapter;
import com.basmapp.marshal.ui.adapters.CoursesSearchRecyclerAdapter;
import com.basmapp.marshal.ui.fragments.CoursesFragment;
import com.basmapp.marshal.util.DateHelper;
import com.basmapp.marshal.util.SuggestionProvider;
import com.basmapp.marshal.util.ThemeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static android.app.SearchManager.QUERY;
import static android.content.Intent.ACTION_SEARCH;


public class SearchActivity extends BaseActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private Toolbar mToolbar;
    private SearchView mSearchView;
    private RecyclerView mRecycler;
    private CoursesSearchRecyclerAdapter mAdapter;

    private ArrayList<Course> mCoursesList;
    private ArrayList<Course> mFilteredCourseList;

    private String mSearchQuery;
    private TextView mNoResults;
    private static String mStartDate;
    private static String mEndDate;
    private boolean isEmptyResult = false;
    private MaterialTapTargetPrompt mFilterPrompt;
    private BroadcastReceiver mAdaptersBroadcastReceiver;

    private static final String SEARCH_PREVIOUS_QUERY = "SEARCH_PREVIOUS_QUERY";
    private static final String FILTER_PREVIOUS_START_DATE = "FILTER_PREVIOUS_START_DATE";
    private static final String FILTER_PREVIOUS_END_DATE = "FILTER_PREVIOUS_END_DATE";
    private static final String FILTER_PREVIOUS_START_DATE_FINAL = "FILTER_PREVIOUS_START_DATE_FINAL";
    private static final String FILTER_PREVIOUS_END_DATE_FINAL = "FILTER_PREVIOUS_END_DATE_FINAL";
    private String mFinalStartDate;
    private String mFinalEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.search_title);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mRecycler = (RecyclerView) findViewById(R.id.search_activity_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(linearLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());

        mNoResults = (TextView) findViewById(R.id.search_activity_no_results);

        mCoursesList = CoursesFragment.mCoursesList;

        if (mCoursesList != null)
            mFilteredCourseList = new ArrayList<>(mCoursesList);
        else
            mFilteredCourseList = new ArrayList<>();

        if (mAdapter == null)
            mAdapter = new CoursesSearchRecyclerAdapter(this, mFilteredCourseList);

        if (mRecycler.getAdapter() == null)
            mRecycler.setAdapter(mAdapter);

        mAdaptersBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.ACTION_COURSE_SUBSCRIPTION_STATE_CHANGED)) {
                    int coursePositionInList = intent.getIntExtra(Constants.EXTRA_COURSE_POSITION_IN_LIST, -1);
                    Course course = intent.getParcelableExtra(Constants.EXTRA_COURSE);
                    if (course != null && course.getCategory() != null &&
                            coursePositionInList != -1) {
                        mCoursesList.set(coursePositionInList, course);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoursesRecyclerAdapter.ACTION_ITEM_DATA_CHANGED);
        intentFilter.addAction(Constants.ACTION_COURSE_SUBSCRIPTION_STATE_CHANGED);
        registerReceiver(mAdaptersBroadcastReceiver, intentFilter);

        handleIntent(getIntent());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save SearchView query if possible
        if (mSearchView != null) {
            outState.putString(SEARCH_PREVIOUS_QUERY, mSearchView.getQuery().toString());
        }
        // Save filtered dates if available
        if (mStartDate != null && mEndDate != null) {
            outState.putString(FILTER_PREVIOUS_START_DATE, mStartDate);
            outState.putString(FILTER_PREVIOUS_END_DATE, mEndDate);
        }
        // Save final filtered dates if available
        if (mFinalStartDate != null && mFinalEndDate != null) {
            outState.putString(FILTER_PREVIOUS_START_DATE_FINAL, mFinalStartDate);
            outState.putString(FILTER_PREVIOUS_END_DATE_FINAL, mFinalEndDate);
        }
    }


    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore previous SearchView query
        mSearchQuery = savedInstanceState.getString(SEARCH_PREVIOUS_QUERY);
        // Restore previous filter dates
        mStartDate = savedInstanceState.getString(FILTER_PREVIOUS_START_DATE);
        mEndDate = savedInstanceState.getString(FILTER_PREVIOUS_END_DATE);
        // Restore previous final filter dates
        mFinalStartDate = savedInstanceState.getString(FILTER_PREVIOUS_START_DATE_FINAL);
        mFinalEndDate = savedInstanceState.getString(FILTER_PREVIOUS_END_DATE_FINAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAdaptersBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getMenuInflater().inflate(R.menu.activity_search, options);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = options.findItem(R.id.m_search);
        MenuItemCompat.expandActionView(searchItem);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setQueryRefinementEnabled(true);

        // Close activity when collapsing SearchView
        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        if (mFilterPrompt != null) {
                            mFilterPrompt.finish();
                            mFilterPrompt = null;
                        } else {
                            finish();
                        }
                        return false; // Return false to prevent collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true; // Return true to expand action view
                    }
                });

        // Make sure that query will be set to SearchView
        mSearchView.setQuery(mSearchQuery, false);
        mSearchView.clearFocus();

        // Show filtered search if dates are available (from saved instance for example)
        if (mFinalStartDate != null && mFinalEndDate != null) {
            filterByDatesRange(mFinalStartDate, mFinalEndDate);
        }

        // Show target prompt for filter
        showFilterTargetPrompt();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_filter:
                if (!isEmptyResult) {
                    new FilterByDateDialogFragment().show(getSupportFragmentManager(),
                            Constants.DIALOG_FRAGMENT_FILTER_BY_DATE);
                } else {
                    Toast.makeText(this, R.string.filter_not_available,
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (ACTION_SEARCH.equals(intent.getAction())) {
            mSearchQuery = intent.getStringExtra(QUERY);
            if (mToolbar != null) {
                mToolbar.setTitle(mSearchQuery);
            }
            if (mSearchView != null) {
                mSearchView.setQuery(mSearchQuery, false);
                mSearchView.clearFocus();
            }
            filter(mSearchQuery);
            SuggestionProvider.save(this, mSearchQuery.trim());
        }
    }

    private void showFilterTargetPrompt() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Constants.SHOW_FILTER_TAP_TARGET, true) && !isEmptyResult) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            mFilterPrompt = new MaterialTapTargetPrompt.Builder(this)
                    .setTarget(toolbar.getChildAt(0))
                    .setPrimaryText(R.string.filter_tip_title)
                    .setSecondaryText(R.string.filter_tip_subtitle)
                    .setBackgroundColour(ThemeUtils.getThemeColor(this, R.attr.colorPrimary))
                    .setIcon(R.drawable.ic_filter_vert)
                    .setIconDrawableColourFilter(ThemeUtils.getThemeColor(this, R.attr.colorPrimary))
                    .setAnimationInterpolator(new FastOutSlowInInterpolator())
                    .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                    .setAutoDismiss(false)
                    .setAutoFinish(false)
                    .setCaptureTouchEventOutsidePrompt(true)
                    .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                        @Override
                        public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                            if (tappedTarget) {
                                mFilterPrompt.finish();
                                mFilterPrompt = null;
                                PreferenceManager.getDefaultSharedPreferences(SearchActivity.this).edit()
                                        .putBoolean(Constants.SHOW_FILTER_TAP_TARGET, false).apply();
                            }
                        }

                        @Override
                        public void onHidePromptComplete() {
                        }
                    })
                    .show();
        }
    }

    public static class FilterByDateDialogFragment extends DialogFragment {
        private SimpleDateFormat mSimpleDateFormat;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout to use as dialog or embedded fragment
            View rootView = inflater.inflate(R.layout.date_range_picker, container, false);

            TabHost tabHost = (TabHost) rootView.findViewById(R.id.tabHost);

            DatePicker startDate = (DatePicker) rootView.findViewById(R.id.start_date_picker);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                //noinspection deprecation
                startDate.setCalendarViewShown(false);
            }
            final DatePicker endDate = (DatePicker) rootView.findViewById(R.id.end_date_picker);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                //noinspection deprecation
                endDate.setCalendarViewShown(false);
            }

            mSimpleDateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

//         Set end date a month later from now
            Calendar now = Calendar.getInstance();
            now.add(Calendar.MONTH, 1);
            mEndDate = (mSimpleDateFormat.format(now.getTime()));
            endDate.updateDate(endDate.getYear(), endDate.getMonth() + 1, endDate.getDayOfMonth());
            endDate.setMinDate(System.currentTimeMillis() - 1000);
            endDate.init(
                    endDate.getYear(), endDate.getMonth(), endDate.getDayOfMonth(),
                    new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, monthOfYear, dayOfMonth);
                            mEndDate = (mSimpleDateFormat.format(calendar.getTime()));
                        }
                    });

            mStartDate = (mSimpleDateFormat.format(Calendar.getInstance().getTime()));
            startDate.setMinDate(System.currentTimeMillis() - 1000);
            startDate.init(
                    startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth(),
                    new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, monthOfYear, dayOfMonth);
                            calendar.set(Calendar.HOUR_OF_DAY,
                                    calendar.getMinimum(Calendar.HOUR_OF_DAY));
                            calendar.set(Calendar.MINUTE,
                                    calendar.getMinimum(Calendar.MINUTE));
                            calendar.set(Calendar.SECOND,
                                    calendar.getMinimum(Calendar.SECOND));
                            calendar.set(Calendar.MILLISECOND,
                                    calendar.getMinimum(Calendar.MILLISECOND));
                            // Set twice to workaround this issue https://goo.gl/PV17la
                            endDate.setMinDate(0);
                            endDate.setMinDate(calendar.getTimeInMillis());
                            mStartDate = (mSimpleDateFormat.format(calendar.getTime()));
                        }
                    });

            Button filter = (Button) rootView.findViewById(R.id.filter);
            Button clear = (Button) rootView.findViewById(R.id.clear);

            filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((SearchActivity) getActivity()).filter();
                    getDialog().dismiss();
                }
            });

            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((SearchActivity) getActivity()).clear();
                    getDialog().dismiss();
                }
            });

            tabHost.findViewById(R.id.tabHost);
            tabHost.setup();
            TabHost.TabSpec startDatePage = tabHost.newTabSpec("start");
            startDatePage.setContent(R.id.start_date_group);
            startDatePage.setIndicator(getString(R.string.start_date));

            TabHost.TabSpec endDatePage = tabHost.newTabSpec("end");
            endDatePage.setContent(R.id.end_date_group);
            endDatePage.setIndicator(getString(R.string.end_date));

            tabHost.addTab(startDatePage);
            tabHost.addTab(endDatePage);

            return rootView;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            return dialog;
        }
    }


    public void filter() {
        if (mStartDate != null && mEndDate != null) {
            filterByDatesRange(mStartDate, mEndDate);
            // Save final filter dates only after submit
            mFinalStartDate = mStartDate;
            mFinalEndDate = mEndDate;
        }
    }

    public void clear() {
        String query = mSearchView.getQuery() == null ? "" : mSearchView.getQuery().toString();
        if (mFilteredCourseList != null) {
            showResults(query, mFilteredCourseList, true);
        }
        mStartDate = mFinalStartDate = mEndDate = mFinalEndDate = null;
    }

    private void filterByDatesRange(String sStartDate, String sEndDate) {

        ArrayList<Course> currentFilteredList = new ArrayList<>();

        try {
            if (mFilteredCourseList != null && mFilteredCourseList.size() > 0) {
                long rangeStartTime = DateHelper.stringToDate(sStartDate).getTime();
                long rangeEndTime = DateHelper.stringToDate(sEndDate).getTime();

                for (Course course : mFilteredCourseList) {
                    if (course.getCycles() != null) {
                        for (Cycle cycle : course.getCycles()) {
                            try {
                                long cycleStartTime = cycle.getStartDate().getTime();
                                long cycleEndTime = cycle.getEndDate().getTime();

                                if (cycleStartTime >= rangeStartTime && cycleEndTime <= rangeEndTime) {
                                    currentFilteredList.add(course);
                                    break;
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            showResults(sStartDate + sEndDate, currentFilteredList, true);
        }
    }

    private void filter(String query) {
        if (query == null || query.equals("*")) {
            mFilteredCourseList = new ArrayList<>(mCoursesList);
        } else {
            mFilteredCourseList = new ArrayList<>();
            for (Course item : mCoursesList) {
                if (item.getName().toLowerCase().contains(query.toLowerCase().trim()) ||
                        item.getDescription().toLowerCase().contains(query.toLowerCase().trim()) ||
                        item.getSyllabus().toLowerCase().contains(query.toLowerCase().trim()) ||
                        isHasCycle(item, query.toLowerCase().trim())) {
                    mFilteredCourseList.add(item);
                }
            }
        }
        showResults(query, mFilteredCourseList, false);
    }

    private void showResults(String query, ArrayList<Course> listToShow, boolean filter) {
        if (listToShow.isEmpty()) {
            String searchResult;
            if (filter) {
                searchResult = String.format(getString(
                        R.string.no_results_for_filter), mStartDate, mEndDate);
            } else {
                searchResult = String.format(getString(
                        R.string.no_results_for_query), query);
                isEmptyResult = true;
            }
            mNoResults.setText(searchResult);
            mNoResults.setGravity(Gravity.CENTER);
            mNoResults.setVisibility(View.VISIBLE);
        } else {
            mNoResults.setVisibility(View.GONE);
            isEmptyResult = false;
        }
        mAdapter.animateTo(listToShow);
        mRecycler.scrollToPosition(0);
    }

    private boolean isHasCycle(Course course, String filterText) {

        if (course.getCycles() == null || course.getCycles().size() == 0) {
            return false;
        } else {
            for (Cycle cycle : course.getCycles()) {
                if (isTextIncludeInCycle(cycle, filterText)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isTextIncludeInCycle(Cycle cycle, String text) {

        int day, month;
        text = text.replace(".", "/");
        String[] textParts = text.split("/");

        if (textParts.length == 1) {
            try {
                Calendar startCalendar, endCalendar;
                int searchNumber, startDay, endDay, startMonth, endMonth;
                searchNumber = Integer.valueOf(text);

                startCalendar = Calendar.getInstance();
                startCalendar.setTime(cycle.getStartDate());
                startDay = startCalendar.get(Calendar.DAY_OF_MONTH);
                startMonth = startCalendar.get(Calendar.MONTH);

                endCalendar = Calendar.getInstance();
                endCalendar.setTime(cycle.getEndDate());
                endDay = startCalendar.get(Calendar.DAY_OF_MONTH);
                endMonth = startCalendar.get(Calendar.MONTH);

                if ((searchNumber >= startDay || searchNumber <= endDay) ||
                        searchNumber == endMonth || searchNumber == startMonth) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                day = Integer.valueOf(textParts[0]);
                month = Integer.valueOf(textParts[1]) - 1;
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.getInstance().get(Calendar.YEAR), month, day);
                long searchTimeStamp = calendar.getTime().getTime();
                long startTimeStamp = cycle.getStartDate().getTime();
                startTimeStamp -= (startTimeStamp % 86400000);
                long endTimeStamp = cycle.getEndDate().getTime();
                endTimeStamp -= (endTimeStamp % 86400000);

                if (searchTimeStamp >= startTimeStamp && searchTimeStamp <= endTimeStamp) {
                    return true;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        }
        return false;
    }
}