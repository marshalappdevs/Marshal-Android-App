package com.basmapp.marshal.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.basmapp.marshal.interfaces.ContentProviderCallBack;
import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.ui.adapters.CoursesSearchRecyclerAdapter;
import com.basmapp.marshal.util.ContentProvider;

import java.util.ArrayList;
import java.util.Calendar;

public class WishlistFragment extends Fragment {

    private RecyclerView mRecycler;
    private TextView mNoResults;
    private LinearLayout mEmptyWishlist, sErrorScreen;
    private SearchView mSearchView;
    private MenuItem mSearchItem;
    private CoursesSearchRecyclerAdapter mAdapter;
    private ArrayList<Course> mFilteredCourseList;
    private ArrayList<Course> mSubscriptionsList;
    private String mFilterText;
    private BroadcastReceiver mAdaptersBroadcastReceiver;
    private static final String WISHLIST_PREVIOUS_QUERY = "WISHLIST_PREVIOUS_QUERY";
    private String mPreviousQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wishlist, container, false);

        setHasOptionsMenu(true);

        mNoResults = (TextView) rootView.findViewById(R.id.fragment_wishlist_no_search_results);
        mEmptyWishlist = (LinearLayout) rootView.findViewById(R.id.empty_wishlist_linearLayout);

        // Initialize RecyclerView
        mRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_wishlist_search_recyclerView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());

        if (mAdapter == null) {
            if (mFilteredCourseList == null) {
                mFilteredCourseList = new ArrayList<>();
                mAdapter = new CoursesSearchRecyclerAdapter(getActivity(), mFilteredCourseList);
            }
        }

        if (mRecycler.getAdapter() == null)
            mRecycler.setAdapter(mAdapter);

        mAdaptersBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ContentProvider.Actions.COURSE_RATING_UPDATED)) {
                    Course course = intent.getParcelableExtra(ContentProvider.Extras.COURSE);
                    int itemPosition = ContentProvider.Utils.getCoursePositionInList(mSubscriptionsList, course);

                    if (itemPosition > -1)
                        mAdapter.notifyItemChanged(itemPosition);
                }
//                else if (intent.getAction().equals(ContentProvider.Actions.COURSE_SUBSCRIPTION_UPDATED)) {
//                    Course course = intent.getParcelableExtra(ContentProvider.Extras.COURSE);
//                    if (course.getIsUserSubscribe()) {
//                        mSubscriptionsList.add(course);
//                        mAdapter.notifyItemInserted(mSubscriptionsList.size() - 1);
//                    } else {
//                        int itemPosition = ContentProvider.Utils.getCoursePositionInList(mSubscriptionsList, course);
//                        if (itemPosition > -1)
//                            mSubscriptionsList.remove(itemPosition);
//                            mAdapter.notifyItemRemoved(itemPosition);
//                    }
//                }
            }
        };

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ContentProvider.Actions.COURSE_RATING_UPDATED);
        intentFilter.addAction(ContentProvider.Actions.COURSE_SUBSCRIPTION_UPDATED);
        getActivity().registerReceiver(mAdaptersBroadcastReceiver, intentFilter);

        ContentProvider.getInstance().getSubscribedCourses(getContext(), new ContentProviderCallBack() {
            @Override
            public void onDataReady(ArrayList<? extends DBObject> data, Object extra) {
                mSubscriptionsList = (ArrayList<Course>) data;
                if (mSubscriptionsList.isEmpty()) {
                    if (mSearchItem != null)
                        mSearchItem.setVisible(false);
                    if (sErrorScreen != null)
                        if (sErrorScreen.getVisibility() == View.VISIBLE) {
                            mEmptyWishlist.setVisibility(View.GONE);
                        } else {
                            mEmptyWishlist.setVisibility(View.VISIBLE);
                        }
                } else if (!mSubscriptionsList.isEmpty()) {
                    if (mSearchItem != null)
                        mSearchItem.setVisible(true);
                    mEmptyWishlist.setVisibility(View.GONE);
                }
                filter("");
            }

            @Override
            public void onError(Exception e) {
                mSubscriptionsList = new ArrayList<>();
                if (mSubscriptionsList.isEmpty()) {
                    if (mSearchItem != null)
                        mSearchItem.setVisible(false);
                    mEmptyWishlist.setVisibility(View.VISIBLE);
                } else if (!mSubscriptionsList.isEmpty()) {
                    if (mSearchItem != null)
                        mSearchItem.setVisible(true);
                    mEmptyWishlist.setVisibility(View.GONE);
                }
                filter("");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mAdaptersBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save SearchView query if possible
        if (mSearchView != null) {
            outState.putString(WISHLIST_PREVIOUS_QUERY, mSearchView.getQuery().toString());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore previous SearchView query
            mPreviousQuery = savedInstanceState.getString(WISHLIST_PREVIOUS_QUERY);
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
        mSearchItem = menu.findItem(R.id.m_search);
        mSearchView = (SearchView) mSearchItem.getActionView();
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
        MenuItemCompat.setOnActionExpandListener(mSearchItem,
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
            search(mPreviousQuery);
            filter(mPreviousQuery);
            mSearchView.clearFocus();
        }
    }

    private void filter(String filterText) {
        if (filterText == null) {
            mFilteredCourseList = new ArrayList<>(mSubscriptionsList);
        } else if (filterText.equals("*")) {
            mFilteredCourseList = new ArrayList<>(mSubscriptionsList);
        } else {
            mFilterText = filterText.toLowerCase();
            mFilteredCourseList = new ArrayList<>();
            for (Course item : mSubscriptionsList) {
                if (item.getName().toLowerCase().contains(mFilterText) ||
                        item.getDescription().toLowerCase().contains(mFilterText) ||
                        item.getSyllabus().toLowerCase().contains(mFilterText) || isHasCycle(item, mFilterText)) {
                    mFilteredCourseList.add(item);
                }
            }
        }
        showResults(filterText, mFilteredCourseList, false);
    }

    public void search(String query) {
        if (mSearchView != null && mSearchItem != null) {
            MenuItemCompat.expandActionView(mSearchItem);
            mSearchView.setQuery(query, true);
        }
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
