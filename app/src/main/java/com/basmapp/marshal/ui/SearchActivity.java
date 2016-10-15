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
import android.view.Window;
import android.view.WindowManager;
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

    private SearchView mSearchView;
    private RecyclerView mRecycler;
    private CoursesSearchRecyclerAdapter mAdapter;

    private ArrayList<Course> mCoursesList;
    private ArrayList<Course> mFilteredCourseList;

    private Dialog mFilterDialog;
    private String mSearchQuery;
    private TextView mNoResults;
    private String mStartDate;
    private String mEndDate;
    private boolean isEmptyResult = false;
    private MaterialTapTargetPrompt mFilterPrompt;
    private BroadcastReceiver mAdaptersBroadcastReceiver;
    private SimpleDateFormat mSimpleDateFormat;

    private static final String SEARCH_PREVIOUS_QUERY = "SEARCH_PREVIOUS_QUERY";
    private static final String FILTER_PREVIOUS_START_DATE = "FILTER_PREVIOUS_START_DATE";
    private static final String FILTER_PREVIOUS_END_DATE = "FILTER_PREVIOUS_END_DATE";
    private String mSavedStartDate;
    private String mSavedEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.search_title);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
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
        if (mSavedStartDate != null && mSavedEndDate != null) {
            outState.putString(FILTER_PREVIOUS_START_DATE, mSavedStartDate);
            outState.putString(FILTER_PREVIOUS_END_DATE, mSavedEndDate);
        }
    }


    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore previous SearchView query
        mSearchQuery = savedInstanceState.getString(SEARCH_PREVIOUS_QUERY);
        // Restore previous filter dates
        mStartDate = mSavedStartDate = savedInstanceState.getString(FILTER_PREVIOUS_START_DATE);
        mEndDate = mSavedEndDate = savedInstanceState.getString(FILTER_PREVIOUS_END_DATE);
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
        if (mStartDate != null && mEndDate != null) {
            filterByDatesRange(mStartDate, mEndDate);
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
                    showFilterByDateDialog();
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
            if (mSearchView != null) {
                mSearchView.setQuery(mSearchQuery, false);
                mSearchView.clearFocus();
            }
            filter(mSearchQuery);
            SuggestionProvider.save(this, mSearchQuery.trim());
        }
    }

    private void showFilterTargetPrompt() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.SHOW_FILTER_TAP_TARGET, true)) {
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

    public void showFilterByDateDialog() {
        mFilterDialog = new Dialog(this);
        mFilterDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        mFilterDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View dateRangeView = layoutInflater.inflate(R.layout.date_range_picker, null);
        mFilterDialog.setContentView(dateRangeView);
        TabHost tabHost = (TabHost) dateRangeView.findViewById(R.id.tabHost);

        DatePicker startDate = (DatePicker) dateRangeView.findViewById(R.id.start_date_picker);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            //noinspection deprecation
            startDate.setCalendarViewShown(false);
        }
        final DatePicker endDate = (DatePicker) dateRangeView.findViewById(R.id.end_date_picker);
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

        Button filter = (Button) dateRangeView.findViewById(R.id.filter);
        Button clear = (Button) dateRangeView.findViewById(R.id.clear);

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStartDate != null && mEndDate != null) {
                    filterByDatesRange(mStartDate, mEndDate);
                    // Save filter dates only after submit
                    mSavedStartDate = mStartDate;
                    mSavedEndDate = mEndDate;
                    mFilterDialog.dismiss();
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query;
                if (mSearchView.getQuery() != null) {
                    query = mSearchView.getQuery().toString();
                } else {
                    query = "";
                }
                if (mFilteredCourseList != null) {
                    showResults(query, mFilteredCourseList, true);
                }
                mFilterDialog.dismiss();
                mStartDate = mSavedStartDate = mEndDate = mSavedEndDate = null;
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
        mFilterDialog.show();
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

    private void filter(String filterText) {
        if (filterText == null) {
            mFilteredCourseList = new ArrayList<>(mCoursesList);
        } else if (filterText.equals("*")) {
            mFilteredCourseList = new ArrayList<>(mCoursesList);
        } else {
            mFilteredCourseList = new ArrayList<>();
            for (Course item : mCoursesList) {
                if (item.getName().toLowerCase().contains(filterText.toLowerCase().trim()) ||
                        item.getDescription().toLowerCase().contains(filterText.toLowerCase().trim()) ||
                        item.getSyllabus().toLowerCase().contains(filterText.toLowerCase().trim()) ||
                        isHasCycle(item, filterText.toLowerCase().trim())) {
                    mFilteredCourseList.add(item);
                }
            }
        }
        showResults(filterText, mFilteredCourseList, false);
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