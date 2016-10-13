package com.basmapp.marshal.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.ui.adapters.CoursesSearchRecyclerAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MeetupsFragment extends Fragment {

    private RecyclerView mRecycler;
    private TextView mNoResults;
    private SearchView mSearchView;
    private CoursesSearchRecyclerAdapter mAdapter;
    private ArrayList<Course> mFilteredMeetupsList;
    private ArrayList<Course> mMeetupsList;
    private String mFilterText;
    private static final String MEETUPS_PREVIOUS_QUERY = "MEETUPS_PREVIOUS_QUERY";
    private String mPreviousQuery;
    private LinearLayout sErrorScreen;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_meetups, container, false);

        setHasOptionsMenu(true);

        mNoResults = (TextView) rootView.findViewById(R.id.meetup_no_results);

        // Initialize RecyclerView
        mRecycler = (RecyclerView) rootView.findViewById(R.id.meetup_recyclerView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());

        if (mAdapter == null) {
            if (mFilteredMeetupsList == null) {
                mFilteredMeetupsList = new ArrayList<>();
                mAdapter = new CoursesSearchRecyclerAdapter(getActivity(), mFilteredMeetupsList);
            }
        }

        if (mRecycler.getAdapter() == null)
            mRecycler.setAdapter(mAdapter);

        if (mMeetupsList == null) {
            if (MainActivity.sMeetups == null) {
                Course.getByColumnInBackground(true, DBConstants.COL_IS_MEETUP, true, DBConstants.COL_ID,
                        getActivity(), Course.class, new BackgroundTaskCallBack() {
                            @Override
                            public void onSuccess(String result, List<Object> data) {
                                if (data != null && data.size() > 0) {
                                    try {
                                        mMeetupsList = new ArrayList<>((ArrayList) data);
                                        MainActivity.sMeetups = mMeetupsList;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        mMeetupsList = new ArrayList<>();
                                    }
                                } else {
                                    mMeetupsList = new ArrayList<>();
                                }

                                filter("");
                            }

                            @Override
                            public void onError(String error) {
                                mMeetupsList = new ArrayList<>();
                                filter("");
                            }
                        });
            } else {
                mMeetupsList = new ArrayList<>(MainActivity.sMeetups);
                filter("");
            }
        } else {
            filter("");
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save SearchView query if possible
        if (mSearchView != null) {
            outState.putString(MEETUPS_PREVIOUS_QUERY, mSearchView.getQuery().toString());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore previous SearchView query
            mPreviousQuery = savedInstanceState.getString(MEETUPS_PREVIOUS_QUERY);
        }
        setHasOptionsMenu(true);
        if (mAdapter != null && mRecycler != null) {
            mRecycler.setAdapter(mAdapter);
        }
        sErrorScreen = (LinearLayout) getActivity().findViewById(R.id.placeholder_error);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Setup search button
        MenuItem searchItem = menu.findItem(R.id.menu_main_searchView);

        // Disable search if error screen shown
        if (sErrorScreen != null) {
            if (sErrorScreen.getVisibility() == View.VISIBLE) {
                searchItem.setEnabled(false);
            } else {
                searchItem.setEnabled(true);
            }
        }

        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setIconifiedByDefault(true);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mFilterText = newText;
                filter(newText);
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        filter(null);
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true; // Return true to expand action view
                    }
                });
        if (mPreviousQuery != null && !mPreviousQuery.isEmpty()) {
            MenuItemCompat.expandActionView(searchItem);
            mSearchView.setQuery(mPreviousQuery, true);
            filter(mPreviousQuery);
            mSearchView.clearFocus();
        }
    }

    private void filter(String filterText) {
        if (filterText == null) {
            mFilteredMeetupsList = new ArrayList<>(mMeetupsList);
        } else if (filterText.equals("*")) {
            mFilteredMeetupsList = new ArrayList<>(mMeetupsList);
        } else {
            mFilterText = filterText.toLowerCase();
            mFilteredMeetupsList = new ArrayList<>();
            for (Course item : mMeetupsList) {
                if (item.getName().toLowerCase().contains(mFilterText) ||
                        item.getDescription().toLowerCase().contains(mFilterText) ||
                        item.getSyllabus().toLowerCase().contains(mFilterText) || isHasCycle(item, mFilterText)) {
                    mFilteredMeetupsList.add(item);
                }
            }
        }
        showResults(filterText, mFilteredMeetupsList, false);
    }

    private void showResults(String query, ArrayList<Course> listToShow, boolean filter) {
        if (listToShow.isEmpty()) {
            String searchResult;
            if (filter) {
                searchResult = getString(R.string.no_results_for_filter);
                mNoResults.setText(searchResult);
                mNoResults.setVisibility(View.VISIBLE);
            } else if (query != null && !query.isEmpty()) {
                searchResult = String.format(getString(R.string.no_results_for_query), query);
                mNoResults.setText(searchResult);
                mNoResults.setVisibility(View.VISIBLE);
            } else {
                mNoResults.setVisibility(View.GONE);
            }
        } else {
            mNoResults.setVisibility(View.GONE);
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