package com.basmach.marshal.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.ui.ShowAllCoursesActivity;
import com.basmach.marshal.ui.utils.CoursesRecyclerAdapter;
import com.basmach.marshal.ui.utils.InkPageIndicator;
import com.basmach.marshal.ui.utils.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CoursesFragment extends Fragment {
    private static final String EXTRA_COURSES_LIST = "extra_courses_list";
    private static final String EXTRA_COURSES_IMAGES_LIST = "extra_courses_images_list";
    private static final String EXTRA_LAST_VIEWPAGER_POSITION = "extra_last_viewpager_position";

    public ArrayList<Course> mCoursesList = null;
    private ArrayList<String> mCoursesImages = null;

    private ViewPager mViewPager;
    private TimerTask mTimerTask;
    private Timer mTimer;
    private Handler mTimerTaskHandler = new Handler();

    private RecyclerView mRecyclerSoftware;
    private LinearLayoutManager mLinearLayoutManagerSoftware;
    private CoursesRecyclerAdapter mRecyclerAdapterSoftware;
    private Button mBtnShowAllSoftware;

    private RecyclerView mRecyclerCyber;
    private LinearLayoutManager mLinearLayoutManagerCyber;
    private CoursesRecyclerAdapter mRecyclerAdapterCyber;
    private Button mBtnShowAllCyber;

    private RecyclerView mRecyclerIT;
    private LinearLayoutManager mLinearLayoutManagerIT;
    private CoursesRecyclerAdapter mRecyclerAdapterIT;
    private Button mBtnShowAllIT;
    
    private View mRootView;
    private InkPageIndicator mInkPageIndicator;
    private ViewPagerAdapter mViewPagerAdapter;

    private SearchView mSearchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_courses, container, false);

        setHasOptionsMenu(true);

        mViewPager = (ViewPager) mRootView.findViewById(R.id.main_catalog_view_pager);

        if (mCoursesList == null || mCoursesImages == null) {

            if (savedInstanceState != null) {
                mCoursesList = savedInstanceState.getParcelableArrayList(EXTRA_COURSES_LIST);
                mCoursesImages = savedInstanceState.getStringArrayList(EXTRA_COURSES_IMAGES_LIST);
            }

            if (mCoursesList == null || mCoursesImages == null) {
                mCoursesImages = new ArrayList<>();
                mCoursesList = new ArrayList<>();
                Course.getAllInBackground(DBConstants.COL_NAME, Course.class, getActivity(), true,
                        new BackgroundTaskCallBack() {
                            @Override
                            public void onSuccess(String result, List<Object> data) {
                                for(Object item:data) {
                                    Log.i("GET COURSES "," ITEM: " + ((Course)item).getName());
                                    mCoursesList.add((Course)item);
                                    mCoursesImages.add(((Course) item).getImageUrl());
                                }
                                showImagesViewPager();
                                showData();
                            }

                            @Override
                            public void onError(String error) {

                            }
                        });
            } else {
                showImagesViewPager();
                showData();
            }
        } else {
            showImagesViewPager();
            showData();
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public void onStop() {
        super.onStop();
        mViewPager.setCurrentItem(0);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void showImagesViewPager() {
        mViewPagerAdapter = new ViewPagerAdapter(getActivity(), mCoursesImages);
        mViewPager.setAdapter(mViewPagerAdapter);
        mInkPageIndicator = (InkPageIndicator) mRootView.findViewById(R.id.main_catalog_indicator);
        mInkPageIndicator.setVisibility(View.VISIBLE);
        mInkPageIndicator.setViewPager(mViewPager);

        startViewPagerTimer();
        stopViewPagerTimerOnTouch();
    }

    private void showData() {
        initializeSoftwareComponents();
        initializeCyberComponents();
        initializeITComponents();
    }

    private void initializeSoftwareComponents() {
        mBtnShowAllSoftware = (Button) mRootView.findViewById(R.id.fragment_courses_software_seeAll);
        mBtnShowAllSoftware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST,mCoursesList);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_software));
                startActivity(intent);
            }
        });
        mRecyclerSoftware = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_software_recyclerView);
        mLinearLayoutManagerSoftware = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerSoftware.setLayoutManager(mLinearLayoutManagerSoftware);
        mRecyclerAdapterSoftware = new CoursesRecyclerAdapter(getActivity(),mCoursesList, CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerSoftware.setItemAnimator(new DefaultItemAnimator());
        mRecyclerSoftware.setAdapter(mRecyclerAdapterSoftware);
        mRootView.findViewById(R.id.fragment_courses_relativeLayout_software).setVisibility(View.VISIBLE);
    }

    private void initializeCyberComponents() {
        mBtnShowAllCyber = (Button) mRootView.findViewById(R.id.fragment_courses_cyber_seeAll);
        mBtnShowAllCyber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST,mCoursesList);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_cyber));
                startActivity(intent);
            }
        });
        mRecyclerCyber = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_cyber_recyclerView);
        mLinearLayoutManagerCyber = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerCyber.setLayoutManager(mLinearLayoutManagerCyber);
        mRecyclerAdapterCyber = new CoursesRecyclerAdapter(getActivity(),mCoursesList, CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerCyber.setItemAnimator(new DefaultItemAnimator());
        mRecyclerCyber.setAdapter(mRecyclerAdapterCyber);
        mRootView.findViewById(R.id.fragment_courses_relativeLayout_cyber).setVisibility(View.VISIBLE);
    }

    private void initializeITComponents() {
        mBtnShowAllIT = (Button) mRootView.findViewById(R.id.fragment_courses_it_seeAll);
        mBtnShowAllIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST, mCoursesList);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_it));
                startActivity(intent);
            }
        });
        mRecyclerIT = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_it_recyclerView);
        mLinearLayoutManagerIT = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerIT.setLayoutManager(mLinearLayoutManagerIT);
        mRecyclerAdapterIT = new CoursesRecyclerAdapter(getActivity(),mCoursesList, CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerIT.setItemAnimator(new DefaultItemAnimator());
        mRecyclerIT.setAdapter(mRecyclerAdapterIT);
        mRootView.findViewById(R.id.fragment_courses_relativeLayout_it).setVisibility(View.VISIBLE);
    }

    private void startViewPagerTimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mTimerTaskHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mViewPager.getCurrentItem() < mViewPager.getAdapter().getCount() - 1) {
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                        } else {
                            mViewPager.setCurrentItem(0);
                        }
                    }
                });
            }
        };
        mTimer.schedule(mTimerTask, 8000, 8000);
    }

    private void stopViewPagerTimerOnTouch() {
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer = null;
                        }
                        startViewPagerTimer();
                    }
                }, 2000);
                return false;
            }
        });
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
                mSearchView.clearFocus();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, CoursesSearchableFragment.newInstance(query,mCoursesList)).commit();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
//        MenuItemCompat.setOnActionExpandListener(searchItem,
//                new MenuItemCompat.OnActionExpandListener() {
//                    @Override
//                    public boolean onMenuItemActionCollapse(MenuItem item) {
//                        return true; // Return true to collapse action view
//                    }
//
//                    @Override
//                    public boolean onMenuItemActionExpand(MenuItem item) {
//                        return true; // Return true to expand action view
//                    }
//                });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(EXTRA_COURSES_LIST,mCoursesList);
        outState.putStringArrayList(EXTRA_COURSES_IMAGES_LIST,mCoursesImages);
        outState.putInt(EXTRA_LAST_VIEWPAGER_POSITION, mViewPager.getCurrentItem());
    }
}