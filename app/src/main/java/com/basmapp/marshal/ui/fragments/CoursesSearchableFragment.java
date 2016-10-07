package com.basmapp.marshal.ui.fragments;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.ui.adapters.CoursesRecyclerAdapter;
import com.basmapp.marshal.ui.adapters.CoursesSearchRecyclerAdapter;
import com.basmapp.marshal.util.SuggestionProvider;
import com.basmapp.marshal.util.DateHelper;
import com.basmapp.marshal.util.ThemeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class CoursesSearchableFragment extends Fragment {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private SearchView mSearchView;
    private RecyclerView mRecycler;
    private DrawerLayout mDrawerLayout;
    private LinearLayoutManager mLayoutManager;
    private CoursesSearchRecyclerAdapter mAdapter;

    private ArrayList<Course> mCoursesList;
    private ArrayList<Course> mFilteredCourseList;

    private String mFilterText;
    private String mSearchQuery;
    private TextView mNoResults;
    private String mTempStartDate;
    private String mTempEndDate;
    private boolean isEmptyResult = false;
    private MaterialTapTargetPrompt mFilterPrompt;
    private BroadcastReceiver mAdaptersBroadcastReceiver;
    private SimpleDateFormat mSimpleDateFormat;

    public static CoursesSearchableFragment newInstance(String query, ArrayList<Course> courses) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.EXTRA_SEARCH_QUERY, query);
        bundle.putParcelableArrayList(Constants.EXTRA_ALL_COURSES, courses);
        CoursesSearchableFragment coursesSearchableFragment = new CoursesSearchableFragment();
        coursesSearchableFragment.setArguments(bundle);
        return coursesSearchableFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_courses_search, container, false);

        setHasOptionsMenu(true);

        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        mRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_courses_search_recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());

        mNoResults = (TextView) rootView.findViewById(R.id.fragment_courses_search_no_results);

        mSearchQuery = getArguments().getString(Constants.EXTRA_SEARCH_QUERY);
        mCoursesList = getArguments().getParcelableArrayList(Constants.EXTRA_ALL_COURSES);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (mCoursesList != null)
            mFilteredCourseList = new ArrayList<>(mCoursesList);
        else
            mFilteredCourseList = new ArrayList<>();

        if (mAdapter == null)
            mAdapter = new CoursesSearchRecyclerAdapter(getActivity(), mFilteredCourseList);

        if (mRecycler.getAdapter() == null)
            mRecycler.setAdapter(mAdapter);

        getActivity().setTitle(R.string.search_title);

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
        getActivity().registerReceiver(mAdaptersBroadcastReceiver, intentFilter);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mAdaptersBroadcastReceiver);
        // Release navigation view lock
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        // Hide tap target
        if (mFilterPrompt != null) {
            mFilterPrompt.finish();
            mFilterPrompt = null;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        if (mAdapter != null && mRecycler != null) {
            mRecycler.setAdapter(mAdapter);
        }
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Constants.SHOW_FILTER_TAP_TARGET, true)) {
            Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            mFilterPrompt = new MaterialTapTargetPrompt.Builder(getActivity())
                    .setTarget(toolbar.getChildAt(0))
                    .setPrimaryText(R.string.filter_tip_title)
                    .setSecondaryText(R.string.filter_tip_subtitle)
                    .setBackgroundColour(ThemeUtils.getThemeColor(getActivity(), R.attr.colorPrimary))
                    .setIcon(R.drawable.ic_filter_vert)
                    .setIconDrawableColourFilter(ThemeUtils.getThemeColor(getActivity(), R.attr.colorPrimary))
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
                            }
                        }

                        @Override
                        public void onHidePromptComplete() {

                        }
                    })
                    .show();
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                    .putBoolean(Constants.SHOW_FILTER_TAP_TARGET, false).apply();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Setup filter button
        menu.findItem(R.id.menu_main_filter).setVisible(true);

        MenuItem filterItem = menu.findItem(R.id.menu_main_filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!isEmptyResult) {
                    showFilterByDateDialog();
                } else {
                    Toast.makeText(getActivity(), R.string.filter_not_available, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        // Setup search button
        MenuItem searchItem = menu.findItem(R.id.menu_main_searchView);
        mSearchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {
                String suggestion = getSuggestion(position);
                mSearchView.setQuery(suggestion, true);
                mSearchView.clearFocus();
                return true;
            }

            private String getSuggestion(int position) {
                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
                return cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mFilterText = query;
                filter(query);
                mSearchView.clearFocus();
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                suggestions.saveRecentQuery(query, null);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        getActivity().onBackPressed();
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true; // Return true to expand action view
                    }
                });
        MenuItemCompat.expandActionView(searchItem);
        mSearchView.setQuery(mSearchQuery, true);
    }

    public void showFilterByDateDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View dateRangeView = layoutInflater.inflate(R.layout.date_range_picker, null);
        dialog.setContentView(dateRangeView);
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
        mTempEndDate = (mSimpleDateFormat.format(now.getTime()));
        endDate.updateDate(endDate.getYear(), endDate.getMonth() + 1, endDate.getDayOfMonth());
        endDate.setMinDate(System.currentTimeMillis() - 1000);
        endDate.init(
                endDate.getYear(), endDate.getMonth(), endDate.getDayOfMonth(),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        mTempEndDate = (mSimpleDateFormat.format(calendar.getTime()));
                    }
                });

        mTempStartDate = (mSimpleDateFormat.format(Calendar.getInstance().getTime()));
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
                        mTempStartDate = (mSimpleDateFormat.format(calendar.getTime()));
                    }
                });

        Button filter = (Button) dateRangeView.findViewById(R.id.filter);
        Button clear = (Button) dateRangeView.findViewById(R.id.clear);

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTempStartDate != null && mTempEndDate != null) {
                    String sStartDate = mTempStartDate;
                    String sEndDate = mTempEndDate;
                    filterByDatesRange(sStartDate, sEndDate);
                    mTempStartDate = null;
                    mTempEndDate = null;
                    dialog.dismiss();
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
                mTempStartDate = null;
                mTempEndDate = null;
                dialog.dismiss();
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
        dialog.show();
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
            mFilterText = filterText.toLowerCase();
            mFilteredCourseList = new ArrayList<>();
            for (Course item : mCoursesList) {
                if (item.getName().toLowerCase().contains(mFilterText) ||
                        item.getDescription().toLowerCase().contains(mFilterText) ||
                        item.getSyllabus().toLowerCase().contains(mFilterText) || isHasCycle(item, mFilterText)) {
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
                searchResult = String.format(getString(R.string.no_results_for_filter), mTempStartDate, mTempEndDate);
            } else {
                searchResult = String.format(getString(R.string.no_results_for_query), query);
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