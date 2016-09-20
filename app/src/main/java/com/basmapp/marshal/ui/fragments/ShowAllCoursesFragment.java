package com.basmapp.marshal.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.ui.adapters.CoursesRecyclerAdapter;

import java.util.ArrayList;

public class ShowAllCoursesFragment extends Fragment {

    private BroadcastReceiver mAdaptersBroadcastReceiver;

    RecyclerView mRecyclerView;
    GridLayoutManager mGridLayoutManager;
    CoursesRecyclerAdapter mAdapter;
    Toolbar mToolbar;

    String mCoursesType;
    ArrayList<Course> mCourses;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_show_all_courses, container, false);

        setHasOptionsMenu(true);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mCoursesType = bundle.getString(Constants.EXTRA_COURSE_TYPE);
            mCourses = bundle.getParcelableArrayList(Constants.EXTRA_COURSES_LIST);
        }

        MainActivity.mDrawerToggle.setDrawerIndicatorEnabled(false);

        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        if (mCoursesType != null && mToolbar != null)
            mToolbar.setTitle(mCoursesType);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        if (mCourses != null) {
            if (mCourses.size() > 0) {
                mRecyclerView = (RecyclerView) rootView.findViewById(R.id.showAllCourses_recyclerView);
                mGridLayoutManager = new GridLayoutManager(getActivity(), 3);
                mRecyclerView.setLayoutManager(mGridLayoutManager);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setHasFixedSize(true);
                mAdapter = new CoursesRecyclerAdapter(getActivity(), mCourses,
                        CoursesRecyclerAdapter.LAYOUT_TYPE_GRID);
                mRecyclerView.setAdapter(mAdapter);
            }
        }
        mAdaptersBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mAdapter.notifyDataSetChanged();
            }
        };

        getActivity().registerReceiver(mAdaptersBroadcastReceiver, new IntentFilter(CoursesRecyclerAdapter.ACTION_ITEM_DATA_CHANGED));

        return rootView;
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_main_searchView).setVisible(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mAdaptersBroadcastReceiver);
        MainActivity.mDrawerToggle.setDrawerIndicatorEnabled(true);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.navigation_drawer_courses);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                    drawerLayout.openDrawer(GravityCompat.START, false);
                }
            });
        }
    }
}
