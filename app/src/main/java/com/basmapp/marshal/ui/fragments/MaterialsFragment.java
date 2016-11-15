package com.basmapp.marshal.ui.fragments;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.MaterialItem;
import com.basmapp.marshal.interfaces.ContentProviderCallBack;
import com.basmapp.marshal.interfaces.OnHashTagClickListener;
import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.ui.adapters.MaterialsRecyclerAdapter;
import com.basmapp.marshal.util.ContentProvider;

import java.util.ArrayList;

public class MaterialsFragment extends Fragment {
    private SearchView mSearchView;
    private ProgressBar mProgressBar;
    private RecyclerView mRecycler;
    private LinearLayoutManager mLayoutManager;
    private MaterialsRecyclerAdapter mAdapter;
    private ArrayList<MaterialItem> mMaterialsList;
    private ArrayList<MaterialItem> mFilteredMaterialsList;
    private String mFilterText;
    private TextView mNoResults;
    private MenuItem mSearchMenuItem;
    private boolean mIsRunForCourse;
    private int mCourseID;
    private static final String MATERIALS_PREVIOUS_QUERY = "MATERIALS_PREVIOUS_QUERY";
    private String mPreviousQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_materials, container, false);

        setHasOptionsMenu(true);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.materials_progressBar);
        mRecycler = (RecyclerView) rootView.findViewById(R.id.materials_recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        mNoResults = (TextView) rootView.findViewById(R.id.materials_no_results);

        // Hide keyboard while scrolling
        mRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide only if there are results
                if (mFilteredMaterialsList != null && !mFilteredMaterialsList.isEmpty() && mSearchView != null) {
                    mSearchView.clearFocus();
                }
                return false;
            }
        });

        if (getArguments() != null) {
            mIsRunForCourse = getArguments().getBoolean(Constants.EXTRA_IS_RUN_FOR_COURSE);
//            mCourseID = getArguments().getInt(Constants.EXTRA_COURSE_ID);
        }

        if (!mIsRunForCourse) {
            if (mMaterialsList == null) {
                ContentProvider.getInstance().getMaterialItems(getContext(), new ContentProviderCallBack() {
                    @Override
                    public void onDataReady(ArrayList<? extends DBObject> data, Object extra) {
                        mMaterialsList = (ArrayList<MaterialItem>) data;
                        setProgressBarVisibility(View.GONE);
                        showData();
                    }

                    @Override
                    public void onError(Exception e) {
                        setProgressBarVisibility(View.GONE);
                    }
                });
            }
        } else {
            mProgressBar.setVisibility(View.VISIBLE);

            if (getArguments() != null) {
                mCourseID = getArguments().getInt(Constants.EXTRA_COURSE_ID);
                mMaterialsList = getArguments().getParcelableArrayList(Constants.EXTRA_COURSE_MATERIALS_LIST);

                if (mCourseID != 0 && mMaterialsList != null) {
                    showData();
                }

                mProgressBar.setVisibility(View.GONE);
            }
        }

        return rootView;
    }

    private void setProgressBarVisibility(int visibility) {
        mProgressBar.setVisibility(visibility);
    }

    private void showData() {
        mFilteredMaterialsList = new ArrayList<>(mMaterialsList);
        OnHashTagClickListener onHashTagClickListener = new OnHashTagClickListener() {
            @Override
            public void onClick(String hashTag) {
                if (mSearchView != null && mSearchMenuItem != null) {
                    MenuItemCompat.expandActionView(mSearchMenuItem);
                    mSearchView.setQuery(hashTag, true);
                }
            }
        };

        mAdapter = new MaterialsRecyclerAdapter(getActivity(), mFilteredMaterialsList, onHashTagClickListener);
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save SearchView query if possible
        if (mSearchView != null) {
            outState.putString(MATERIALS_PREVIOUS_QUERY, mSearchView.getQuery().toString());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore previous SearchView query
            mPreviousQuery = savedInstanceState.getString(MATERIALS_PREVIOUS_QUERY);
        }
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
        if (mIsRunForCourse) {
            mSearchMenuItem = menu.findItem(R.id.course_materials_searchView);
        } else {
            mSearchMenuItem = menu.findItem(R.id.m_search);
        }

        mSearchView = (SearchView) mSearchMenuItem.getActionView();
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
        MenuItemCompat.setOnActionExpandListener(mSearchMenuItem,
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

        if (mMaterialsList != null) {
            showData();
        }
        if (mPreviousQuery != null && !mPreviousQuery.isEmpty()) {
            search(mPreviousQuery);
            filter(mPreviousQuery);
            mSearchView.clearFocus();
        }
    }

    private void filter(String filterText) {
        if (filterText == null) {
            mFilteredMaterialsList = new ArrayList<>(mMaterialsList);
            mAdapter.setIsDataFiltered(false);
        } else if (filterText.equals("")) {
            mFilteredMaterialsList = new ArrayList<>(mMaterialsList);
            mAdapter.setIsDataFiltered(false);
        } else {
            mFilterText = filterText.toLowerCase();
            mFilteredMaterialsList = new ArrayList<>();

            for (MaterialItem item : mMaterialsList) {
                if (item.getTitle() != null && item.getTitle().contains(filterText)) {
                    mFilteredMaterialsList.add(item);
                } else if (item.getDescription() != null && item.getDescription().contains(filterText)) {
                    mFilteredMaterialsList.add(item);
                } else if (item.getTags() != null && item.getTags().contains(filterText)) {
                    mFilteredMaterialsList.add(item);
                }
            }
        }

        if (mFilteredMaterialsList.isEmpty()) {
            String searchResult = String.format(getString(R.string.no_results_for_query), mFilterText);
            mNoResults.setText(searchResult);
            mNoResults.setGravity(Gravity.CENTER);
            mNoResults.setVisibility(View.VISIBLE);
        } else {
            mNoResults.setVisibility(View.GONE);
        }

        mAdapter.setIsDataFiltered(true);
        mAdapter.animateTo(mFilteredMaterialsList);
        mRecycler.scrollToPosition(0);
    }

    public void search(String query) {
        if (mSearchView != null && mSearchMenuItem != null) {
            MenuItemCompat.expandActionView(mSearchMenuItem);
            mSearchView.setQuery(query, true);
        }
    }

//    public static MaterialsFragment newInstanceWithQuery(String courseCode) {
//        MaterialsFragment materialsFragment = new MaterialsFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString(MainActivity.EXTRA_COURSE_CODE, courseCode);
//        materialsFragment.setArguments(bundle);
//        return materialsFragment;
//    }

    public static MaterialsFragment newInstanceForCourse(int courseID, ArrayList<MaterialItem> materials) {
        MaterialsFragment materialsFragment = new MaterialsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.EXTRA_COURSE_ID, courseID);
        bundle.putParcelableArrayList(Constants.EXTRA_COURSE_MATERIALS_LIST, materials);
        bundle.putBoolean(Constants.EXTRA_IS_RUN_FOR_COURSE, true);
        materialsFragment.setArguments(bundle);
        return materialsFragment;
    }
}