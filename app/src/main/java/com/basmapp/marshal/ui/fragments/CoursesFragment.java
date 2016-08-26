package com.basmapp.marshal.ui.fragments;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.ui.adapters.CoursesRecyclerAdapter;
import com.basmapp.marshal.ui.utils.AutoScrollViewPager;
import com.basmapp.marshal.ui.utils.InkPageIndicator;
import com.basmapp.marshal.ui.utils.SuggestionProvider;
import com.basmapp.marshal.ui.adapters.ViewPagerAdapter;
import com.basmapp.marshal.utils.DateHelper;

import java.util.ArrayList;

public class CoursesFragment extends Fragment {
    public static ArrayList<Course> mCoursesList = null;
    private ArrayList<Course> mViewPagerCourses = null;

    public static ArrayList<Course> mSoftwareCourses = null;
    public static ArrayList<Course> mCyberCourses = null;
    public static ArrayList<Course> mITCourses = null;
    public static ArrayList<Course> mToolsCourses = null;
    public static ArrayList<Course> mSystemCourses = null;

    private BroadcastReceiver mAdaptersBroadcastReceiver;

    private InkPageIndicator mInkPageIndicator;
    private AutoScrollViewPager mViewPager;
    private View mViewPagerOverlay;
    private LinearLayout mViewPagerTitlesLayout;
    private TextView mViewPagerTitleTextView;
    private TextView mViewPagerSubtitleTextView;

    private RecyclerView mRecyclerSoftware;
    private LinearLayoutManager mLinearLayoutManagerSoftware;
    private CoursesRecyclerAdapter mRecyclerAdapterSoftware;
    private LinearLayout mBtnShowAllSoftware;

    private RecyclerView mRecyclerCyber;
    private LinearLayoutManager mLinearLayoutManagerCyber;
    private CoursesRecyclerAdapter mRecyclerAdapterCyber;
    private LinearLayout mBtnShowAllCyber;

    private RecyclerView mRecyclerIT;
    private LinearLayoutManager mLinearLayoutManagerIT;
    private CoursesRecyclerAdapter mRecyclerAdapterIT;
    private LinearLayout mBtnShowAllIT;

    private RecyclerView mRecyclerTools;
    private LinearLayoutManager mLinearLayoutManagerTools;
    private CoursesRecyclerAdapter mRecyclerAdapterTools;
    private LinearLayout mBtnShowAllTools;

    private RecyclerView mRecyclerSystem;
    private LinearLayoutManager mLinearLayoutManagerSystem;
    private CoursesRecyclerAdapter mRecyclerAdapterSystem;
    private LinearLayout mBtnShowAllSystem;

    private View mRootView;

    private SearchView mSearchView;

     @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         mRootView = inflater.inflate(R.layout.fragment_courses, container, false);
         mCoursesList = null;
         mCyberCourses = null;
         mSoftwareCourses = null;
         mITCourses = null;
         mToolsCourses = null;
         mSystemCourses = null;

         setHasOptionsMenu(true);

         Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
         toolbar.setTitle(R.string.navigation_drawer_courses);

         mViewPager = (AutoScrollViewPager) mRootView.findViewById(R.id.main_catalog_view_pager);

         if (savedInstanceState != null && mCoursesList == null) {
             mCoursesList = savedInstanceState.getParcelableArrayList(Constants.EXTRA_COURSES_LIST);
         }

         if (mCoursesList == null || mViewPagerCourses == null) {

             mViewPagerCourses = new ArrayList<>();

             mCoursesList = new ArrayList<>();

             new AsyncTask<Void, Void, Boolean>() {

                 ProgressDialog progressDialog;

                 @Override
                 protected void onPreExecute() {
                     super.onPreExecute();
                     progressDialog = new ProgressDialog(getActivity());
                     progressDialog.setMessage(getActivity().getResources().getString(R.string.loading));
                     progressDialog.setCancelable(false);
                     progressDialog.setCanceledOnTouchOutside(false);
                     progressDialog.show();
                 }

                 @Override
                 @SuppressWarnings("unchecked")
                 protected Boolean doInBackground(Void... voids) {
                     try {
                         if (MainActivity.sAllCourses == null) {
                             mCoursesList = (ArrayList) Course.getAllByColumn(DBConstants.COL_IS_MEETUP,
                                     false, DBConstants.COL_ID, getActivity(), Course.class);
                             MainActivity.sAllCourses = mCoursesList;
                         } else {
                             mCoursesList = MainActivity.sAllCourses;
                         }

                         if (MainActivity.sViewPagerCourses == null) {
                             mViewPagerCourses = (ArrayList) Course.rawQuery(getActivity(),
                                     Course.getCloestCoursesSqlQuery(5, true), Course.class);

                             if (mViewPagerCourses == null || mViewPagerCourses.size() == 0)
                                 mViewPagerCourses = (ArrayList) Course.rawQuery(getActivity(),
                                         Course.getCloestCoursesSqlQuery(5, false), Course.class);

                             MainActivity.sViewPagerCourses = mViewPagerCourses;
                         } else {
                             mViewPagerCourses = MainActivity.sViewPagerCourses;
                         }

                         filterData();

                         return mCoursesList.size() > 0;
                     } catch (Exception e) {
                         e.printStackTrace();
                         return false;
                     }
                 }

                 @Override
                 protected void onPostExecute(Boolean result) {
                     super.onPostExecute(result);
                     if (result) {
                         showImagesViewPager();
                         showData();
//                         initializeTutorial();
                     }

                     progressDialog.dismiss();
                 }
             }.execute();

         } else {
             showImagesViewPager();
             showData();
//             initializeTutorial();
        }

         mAdaptersBroadcastReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 if (intent.getAction().equals(Constants.ACTION_COURSE_SUBSCRIPTION_STATE_CHANGED)) {
                     int coursePositionInList = intent.getIntExtra(Constants.EXTRA_COURSE_POSITION_IN_LIST, -1);
                     Course course = intent.getParcelableExtra(Constants.EXTRA_COURSE);
                     if (course != null && course.getCategory() != null &&
                             coursePositionInList != -1) {
                         switch (course.getCategory()) {
                             case Course.CATEGORY_SOFTWARE:
                                 mSoftwareCourses.set(coursePositionInList, course);
                                 break;
                             case Course.CATEGORY_CYBER:
                                 mCyberCourses.set(coursePositionInList, course);
                                 break;
                             case Course.CATEGORY_SYSTEM:
                                 mSystemCourses.set(coursePositionInList, course);
                                 break;
                             case Course.CATEGORY_IT:
                                 mITCourses.set(coursePositionInList, course);
                                 break;
                             case Course.CATEGORY_TOOLS:
                                 mToolsCourses.set(coursePositionInList, course);
                                 break;
                             default:
                                 break;
                         }
                     }
                 }
                 notifyDataSetsChanged();
             }
         };

         IntentFilter intentFilter = new IntentFilter();
         intentFilter.addAction(CoursesRecyclerAdapter.ACTION_ITEM_DATA_CHANGED);
         intentFilter.addAction(Constants.ACTION_COURSE_SUBSCRIPTION_STATE_CHANGED);
         getActivity().registerReceiver(mAdaptersBroadcastReceiver, intentFilter);

         return mRootView;
     }

    private void notifyDataSetsChanged() {
        if (mRecyclerAdapterCyber != null) mRecyclerAdapterCyber.notifyDataSetChanged();
        if (mRecyclerAdapterIT != null) mRecyclerAdapterIT.notifyDataSetChanged();
        if (mRecyclerAdapterSoftware != null) mRecyclerAdapterSoftware.notifyDataSetChanged();
        if (mRecyclerAdapterTools != null) mRecyclerAdapterTools.notifyDataSetChanged();
        if (mRecyclerAdapterSystem != null) mRecyclerAdapterSystem.notifyDataSetChanged();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mViewPager == null) {
            mViewPager = (AutoScrollViewPager) mRootView.findViewById(R.id.main_catalog_view_pager);
        } else if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mViewPager.setDirection(AutoScrollViewPager.LEFT);
        } else {
            mViewPager.setDirection(AutoScrollViewPager.RIGHT);
        }
    }

    private void filterData() {
        if (mCyberCourses == null ||
                mITCourses == null ||
                mSoftwareCourses == null ||
                mToolsCourses == null ||
                mSystemCourses == null) {

            mCyberCourses = new ArrayList<>();
            mITCourses = new ArrayList<>();
            mSoftwareCourses = new ArrayList<>();
            mToolsCourses = new ArrayList<>();
            mSystemCourses = new ArrayList<>();

            for(Course course : mCoursesList) {
                if (course.getCategory() != null) {
                    switch (course.getCategory()) {
                        case Course.CATEGORY_SOFTWARE:
                            mSoftwareCourses.add(course);
                            break;
                        case Course.CATEGORY_CYBER:
                            mCyberCourses.add(course);
                            break;
                        case Course.CATEGORY_IT:
                            mITCourses.add(course);
                            break;
                        case Course.CATEGORY_TOOLS:
                            mToolsCourses.add(course);
                            break;
                        case Course.CATEGORY_SYSTEM:
                            mSystemCourses.add(course);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mAdaptersBroadcastReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.sLastCoursesViewPagerIndex = mViewPager.getCurrentItem();
        // stop auto scroll when onPause
        mViewPager.stopAutoScroll();
    }

    @Override
    public void onResume() {
        super.onResume();
        // start auto scroll when onResume
        mViewPager.startAutoScroll();
    }

    private void showImagesViewPager() {
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(getActivity(), mViewPagerCourses);
        mViewPager.setVisibility(View.VISIBLE);
        mViewPager.setAdapter(mViewPagerAdapter);
        mInkPageIndicator = (InkPageIndicator) mRootView.findViewById(R.id.main_catalog_indicator);
        mInkPageIndicator.setVisibility(View.VISIBLE);
        mViewPagerOverlay = mRootView.findViewById(R.id.gradient_overlay);
        mViewPagerOverlay.setVisibility(View.VISIBLE);
        mViewPagerTitlesLayout = (LinearLayout) mRootView.findViewById(R.id.highlight_overlay_title);
        mViewPagerTitlesLayout.setVisibility(View.VISIBLE);
        mViewPagerTitleTextView = (TextView) mRootView.findViewById(R.id.li_title);
        mViewPagerSubtitleTextView = (TextView) mRootView.findViewById(R.id.li_subtitle);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                if (positionOffset == 0) {
                mViewPagerTitleTextView.setText(mViewPagerCourses.get(position).getName());
                Cycle firstCycle = mViewPagerCourses.get(position).getFirstCycle();
                String cycleDates = String.format(getString(R.string.course_cycle_format),
                        DateHelper.dateToString(firstCycle.getStartDate()),
                        DateHelper.dateToString(firstCycle.getEndDate()));
                mViewPagerSubtitleTextView.setText(cycleDates);
//                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(MainActivity.sLastCoursesViewPagerIndex);
//        mViewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
//            @Override
//            public void transformPage(View view, float position) {
//                if(position <= -1.0F || position >= 1.0F) {
////                    mViewPagerTitlesLayout.setAlpha(0.0F);
//                } else if(position == 0.0F) {
//                    mViewPagerTitlesLayout.setAlpha(1.0F);
//                    mViewPagerTitlesLayout.setTranslationX(0);
//                } else {
//                    mViewPagerTitlesLayout.setTranslationX(view.getWidth() * position);
//                    mViewPagerTitlesLayout.setAlpha(1.0F - Math.abs(position));
//                }
//            }
//        });
        mInkPageIndicator.setViewPager(mViewPager);
        mViewPager.setInterval(5000);
        mViewPager.startAutoScroll();
    }

    private void showData() {
        initializeSoftwareComponents();
        initializeCyberComponents();
        initializeITComponents();
        initializeToolsComponents();
        initializeSystemComponents();
    }

    private void initializeSoftwareComponents() {
        mBtnShowAllSoftware = (LinearLayout) mRootView.findViewById(R.id.fragment_courses_software_see_all);
        mBtnShowAllSoftware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment showAllCoursesFragment = new ShowAllCoursesFragment();
                Bundle args = new Bundle();
                args.putParcelableArrayList(Constants.EXTRA_COURSES_LIST, mSoftwareCourses);
                args.putString(Constants.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_software));
                showAllCoursesFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().
                        add(R.id.content_frame, showAllCoursesFragment).commit();
            }
        });
        mRecyclerSoftware = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_software_recyclerView);
        mLinearLayoutManagerSoftware = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerSoftware.setLayoutManager(mLinearLayoutManagerSoftware);
        mRecyclerAdapterSoftware = new CoursesRecyclerAdapter(getActivity(),mSoftwareCourses,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerSoftware.setItemAnimator(new DefaultItemAnimator());
        mRecyclerSoftware.setAdapter(mRecyclerAdapterSoftware);
        mRootView.findViewById(R.id.fragment_courses_software_see_all).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.fragment_courses_software_recyclerView).setVisibility(View.VISIBLE);
    }

    private void initializeCyberComponents() {
        mBtnShowAllCyber = (LinearLayout) mRootView.findViewById(R.id.fragment_courses_cyber_see_all);
        mBtnShowAllCyber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment showAllCoursesFragment = new ShowAllCoursesFragment();
                Bundle args = new Bundle();
                args.putParcelableArrayList(Constants.EXTRA_COURSES_LIST, mCyberCourses);
                args.putString(Constants.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_cyber));
                showAllCoursesFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().
                        add(R.id.content_frame, showAllCoursesFragment).commit();
            }
        });
        mRecyclerCyber = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_cyber_recyclerView);
        mLinearLayoutManagerCyber = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerCyber.setLayoutManager(mLinearLayoutManagerCyber);
        mRecyclerAdapterCyber = new CoursesRecyclerAdapter(getActivity(),mCyberCourses,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerCyber.setItemAnimator(new DefaultItemAnimator());
        mRecyclerCyber.setAdapter(mRecyclerAdapterCyber);
        mRootView.findViewById(R.id.fragment_courses_cyber_see_all).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.fragment_courses_cyber_recyclerView).setVisibility(View.VISIBLE);
    }

    private void initializeITComponents() {
        mBtnShowAllIT = (LinearLayout) mRootView.findViewById(R.id.fragment_courses_it_see_all);
        mBtnShowAllIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment showAllCoursesFragment = new ShowAllCoursesFragment();
                Bundle args = new Bundle();
                args.putParcelableArrayList(Constants.EXTRA_COURSES_LIST, mITCourses);
                args.putString(Constants.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_it));
                showAllCoursesFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().
                        add(R.id.content_frame, showAllCoursesFragment).commit();
            }
        });
        mRecyclerIT = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_it_recyclerView);
        mLinearLayoutManagerIT = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerIT.setLayoutManager(mLinearLayoutManagerIT);
        mRecyclerAdapterIT = new CoursesRecyclerAdapter(getActivity(),mITCourses,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerIT.setItemAnimator(new DefaultItemAnimator());
        mRecyclerIT.setAdapter(mRecyclerAdapterIT);
        mRootView.findViewById(R.id.fragment_courses_it_see_all).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.fragment_courses_it_recyclerView).setVisibility(View.VISIBLE);
    }

    private void initializeToolsComponents() {
        mBtnShowAllTools = (LinearLayout) mRootView.findViewById(R.id.fragment_courses_tools_see_all);
        mBtnShowAllTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment showAllCoursesFragment = new ShowAllCoursesFragment();
                Bundle args = new Bundle();
                args.putParcelableArrayList(Constants.EXTRA_COURSES_LIST, mToolsCourses);
                args.putString(Constants.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_tools));
                showAllCoursesFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().
                        add(R.id.content_frame, showAllCoursesFragment).commit();
            }
        });
        mRecyclerTools = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_tools_recyclerView);
        mLinearLayoutManagerTools = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerTools.setLayoutManager(mLinearLayoutManagerTools);
        mRecyclerAdapterTools = new CoursesRecyclerAdapter(getActivity(),mToolsCourses,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerTools.setItemAnimator(new DefaultItemAnimator());
        mRecyclerTools.setAdapter(mRecyclerAdapterTools);
        mRootView.findViewById(R.id.fragment_courses_tools_see_all).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.fragment_courses_tools_recyclerView).setVisibility(View.VISIBLE);
    }

    private void initializeSystemComponents() {
        mBtnShowAllSystem = (LinearLayout) mRootView.findViewById(R.id.fragment_courses_system_see_all);
        mBtnShowAllSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment showAllCoursesFragment = new ShowAllCoursesFragment();
                Bundle args = new Bundle();
                args.putParcelableArrayList(Constants.EXTRA_COURSES_LIST, mSystemCourses);
                args.putString(Constants.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_system));
                showAllCoursesFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().
                        add(R.id.content_frame, showAllCoursesFragment).commit();
            }
        });
        mRecyclerSystem = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_system_recyclerView);
        mLinearLayoutManagerSystem = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerSystem.setLayoutManager(mLinearLayoutManagerSystem);
        mRecyclerAdapterSystem = new CoursesRecyclerAdapter(getActivity(),mSystemCourses,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerSystem.setItemAnimator(new DefaultItemAnimator());
        mRecyclerSystem.setAdapter(mRecyclerAdapterSystem);
        mRootView.findViewById(R.id.fragment_courses_system_see_all).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.fragment_courses_system_recyclerView).setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Setup search button
        final MenuItem searchItem = menu.findItem(R.id.menu_main_searchView);
        mSearchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
        {
            @Override
            public boolean onSuggestionClick(int position) {
                mSearchView.clearFocus();
                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
                String query = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame,
                        CoursesSearchableFragment.newInstance(query, mCoursesList)).commit();
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                // remove trailing and leading space
                query = query.trim();
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                suggestions.saveRecentQuery(query, null);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                if (query.equals("*")) query = "";
                fragmentManager.beginTransaction().replace(R.id.content_frame,
                        CoursesSearchableFragment.newInstance(query, mCoursesList)).commit();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
//        // Collapse search view and close keyboard together
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                searchItem.collapseActionView();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.EXTRA_COURSES_LIST,mCoursesList);
        outState.putInt(Constants.EXTRA_LAST_VIEWPAGER_POSITION, mViewPager.getCurrentItem());
    }
}