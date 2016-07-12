package com.basmach.marshal.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.lapism.searchview.SearchView;
import android.util.Log;
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
import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.interfaces.OnHashTagClickListener;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.ui.MainActivity;
import com.basmach.marshal.ui.adapters.MaterialsRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MaterialsFragment extends Fragment {

    private SearchView mSearchView;
    private ProgressBar mProgressBar;
    private RecyclerView mRecycler;
    private LinearLayoutManager mLayoutManager;
    private MaterialsRecyclerAdapter mAdapter;
    private ArrayList<MaterialItem> mMaterialsList;
    private ArrayList<MaterialItem> mFilteredMaterilsList;
    private String mFilterText;
    private TextView mNoResults;
    private MenuItem mSearchMenuItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_materials, container, false);

        setHasOptionsMenu(true);

        mSearchView = ((MainActivity)getActivity()).getSearchView();

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.materials_progressBar);
        mRecycler = (RecyclerView) rootView.findViewById(R.id.materials_recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        mNoResults = (TextView) rootView.findViewById(R.id.no_results);

        if (mMaterialsList == null) {
            MaterialItem.getAllInBackground(DBConstants.COL_TITLE, MaterialItem.class, getActivity(),
                    false, new BackgroundTaskCallBack() {
                        @Override
                        public void onSuccess(String result, List<Object> data) {
                            mMaterialsList = new ArrayList<>();
                            for(Object item:data) {
                                mMaterialsList.add((MaterialItem)item);

                            }
                            showData();
                        }

                        @Override
                        public void onError(String error) {
                            if (error != null) {
                                Log.e("GET MATERIALS "," ERROR:\n" + error);
                            } else {
                                Log.e("GET MATERIALS "," ERROR");
                            }
                        }
                    });
        } else {
            showData();
        }

        return rootView;
    }

    private void showData() {
        mFilteredMaterilsList = new ArrayList<>(mMaterialsList);
        OnHashTagClickListener onHashTagClickListener = new OnHashTagClickListener() {
            @Override
            public void onClick(String hashTag) {
                if (mSearchView != null && mSearchMenuItem != null) {
                    MenuItemCompat.expandActionView(mSearchMenuItem);
//                    mSearchView.setQuery(hashTag, true);
                    mSearchView.setQuery(hashTag);
                }
            }
        };

        mAdapter = new MaterialsRecyclerAdapter(getActivity(), mFilteredMaterilsList, onHashTagClickListener);
        mRecycler.setAdapter(mAdapter);

        if(getArguments() != null) {
            String courseCode = getArguments().getString(MainActivity.EXTRA_COURSE_CODE);
            if (courseCode != null && !(courseCode.equals(""))) {
                search(courseCode);
            }
        }
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
//        inflater.inflate(R.menu.main, menu);

        // Setup search button
        mSearchMenuItem = menu.findItem(R.id.menu_main_searchView);
//        mSearchView = (SearchView) mSearchMenuItem.getActionView();
//        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                mSearchView.clearFocus();
                ((MainActivity)getActivity()).addSearchHistory(query);
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
    }

    private void filter(String filterText) {
        if (filterText == null) {
            mFilteredMaterilsList = new ArrayList<>(mMaterialsList);
            mAdapter.setIsDataFiltered(false);
        } else if (filterText.equals("")) {
            mFilteredMaterilsList = new ArrayList<>(mMaterialsList);
            mAdapter.setIsDataFiltered(false);
        } else {
            mFilterText = filterText.toLowerCase();
            mFilteredMaterilsList = new ArrayList<>();

                for(MaterialItem item:mMaterialsList) {
                    if (item.getTitle() != null && item.getTitle().contains(filterText)) {
                       mFilteredMaterilsList.add(item);
                    } else if (item.getDescription() != null && item.getDescription().contains(filterText)) {
                        mFilteredMaterilsList.add(item);
                    } else if (item.getTags() != null && item.getTags().contains(filterText)) {
                        mFilteredMaterilsList.add(item);
                    }
                }
        }

        if (mFilteredMaterilsList.isEmpty()) {
            String searchResult = String.format(getString(R.string.no_results_for_query), mFilterText);
            mNoResults.setText(searchResult);
            mNoResults.setGravity(Gravity.CENTER);
            mNoResults.setVisibility(View.VISIBLE);
        } else {
            mNoResults.setVisibility(View.GONE);
        }

        mAdapter.setIsDataFiltered(true);
        mAdapter.animateTo(mFilteredMaterilsList);
        mRecycler.scrollToPosition(0);
    }

    public void search(String query) {
        if (mSearchView != null && mSearchMenuItem != null) {
            MenuItemCompat.expandActionView(mSearchMenuItem);
//            mSearchView.setQuery(query, true);
            mSearchView.setQuery(query);
        }
    }

    public static MaterialsFragment newInstanceWithQuery(String courseCode) {
        MaterialsFragment materialsFragment = new MaterialsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.EXTRA_COURSE_CODE, courseCode);
        materialsFragment.setArguments(bundle);
        return materialsFragment;
    }
}
