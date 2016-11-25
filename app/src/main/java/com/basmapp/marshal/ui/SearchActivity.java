package com.basmapp.marshal.ui;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.interfaces.ContentProviderCallBack;
import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.ui.adapters.CoursesSearchRecyclerAdapter;
import com.basmapp.marshal.util.ContentProvider;
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
    private boolean isEmptyResult = false;
    private static long sStartDate, sEndDate = 0;
    private SimpleDateFormat mFilterDateFormat;
    private MaterialTapTargetPrompt mFilterPrompt;
    private BroadcastReceiver mAdaptersBroadcastReceiver;

    private static final String SEARCH_PREVIOUS_QUERY = "SEARCH_PREVIOUS_QUERY";
    private static final String FILTER_PREVIOUS_START_DATE = "FILTER_PREVIOUS_START_DATE";
    private static final String FILTER_PREVIOUS_END_DATE = "FILTER_PREVIOUS_END_DATE";

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

        mFilterDateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

        mNoResults = (TextView) findViewById(R.id.search_activity_no_results);

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
                if (intent.getAction().equals(ContentProvider.Actions.COURSE_RATING_UPDATED)) {
                    Course course = intent.getParcelableExtra(ContentProvider.Extras.COURSE);
                    int itemPosition = ContentProvider.Utils.getCoursePositionInList(mCoursesList, course);

                    if (itemPosition > -1)
                        mAdapter.notifyItemChanged(itemPosition);
                } else if (intent.getAction().equals(ContentProvider.Actions.COURSE_SUBSCRIPTION_UPDATED)) {
                    Course course = intent.getParcelableExtra(ContentProvider.Extras.COURSE);
                    mCoursesList.get(ContentProvider.Utils.getCoursePositionInList(mCoursesList,
                            course)).setIsUserSubscribe(course.getIsUserSubscribe());
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ContentProvider.Actions.COURSE_SUBSCRIPTION_UPDATED);
        intentFilter.addAction(ContentProvider.Actions.COURSE_RATING_UPDATED);
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
        if (sStartDate != 0) outState.putLong(FILTER_PREVIOUS_START_DATE, sStartDate);
        if (sEndDate != 0) outState.putLong(FILTER_PREVIOUS_END_DATE, sEndDate);
    }


    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore previous SearchView query
        mSearchQuery = savedInstanceState.getString(SEARCH_PREVIOUS_QUERY);
        // Restore previous filter dates
        sStartDate = savedInstanceState.getLong(FILTER_PREVIOUS_START_DATE);
        sEndDate = savedInstanceState.getLong(FILTER_PREVIOUS_END_DATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAdaptersBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.m_search);
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
        mSearchView.post(new Runnable() {
            @Override
            public void run() {
                mSearchView.setQuery(mSearchQuery, false);
            }
        });
        mSearchView.clearFocus();

        // Show filtered search if dates are available (from saved instance for example)
        if (sStartDate != 0 && sEndDate != 0) {
            filterByDatesRange(sStartDate, sEndDate);
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
                    new CourseFilterDialog().show(getSupportFragmentManager(),
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

            if (mCoursesList == null) {
                fetchData(mSearchQuery);
            } else {
                filter(mSearchQuery);
            }
            SuggestionProvider.save(this, mSearchQuery.trim());
        }
    }

    private void fetchData(final String query) {
        ContentProvider.getInstance().getCourses(getApplicationContext(), new ContentProviderCallBack() {
            @Override
            public void onDataReady(ArrayList<? extends DBObject> data, Object extra) {
                mCoursesList = (ArrayList<Course>) data;
                mFilteredCourseList = (ArrayList<Course>) data;
                filter(query);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
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

    public static class CourseFilterDialog extends DialogFragment {
        private Calendar mCalendar;
        private TextView mStartDatePicker, mEndDatePicker;
        private SimpleDateFormat mFilterDateFormat;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(STYLE_NO_TITLE, R.style.CourseFilterDialogTheme);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.course_filter_dialog, container);
            WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
            layoutParams.gravity = Gravity.TOP;

            mFilterDateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

            mStartDatePicker = (TextView) rootView.findViewById(R.id.start_date_picker);
            mEndDatePicker = (TextView) rootView.findViewById(R.id.end_date_picker);

            if (sStartDate != 0) {
                mStartDatePicker.setText(mFilterDateFormat.format(sStartDate));
            }

            if (sEndDate != 0) {
                mEndDatePicker.setText(mFilterDateFormat.format(sEndDate));
            }

            // Dismiss dialog button
            ImageButton dismiss = (ImageButton) rootView.findViewById(R.id.course_filter_dismiss);
            dismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getDialog().dismiss();
                }
            });

            // Apply filter button
            Button filter = (Button) rootView.findViewById(R.id.apply_filter);
            filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Check if start date is not empty
                    if (sStartDate == 0) {
                        Toast.makeText(getActivity(), R.string.course_filter_start_date_empty, Toast.LENGTH_SHORT).show();
                    }

                    // Check if end date is not empty
                    if (sEndDate == 0) {
                        Toast.makeText(getActivity(), R.string.course_filter_end_date_empty, Toast.LENGTH_SHORT).show();
                    }

                    // Apply search only if two dates are picked
                    if (sStartDate != 0 && sEndDate != 0) {
                        getDialog().dismiss();
                        ((SearchActivity) getActivity()).applyFilter();
                    }
                }
            });

            // Reset filter button
            Button reset = (Button) rootView.findViewById(R.id.reset_filter);
            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getDialog().dismiss();
                    ((SearchActivity) getActivity()).resetFilter();
                }
            });

            mCalendar = Calendar.getInstance();

            //  Start date dialog picker
            final DatePickerDialog.OnDateSetListener startDatePickerDialog = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    // Get dates from dialog picker
                    mCalendar.set(Calendar.YEAR, year);
                    mCalendar.set(Calendar.MONTH, monthOfYear);
                    mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    // Update TextView
                    mStartDatePicker.setText(mFilterDateFormat.format(mCalendar.getTime()));
                    // Save start date long
                    sStartDate = mCalendar.getTimeInMillis();
                }
            };

            // Open start date dialog picker
            rootView.findViewById(R.id.start_date_picker_clickable_area)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Open dialog picker with today's date
                            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), startDatePickerDialog, mCalendar
                                    .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                                    mCalendar.get(Calendar.DAY_OF_MONTH));
                            // Set minimum date to now
                            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                            // If start date already chosen, open dialog with this date
                            if (sStartDate != 0) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(sStartDate);
                                datePickerDialog.updateDate(calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH));
                            }
                            // Set maximum date to end date if already chosen
                            if (sEndDate != 0) {
                                datePickerDialog.getDatePicker().setMaxDate(sEndDate);
                            }
                            datePickerDialog.show();
                        }
                    });

            final DatePickerDialog.OnDateSetListener endDatePickerDialog = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    // Get dates from dialog picker
                    mCalendar.set(Calendar.YEAR, year);
                    mCalendar.set(Calendar.MONTH, monthOfYear);
                    mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    // Update TextView
                    mEndDatePicker.setText(mFilterDateFormat.format(mCalendar.getTime()));
                    // Save end date long
                    sEndDate = mCalendar.getTimeInMillis();
                }
            };

            rootView.findViewById(R.id.end_date_picker_clickable_area)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Open dialog picker with today's date
                            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), endDatePickerDialog, mCalendar
                                    .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                            // If end date already chosen, open dialog with this date
                            if (sEndDate != 0) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(sEndDate);
                                datePickerDialog.updateDate(calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH));
                            }
                            // Set minimum date to start date if already chosen
                            if (sStartDate != 0) {
                                datePickerDialog.getDatePicker().setMinDate(sStartDate);
                            } else {
                                // Set minimum date to now
                                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                            }
                            datePickerDialog.show();
                        }
                    });

            return rootView;
        }
    }

    public void applyFilter() {
        if (sStartDate != 0 && sEndDate != 0) {
            filterByDatesRange(sStartDate, sEndDate);
        }
    }

    public void resetFilter() {
        String query = mSearchView.getQuery() == null ? "" : mSearchView.getQuery().toString();
        if (mFilteredCourseList != null) {
            showResults(query, mFilteredCourseList, true);
        }
        sStartDate = sEndDate = 0;
    }

    private void filterByDatesRange(long startDate, long endDate) {

        ArrayList<Course> currentFilteredList = new ArrayList<>();

        try {
            if (mFilteredCourseList != null && mFilteredCourseList.size() > 0) {
                long rangeStartTime = DateHelper.stringToDate(mFilterDateFormat.format(startDate)).getTime();
                long rangeEndTime = DateHelper.stringToDate(mFilterDateFormat.format(endDate)).getTime();

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
            showResults(mFilterDateFormat.format(startDate) + mFilterDateFormat.format(endDate), currentFilteredList, true);
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
                searchResult = String.format(getString(R.string.no_results_for_filter),
                        mFilterDateFormat.format(sStartDate), mFilterDateFormat.format(sEndDate));
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