package com.basmach.marshal.ui.fragments;

import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.basmach.marshal.ui.MainActivity;
import com.lapism.searchview.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.ui.adapters.CoursesSearchRecyclerAdapter;
import com.basmach.marshal.utils.DateHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class CoursesSearchableFragment extends Fragment {

    public static final String EXTRA_SEARCH_QUERY = "search_query";
    public static final String EXTRA_ALL_COURSES = "all_courses";
    private static final String EXTRA_IS_MEETUPS = "extra_is_meetups";
    private static final String FILTER_SHOWCASE_ID = "filter_tutorial";

    private SearchView mSearchView;
    private RecyclerView mRecycler;
    private LinearLayoutManager mLayoutManager;
    private CoursesSearchRecyclerAdapter mAdapter;
    private FloatingActionButton mFabSorting;

    private ArrayList<Course> mCoursesList;
    private ArrayList<Course> mFilteredCourseList;

    private String mFilterText;
    private String mSearchQuery;
    private TextView mNoResults;
    private boolean mIsMeetups;
    private Calendar mCalendar;
    private String mTempStartDate;
    private String mTempEndDate;
    private long tempStartDate = 0;
    private boolean isEmptyResult = false;

    public static CoursesSearchableFragment newInstance(String query, ArrayList<Course> courses,
                                                        boolean isMeetups) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SEARCH_QUERY,query);
        bundle.putParcelableArrayList(EXTRA_ALL_COURSES,courses);
        bundle.putBoolean(EXTRA_IS_MEETUPS, isMeetups);
        CoursesSearchableFragment coursesSearchableFragment = new CoursesSearchableFragment();
        coursesSearchableFragment.setArguments(bundle);
        return coursesSearchableFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_courses_search, container, false);

        setHasOptionsMenu(true);

        mRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_courses_search_recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());

        mFabSorting = (FloatingActionButton) rootView.findViewById(R.id.course_searchable_sort_fab);

        mNoResults = (TextView) rootView.findViewById(R.id.no_results);

        mSearchQuery = getArguments().getString(EXTRA_SEARCH_QUERY);
        mCoursesList = getArguments().getParcelableArrayList(EXTRA_ALL_COURSES);

        mIsMeetups = getArguments().getBoolean(EXTRA_IS_MEETUPS);

        mFabSorting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isEmptyResult)
                    showFilterByDateDialog();
                else {
                    Toast.makeText(getActivity(), R.string.filter_not_available, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (mIsMeetups) {
            mSearchView = ((MainActivity) getActivity()).getSearchView(false, false);
            mFabSorting.setVisibility(View.GONE);
        } else {
            mSearchView = ((MainActivity) getActivity()).getSearchView(true, true);
            mFabSorting.setVisibility(View.VISIBLE);
        }

        if (!mIsMeetups) {

            if (mCoursesList != null)
                mFilteredCourseList = new ArrayList<>(mCoursesList);
            else
                mFilteredCourseList = new ArrayList<>();

            if (mAdapter == null)
                mAdapter = new CoursesSearchRecyclerAdapter(getActivity(), mFilteredCourseList);

            if (mRecycler.getAdapter() == null)
                mRecycler.setAdapter(mAdapter);
        } else {
            if (mAdapter == null) {
                if (mFilteredCourseList == null) {
                    mFilteredCourseList = new ArrayList<>();
                    mAdapter = new CoursesSearchRecyclerAdapter(getActivity(), mFilteredCourseList);
                }
            }

            if (mRecycler.getAdapter() == null)
                mRecycler.setAdapter(mAdapter);

            if (mCoursesList == null) {
                Course.getByColumnInBackground(true, DBConstants.COL_IS_MEETUP, true, DBConstants.COL_ID,
                        getActivity(), Course.class, new BackgroundTaskCallBack() {
                            @Override
                            public void onSuccess(String result, List<Object> data) {
                                if (data != null && data.size() > 0) {
                                    mCoursesList = new ArrayList<>((List)data);
                                } else {
                                    mCoursesList = new ArrayList<>();
                                }

                                filter("");
                            }

                            @Override
                            public void onError(String error) {
                                mCoursesList = new ArrayList<>();
                                filter("");
                            }
                        });
            }
        }

        if (mIsMeetups) {
            getActivity().setTitle(R.string.navigation_drawer_meetups);
        } else {
            getActivity().setTitle(R.string.search_title);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                View filterView = null;
                if (getActivity() != null) {
                    filterView = getActivity().findViewById(R.id.menu_main_filter);
                }
                if (filterView != null && filterView.getVisibility() == View.VISIBLE) {
                    new MaterialShowcaseView.Builder(getActivity())
                            .setTarget(filterView)
                            .setShapePadding(24)
                            .setDismissText(R.string.got_it)
                            .setDismissOnTouch(false)
                            .setDismissOnTargetTouch(true)
                            .setTargetTouchable(true)
                            .setTitleText(R.string.filter_tutorial_description)
//                            .setMaskColour(Color.argb(210, 0, 0, 0))
                            .singleUse(FILTER_SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                            .show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSearchView.close(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        if (mAdapter != null && mRecycler != null) {
            mRecycler.setAdapter(mAdapter);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Setup filter button
        if (!mIsMeetups)
            menu.findItem(R.id.menu_main_filter).setVisible(true);

        MenuItem filterItem = menu.findItem(R.id.menu_main_filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!isEmptyResult)
                    showFilterByDateDialog();
                else {
                    Toast.makeText(getActivity(), R.string.filter_not_available, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        // Setup search button
        MenuItem searchItem = menu.findItem(R.id.menu_main_searchView);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        mSearchView = (SearchView) searchItem.getActionView();
//        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        mSearchView.setIconifiedByDefault(true);
//        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
//        {
//            @Override
//            public boolean onSuggestionClick(int position) {
//                String suggestion = getSuggestion(position);
//                mSearchView.setQuery(suggestion, true);
//                mSearchView.clearFocus();
//                return true;
//            }
//
//            private String getSuggestion(int position) {
//                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
//                return cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
//            }
//
//            @Override
//            public boolean onSuggestionSelect(int position) {
//                return false;
//            }
//        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mFilterText = query;
                filter(query);
                mSearchView.clearFocus();
//                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
//                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
//                suggestions.saveRecentQuery(query, null);
                ((MainActivity)getActivity()).addSearchHistory(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mIsMeetups) {
                    mFilterText = newText;
                    filter(newText);
                }
                return true;
            }
        });

        mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
            @Override
            public void onClose() {
                if (!mIsMeetups) {
                    getActivity().onBackPressed();
                    mSearchView.setOnOpenCloseListener(null);
                }
            }

            @Override
            public void onOpen() {

            }
        });

//        MenuItemCompat.setOnActionExpandListener(searchItem,
//                new MenuItemCompat.OnActionExpandListener() {
//                    @Override
//                    public boolean onMenuItemActionCollapse(MenuItem item) {
//                        if (!mIsMeetups) {
//                            getActivity().onBackPressed();
//                        }
//                        return true; // Return true to collapse action view
//                    }
//
//                    @Override
//                    public boolean onMenuItemActionExpand(MenuItem item) {
//                        return true; // Return true to expand action view
//                    }
//                });
        if (!mIsMeetups) {
//            mSearchView.open(true);
//            MenuItemCompat.expandActionView(searchItem);
//            mSearchView.setQuery(mSearchQuery,true);
            mSearchView.setQuery(mSearchQuery);
        }
    }

    public void showFilterByDateDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View dialogView = layoutInflater.inflate(R.layout.filter_dialog, null);
        alertDialog.setView(dialogView);
        alertDialog.setTitle(getString(R.string.date_filter));

        final TextView startDateSpinner = (TextView) dialogView.findViewById(R.id.start_date_spinner);
        final TextView endDateSpinner = (TextView) dialogView.findViewById(R.id.end_date_spinner);
        final Button negativeButton = (Button) dialogView.findViewById(R.id.negative_button);
        final Button positiveButton = (Button) dialogView.findViewById(R.id.positive_button);

        if (mTempStartDate != null && !mTempStartDate.isEmpty())
            startDateSpinner.setText(mTempStartDate);

        if (mTempEndDate != null && !mTempEndDate.isEmpty())
            endDateSpinner.setText(mTempEndDate);

        mCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                // update text field
                String myFormat = "dd/MM/yy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
                startDateSpinner.setText(sdf.format(mCalendar.getTime()));
                mTempStartDate = (sdf.format(mCalendar.getTime()));
                mCalendar.getTime();
                tempStartDate = mCalendar.getTimeInMillis();
            }
        };

        startDateSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), startDate, mCalendar
                        .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        final DatePickerDialog.OnDateSetListener endDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                // update text field
                String myFormat = "dd/MM/yy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
                endDateSpinner.setText(sdf.format(mCalendar.getTime()));
                mTempEndDate = (sdf.format(mCalendar.getTime()));
            }
        };

        endDateSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), endDate, mCalendar
                        .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH));
                if (tempStartDate != 0) {
                    datePickerDialog.getDatePicker().setMinDate(tempStartDate);
                } else {
                    datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                }
                datePickerDialog.show();
            }
        });

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startDateSpinner.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), R.string.start_date_error, Toast.LENGTH_SHORT).show();
                }

                if (endDateSpinner.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), R.string.end_date_error, Toast.LENGTH_SHORT).show();
                }

                if (!startDateSpinner.getText().toString().isEmpty()
                        && !endDateSpinner.getText().toString().isEmpty()) {
                    String sStartDate = startDateSpinner.getText().toString();
                    String sEndDate = endDateSpinner.getText().toString();
                    filterByDatesRange(sStartDate, sEndDate);
                    alertDialog.dismiss();
                }
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
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
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void filterByDatesRange(String sStartDate, String sEndDate) {

        ArrayList<Course> currentFilteredList = new ArrayList<>();

        try {
            if (mFilteredCourseList != null && mFilteredCourseList.size() > 0) {
                long rangeStartTime = DateHelper.stringToDate(sStartDate).getTime();
                long rangeEndTime = DateHelper.stringToDate(sEndDate).getTime();

                for (Course course:mFilteredCourseList) {
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
            for(Course item:mCoursesList) {
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
                searchResult = getString(R.string.no_results_for_filter);
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

//        filterText = filterText.replace(".","/");
//
//        int slashIndex = 0;
//
//        slashIndex = filterText.substring(slashIndex, filterText.length()).indexOf("/");
//        if (slashIndex > -1) {
//            if(!(filterText.substring(slashIndex + 1 ,slashIndex + 2).equals("0"))) {
//                filterText = filterText.substring(0, slashIndex + 1) + "0" + filterText.substring(slashIndex + 1, filterText.length());
//            }
//        }
//
//        if (course.getCycles() == null || course.getCycles().size() == 0) {
//            return false;
//        } else {
//            for (Cycle cycle : course.getCycles()) {
//                if(DateHelper.dateToString(cycle.getStartDate()).contains(filterText) ||
//                        DateHelper.dateToString(cycle.getEndDate()).contains(filterText)) {
//                    return true;
//                }
//            }
//        }

        if (course.getCycles() == null || course.getCycles().size() == 0) {
            return false;
        } else {
            for (Cycle cycle : course.getCycles()) {
                if(isTextIncludeInCycle(cycle, filterText)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isTextIncludeInCycle(Cycle cycle, String text) {

        int day, month;
        text = text.replace(".","/");
        String[] textParts = text.split("/");

        if(textParts.length == 1) {
            try {
//                if(DateHelper.dateToString(cycle.getStartDate()).contains(text) ||
//                        DateHelper.dateToString(cycle.getEndDate()).contains(text)) {
//                    return true;
//                }
                Calendar startCalendar, endCalendar;
                int searchNumber, startDay, endDay, startMonth, endMonth;
                searchNumber = Integer.valueOf(text);

                startCalendar = Calendar.getInstance();
                startCalendar.setTime(cycle.getStartDate());
                startDay = startCalendar.get(Calendar.DAY_OF_MONTH);
                startMonth= startCalendar.get(Calendar.MONTH);

                endCalendar = Calendar.getInstance();
                endCalendar.setTime(cycle.getEndDate());
                endDay = startCalendar.get(Calendar.DAY_OF_MONTH);
                endMonth= startCalendar.get(Calendar.MONTH);

                if((searchNumber >= startDay || searchNumber <= endDay) ||
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
