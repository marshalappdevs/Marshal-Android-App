package com.basmach.marshal.ui.fragments;

import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.entities.Rating;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.ui.adapters.CoursesSearchRecyclerAdapter;
import com.basmach.marshal.ui.utils.SuggestionProvider;
import com.basmach.marshal.utils.DateHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.http.POST;

public class CoursesSearchableFragment extends Fragment {

    public static final String EXTRA_SEARCH_QUERY = "search_query";
    public static final String EXTRA_ALL_COURSES = "all_courses";
    private static final String EXTRA_IS_MEETUPS = "extra_is_meetups";

    private SearchView mSearchView;
    private RecyclerView mRecycler;
    private LinearLayoutManager mLayoutManager;
    private CoursesSearchRecyclerAdapter mAdapter;
    private ArrayList<Course> mCoursesList;
    private ArrayList<Course> mFilteredCourseList;
    private String mFilterText;
    private String mSearchQuery;
    private TextView mNoResults;
    private MenuItem mRefreshMenuItem;
    private boolean mIsMeetups;
    private ImageButton mExpandFilter;
    private ImageButton mCollapseFilter;
    private LinearLayout mFilterDates;
    private EditText mStartDate;
    private EditText mEndDate;
    private Calendar mCalendar;
    private Button mApplyFilter;
    private View mAdvancedFilter;

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

        mNoResults = (TextView) rootView.findViewById(R.id.no_results);

        mFilterDates = (LinearLayout) rootView.findViewById(R.id.filter_dates);
        mExpandFilter = (ImageButton) rootView.findViewById(R.id.expand_dates_filter);
        mCollapseFilter = (ImageButton) rootView.findViewById(R.id.collapse_dates_filter);
        mStartDate = (EditText) rootView.findViewById(R.id.start_filter);
        mEndDate = (EditText) rootView.findViewById(R.id.end_filter);
        mApplyFilter = (Button) rootView.findViewById(R.id.apply_filter_dates);
        mAdvancedFilter = rootView.findViewById(R.id.courses_filter);

        mSearchQuery = getArguments().getString(EXTRA_SEARCH_QUERY);
        mCoursesList = getArguments().getParcelableArrayList(EXTRA_ALL_COURSES);

        mIsMeetups = getArguments().getBoolean(EXTRA_IS_MEETUPS);

        if (!mIsMeetups) {

            initializeAdvancedFilter();

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
            mAdvancedFilter.setVisibility(View.GONE);
        } else {
            getActivity().setTitle(R.string.search_title);
            mAdvancedFilter.setVisibility(View.VISIBLE);
        }

        return rootView;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        mRefreshMenuItem = menu.findItem(R.id.menu_main_refresh);
        // Setup search button
        MenuItem searchItem = menu.findItem(R.id.menu_main_searchView);
        mSearchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
        {
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
                filter(query);
                mSearchView.clearFocus();
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                suggestions.saveRecentQuery(query, null);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                filter(newText);
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        if (!mIsMeetups) {
                            getActivity().onBackPressed();
                        }
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        mRefreshMenuItem.setVisible(false);
                        return true; // Return true to expand action view
                    }
                });
        if (!mIsMeetups) {
            MenuItemCompat.expandActionView(searchItem);
            mSearchView.setQuery(mSearchQuery,true);
        }
    }

    private void initializeAdvancedFilter() {
        mExpandFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilterDates.setVisibility(View.VISIBLE);
                mCollapseFilter.setVisibility(View.VISIBLE);
                mExpandFilter.setVisibility(View.GONE);
            }
        });

        mCollapseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilterDates.setVisibility(View.GONE);
                mCollapseFilter.setVisibility(View.GONE);
                mExpandFilter.setVisibility(View.VISIBLE);
            }
        });

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
                mStartDate.setText(sdf.format(mCalendar.getTime()));
            }
        };

        mStartDate.setOnClickListener(new View.OnClickListener() {
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
                mEndDate.setText(sdf.format(mCalendar.getTime()));
            }
        };

        mEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), endDate, mCalendar
                        .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        mApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStartDate == null || mStartDate.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), R.string.start_date_error, Toast.LENGTH_SHORT).show();
                }

                if (mEndDate == null || mEndDate.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), R.string.end_date_error, Toast.LENGTH_SHORT).show();
                }

                if (mStartDate != null && !mStartDate.getText().toString().isEmpty()
                        && mEndDate != null && !mEndDate.getText().toString().isEmpty()) {
                    mFilterDates.setVisibility(View.GONE);
                    mCollapseFilter.setVisibility(View.GONE);
                    mExpandFilter.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), mStartDate.getText().toString() + " " + mEndDate.getText().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filter(String filterText) {
        if (filterText == null) {
            mFilteredCourseList = new ArrayList<>(mCoursesList);
        } else if (filterText.equals("")) {
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
        if (mFilteredCourseList.isEmpty()) {
            String searchResult = String.format(getString(R.string.no_results_for_query), filterText);

            mNoResults.setText(searchResult);
            mNoResults.setGravity(Gravity.CENTER);
            mNoResults.setVisibility(View.VISIBLE);
        } else {
            mNoResults.setVisibility(View.GONE);
        }
        mAdapter.animateTo(mFilteredCourseList);
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
