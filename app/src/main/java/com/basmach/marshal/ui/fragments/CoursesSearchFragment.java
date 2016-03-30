package com.basmach.marshal.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.ui.utils.CoursesSearchRecyclerAdapter;

import java.util.ArrayList;

public class CoursesSearchFragment extends Fragment {

    public static final String EXTRA_SEARCH_QUERY = "search_query";
    public static final String EXTRA_ALL_COURSES = "all_courses";

    private SearchView mSearchView;
    private RecyclerView mRecycler;
    private LinearLayoutManager mLayoutManager;
    private CoursesSearchRecyclerAdapter mAdapter;
    private ArrayList<Course> mCoursesList;
    private ArrayList<Course> mFilteredCourseList;
    private String mFilterText;
    private String mSearchQuery;
    private TextView mNoResults;

    public static CoursesSearchFragment newInstance(String query,
                                                    ArrayList<Course> courses) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SEARCH_QUERY,query);
        bundle.putParcelableArrayList(EXTRA_ALL_COURSES,courses);
        CoursesSearchFragment coursesSearchFragment = new CoursesSearchFragment();
        coursesSearchFragment.setArguments(bundle);
        return coursesSearchFragment;
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
        mSearchQuery = getArguments().getString(EXTRA_SEARCH_QUERY);
        mCoursesList = getArguments().getParcelableArrayList(EXTRA_ALL_COURSES);

        mFilteredCourseList = new ArrayList<>(mCoursesList);
        mAdapter = new CoursesSearchRecyclerAdapter(getActivity(), mFilteredCourseList);
        mRecycler.setAdapter(mAdapter);

        getActivity().setTitle(R.string.search_title);

        return rootView;
    }

//    private void showData() {
//
//    }

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

        // Setup search button
        final MenuItem searchItem = menu.findItem(R.id.menu_main_searchView);
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
                filter(newText);
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true; // Return true to expand action view
                    }
                });
        MenuItemCompat.expandActionView(searchItem);
        mSearchView.setQuery(mSearchQuery,true);
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
                        item.getDescription().toLowerCase().contains(mFilterText)) {
                    mFilteredCourseList.add(item);
                }
            }
        }
        if (mFilteredCourseList.isEmpty()) {
            String searchResult = String.format(getString(R.string.no_results_for_query), mFilterText);
            mNoResults.setText(searchResult);
            mNoResults.setGravity(Gravity.CENTER);
            mNoResults.setVisibility(View.VISIBLE);
        } else {
            mNoResults.setVisibility(View.GONE);
        }
        mAdapter.animateTo(mFilteredCourseList);
        mRecycler.scrollToPosition(0);
    }
}
