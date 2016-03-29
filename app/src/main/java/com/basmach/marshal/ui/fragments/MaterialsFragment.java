package com.basmach.marshal.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.ui.utils.MaterialsRecyclerAdapter;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;
import com.squareup.picasso.Picasso;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_materials, container, false);

        setHasOptionsMenu(true);

        if (mMaterialsList == null) {
            MaterialItem.getAllInBackground(DBConstants.COL_TITLE, MaterialItem.class, getActivity(),
                    false, new BackgroundTaskCallBack() {
                        @Override
                        public void onSuccess(String result, List<Object> data) {
                            mMaterialsList = new ArrayList<>();
                            for(Object item:data) {
                                Log.i("GET MATERIALS "," ITEM: " + ((MaterialItem)item).getTitle());
                                mMaterialsList.add((MaterialItem)item);
                                showData();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("GET MATERIALS "," ERROR");
                        }
                    });
        }

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.materials_progressBar);
        mRecycler = (RecyclerView) rootView.findViewById(R.id.materials_recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    private void showData() {
        mFilteredMaterilsList = new ArrayList<>(mMaterialsList);
        mAdapter = new MaterialsRecyclerAdapter(getActivity(), mFilteredMaterilsList);
        mRecycler.setAdapter(mAdapter);
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
                if (item.getTitle().toLowerCase().contains(mFilterText) ||
                        item.getDescription().toLowerCase().contains(mFilterText)) {
                    mFilteredMaterilsList.add(item);
                }
            }
        }

        mAdapter.setIsDataFiltered(true);
        mAdapter.animateTo(mFilteredMaterilsList);
        mRecycler.scrollToPosition(0);
    }
}
