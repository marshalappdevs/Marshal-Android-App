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

import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.FaqItem;
import com.basmapp.marshal.interfaces.ContentProviderCallBack;
import com.simplite.orm.DBObject;
import com.basmapp.marshal.ui.adapters.FaqRecyclerAdapter;
import com.basmapp.marshal.util.ContentProvider;

import java.util.ArrayList;

public class FaqFragment extends Fragment {
    private SearchView mSearchView;
    private MenuItem mSearchMenuItem;
    private ProgressBar mProgressBar;
    private RecyclerView mRecycler;
    private LinearLayoutManager mLayoutManager;
    private FaqRecyclerAdapter mAdapter;
    private ArrayList<FaqItem> mFaqList;
    private ArrayList<FaqItem> mFilteredFaqList;
    private TextView mNoResults;
    private static final String FAQ_PREVIOUS_QUERY = "FAQ_PREVIOUS_QUERY";
    private String mPreviousQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_faq, container, false);

        setHasOptionsMenu(true);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.faq_progressBar);
        mRecycler = (RecyclerView) rootView.findViewById(R.id.faq_recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        mNoResults = (TextView) rootView.findViewById(R.id.faq_no_results);

        // Hide keyboard while scrolling
        mRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide only if there are results
                if (mFilteredFaqList != null && !mFilteredFaqList.isEmpty() && mSearchView != null) {
                    mSearchView.clearFocus();
                }
                return false;
            }
        });

        if (mFaqList == null) {
            ContentProvider.getInstance().getFaqItems(getActivity(), new ContentProviderCallBack() {
                @Override
                public void onDataReady(ArrayList<? extends DBObject> data, Object extra) {
                    mFaqList = (ArrayList<FaqItem>) data;
                    setProgressBarVisibility(View.GONE);
                    showData();
                }

                @Override
                public void onError(Exception e) {
                    setProgressBarVisibility(View.GONE);
                }
            });
        }
        return rootView;
    }

    private void setProgressBarVisibility(int visibility) {
        mProgressBar.setVisibility(visibility);
    }

    private void showData() {
        mFilteredFaqList = new ArrayList<>(mFaqList);
        mAdapter = new FaqRecyclerAdapter(getActivity(), mFilteredFaqList);
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save SearchView query if possible
        if (mSearchView != null) {
            outState.putString(FAQ_PREVIOUS_QUERY, mSearchView.getQuery().toString());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore previous SearchView query
            mPreviousQuery = savedInstanceState.getString(FAQ_PREVIOUS_QUERY);
        }
        setHasOptionsMenu(true);
        if (mAdapter != null && mRecycler != null) {
            mRecycler.setAdapter(mAdapter);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        mSearchMenuItem = menu.findItem(R.id.m_search);

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

        if (mFaqList != null) {
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
            mFilteredFaqList = new ArrayList<>(mFaqList);
            mAdapter.setIsDataFiltered(false);
        } else if (filterText.equals("")) {
            mFilteredFaqList = new ArrayList<>(mFaqList);
            mAdapter.setIsDataFiltered(false);
        } else {
            mFilteredFaqList = new ArrayList<>();

            for (FaqItem item : mFaqList) {
                if (item.getQuestion() != null && item.getQuestion().toLowerCase().contains(filterText)) {
                    mFilteredFaqList.add(item);
                } else if (item.getAnswer() != null && item.getAnswer().toLowerCase().contains(filterText)) {
                    mFilteredFaqList.add(item);
                } else if (item.getAnswerLink() != null && item.getAnswerLink().toLowerCase().contains(filterText)) {
                    mFilteredFaqList.add(item);
                } else if (item.getSearchWords() != null && item.getSearchWords().toLowerCase().contains(filterText)) {
                    mFilteredFaqList.add(item);
                }
            }
        }

        if (mFilteredFaqList.isEmpty()) {
            String searchResult = String.format(getString(R.string.no_results_for_query), mSearchView.getQuery());
            mNoResults.setText(searchResult);
            mNoResults.setGravity(Gravity.CENTER);
            mNoResults.setVisibility(View.VISIBLE);
        } else {
            mNoResults.setVisibility(View.GONE);
        }

        mAdapter.setIsDataFiltered(true);
        mAdapter.animateTo(mFilteredFaqList);
        mRecycler.scrollToPosition(0);
    }

    public void search(String query) {
        if (mSearchView != null && mSearchMenuItem != null) {
            MenuItemCompat.expandActionView(mSearchMenuItem);
            mSearchView.setQuery(query, true);
        }
    }
}
