package com.basmach.marshal.ui.fragments;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.Button;
import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Rating;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.ui.MainActivity;
import com.basmach.marshal.ui.ShowAllCoursesActivity;
import com.basmach.marshal.ui.adapters.CoursesRecyclerAdapter;
import com.basmach.marshal.ui.utils.InkPageIndicator;
import com.basmach.marshal.ui.utils.SuggestionProvider;
import com.basmach.marshal.ui.adapters.ViewPagerAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CoursesFragment extends Fragment {
    private static final String EXTRA_COURSES_LIST = "extra_courses_list";
    private static final String EXTRA_LAST_VIEWPAGER_POSITION = "extra_last_viewpager_position";

    public static ArrayList<Rating> mRatingsList = null;
    public static ArrayList<Course> mCoursesList = null;
    public static ArrayList<Course> mSoftwareCourses = null;
    public static ArrayList<Course> mCyberCourses = null;
    public static ArrayList<Course> mITCourses = null;

    private ArrayList<Course> mViewPagerCourses = null;

    private InkPageIndicator mInkPageIndicator;
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

    private SearchView mSearchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_courses, container, false);

        mCoursesList = null;
        mCyberCourses = null;
        mSoftwareCourses = null;
        mITCourses = null;

        setHasOptionsMenu(true);

        mViewPager = (ViewPager) mRootView.findViewById(R.id.main_catalog_view_pager);

        if (savedInstanceState != null && mCoursesList == null) {
            mCoursesList = savedInstanceState.getParcelableArrayList(EXTRA_COURSES_LIST);
        }

        if (mCoursesList == null || mViewPagerCourses == null) {
            mViewPagerCourses = new ArrayList<>();
            mCoursesList = new ArrayList<>();
            mRatingsList = new ArrayList<>();
//            ((MainActivity)getActivity()).getCoursesDataAsync(true, new BackgroundTaskCallBack() {
//                @Override
//                public void onSuccess(String result, List<Object> data) {
//                    if (data != null && data.size() > 0) {
//                        for(Object item : data) {
//                            Log.i("GET COURSES "," ITEM: " + ((Course)item).getName());
//
//                            if (mCoursesList != null) {
//                                mCoursesList.add((Course) item);
//                            }
//
//                            if (((Course) item).getImageUrl() != null) {
//                                if (mViewPagerCourses.size() < 5) {
//                                    mViewPagerCourses.add(((Course) item));
//                                }
//                            }
//                        }
//
//                        filterData();
//                        showImagesViewPager();
//                        showData();
//                    }
//                }
//
//                @Override
//                public void onError(String error) {
//
//                }
//            });

            new AsyncTask<Void, Void, Boolean>() {

                ProgressDialog progressDialog;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getActivity().getResources().getString(R.string.loading));
                    progressDialog.show();
                }

                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        if (MainActivity.allRatings == null) {
                            mRatingsList = (ArrayList) Rating.getAll(DBConstants.COL_COURSE_CODE, getActivity(), Rating.class);
                            MainActivity.allRatings = mRatingsList;
                        } else {
                            mRatingsList = MainActivity.allRatings;
                        }

                        if (MainActivity.allCourses == null) {
                            mCoursesList = (ArrayList) Course.getAll(DBConstants.COL_ID, getActivity(), Course.class);
                            MainActivity.allCourses = mCoursesList;
                        } else {
                            mCoursesList = MainActivity.allCourses;
                        }

                        if (mCoursesList.size() > 0) {
                            for(Course course : mCoursesList) {
                                mViewPagerCourses.add(course);

                                if(mViewPagerCourses.size() == 5)
                                    break;
                            }
                            return true;
                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                    if (result) {
                        filterData();
                        showImagesViewPager();
                        showData();
                    }

                    progressDialog.dismiss();
                }
            }.execute();

        } else {
            showImagesViewPager();
            showData();
        }

        return mRootView;
    }

    private void filterData() {
        if (mCyberCourses == null ||
                mITCourses == null ||
                mSoftwareCourses == null) {

            mCyberCourses = new ArrayList<>();
            mITCourses = new ArrayList<>();
            mSoftwareCourses = new ArrayList<>();

            for(Course course : mCoursesList) {
                switch (course.getCourseCode()) {
                    case "34":
                        mSoftwareCourses.add(course);
                        break;
                    case "35683":
                        mITCourses.add(course);
                        break;
                    case "285":
                        mITCourses.add(course);
                        break;
                    case "2041":
                        mITCourses.add(course);
                        break;
                    case "36985":
                        mITCourses.add(course);
                        break;
                    case "8922":
                        mITCourses.add(course);
                        break;
                    case "4669":
                        mSoftwareCourses.add(course);
                        break;
                    case "2633":
                        mSoftwareCourses.add(course);
                        break;
                    case "9777":
                        mSoftwareCourses.add(course);
                        break;
                    case "6813":
                        mSoftwareCourses.add(course);
                        break;
                    case "579":
                        mSoftwareCourses.add(course);
                        break;
                    case "440":
                        mCyberCourses.add(course);
                        break;
                    case "2753":
                        mCyberCourses.add(course);
                        break;
                    case "2803":
                        mCyberCourses.add(course);
                        break;
                    case "373":
                        mCyberCourses.add(course);
                        break;
                    case "2888":
                        mCyberCourses.add(course);
                        break;
                    default:
                        break;
                }
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    private void showImagesViewPager() {
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(getActivity(), mViewPagerCourses);
        mViewPager.setAdapter(mViewPagerAdapter);
        mInkPageIndicator = (InkPageIndicator) mRootView.findViewById(R.id.main_catalog_indicator);
        mInkPageIndicator.setVisibility(View.VISIBLE);
        mViewPager.setCurrentItem(MainActivity.lastCoursesViewPagerIndex);
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
        mBtnShowAllSoftware = (Button) mRootView.findViewById(R.id.fragment_courses_software_see_all);
        mBtnShowAllSoftware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST,mSoftwareCourses);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_software));
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_RATINGS_LIST, mRatingsList);
                startActivity(intent);
            }
        });
        mRecyclerSoftware = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_software_recyclerView);
        mLinearLayoutManagerSoftware = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerSoftware.setLayoutManager(mLinearLayoutManagerSoftware);
        mRecyclerAdapterSoftware = new CoursesRecyclerAdapter(getActivity(),mSoftwareCourses, mRatingsList,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerSoftware.setItemAnimator(new DefaultItemAnimator());
        mRecyclerSoftware.setAdapter(mRecyclerAdapterSoftware);
        mRootView.findViewById(R.id.fragment_courses_software_relativeLayout).setVisibility(View.VISIBLE);
    }

    private void initializeCyberComponents() {
        mBtnShowAllCyber = (Button) mRootView.findViewById(R.id.fragment_courses_cyber_see_all);
        mBtnShowAllCyber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST,mCyberCourses);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_cyber));
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_RATINGS_LIST, mRatingsList);
                startActivity(intent);
            }
        });
        mRecyclerCyber = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_cyber_recyclerView);
        mLinearLayoutManagerCyber = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerCyber.setLayoutManager(mLinearLayoutManagerCyber);
        mRecyclerAdapterCyber = new CoursesRecyclerAdapter(getActivity(),mCyberCourses, mRatingsList,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerCyber.setItemAnimator(new DefaultItemAnimator());
        mRecyclerCyber.setAdapter(mRecyclerAdapterCyber);
        mRootView.findViewById(R.id.fragment_courses_cyber_relativeLayout).setVisibility(View.VISIBLE);
    }

    private void initializeITComponents() {
        mBtnShowAllIT = (Button) mRootView.findViewById(R.id.fragment_courses_it_see_all);
        mBtnShowAllIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST, mITCourses);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_it));
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_RATINGS_LIST, mRatingsList);
                startActivity(intent);
            }
        });
        mRecyclerIT = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_it_recyclerView);
        mLinearLayoutManagerIT = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerIT.setLayoutManager(mLinearLayoutManagerIT);
        mRecyclerAdapterIT = new CoursesRecyclerAdapter(getActivity(),mITCourses, mRatingsList,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerIT.setItemAnimator(new DefaultItemAnimator());
        mRecyclerIT.setAdapter(mRecyclerAdapterIT);
        mRootView.findViewById(R.id.fragment_courses_it_relativeLayout).setVisibility(View.VISIBLE);
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
                        CoursesSearchableFragment.newInstance(query, mCoursesList, mRatingsList)).commit();
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
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                suggestions.saveRecentQuery(query, null);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame,
                        CoursesSearchableFragment.newInstance(query, mCoursesList, mRatingsList)).commit();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        super.onDestroyView();
        MainActivity.lastCoursesViewPagerIndex = mViewPager.getCurrentItem();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_COURSES_LIST,mCoursesList);
        outState.putInt(EXTRA_LAST_VIEWPAGER_POSITION, mViewPager.getCurrentItem());
    }
}