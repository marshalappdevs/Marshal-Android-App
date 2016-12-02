package com.basmapp.marshal.ui.fragments;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.interfaces.ContentProviderCallBack;
import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.ui.CourseActivity;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.ui.ShowAllCoursesActivity;
import com.basmapp.marshal.ui.adapters.CoursesRecyclerAdapter;
import com.basmapp.marshal.ui.widget.AutoScrollViewPager;
import com.basmapp.marshal.ui.widget.InkPageIndicator;
import com.basmapp.marshal.util.ContentProvider;
import com.basmapp.marshal.util.LocaleUtils;
import com.basmapp.marshal.util.DateHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class CoursesFragment extends Fragment {

    private ArrayList<Course> mViewPagerCourses;

    private HashMap<String, CategoryHolder> mCategoryHoldersMap;
    private ArrayList<Integer> mCategoryHoldersIndexes;

    private BroadcastReceiver mAdaptersBroadcastReceiver;

    private InkPageIndicator mInkPageIndicator;
    private AutoScrollViewPager mViewPager;

    private LinearLayout mMainContainer;

    private View mRootView;

    private SearchView mSearchView;
    private MenuItem mSearchItem;

    private static final String COURSES_SCROLL_X = "COURSES_SCROLL_X";
    private static final String COURSES_SCROLL_Y = "COURSES_SCROLL_Y";
    private ScrollView mScrollView;
    private static final String COURSES_PREVIOUS_QUERY = "COURSES_PREVIOUS_QUERY";
    private String mPreviousQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_courses, container, false);
        mMainContainer = (LinearLayout) mRootView.findViewById(R.id.fragment_courses_main_container);

        mCategoryHoldersMap = new HashMap<>();

        setHasOptionsMenu(true);

        mScrollView = (ScrollView) mRootView.findViewById(R.id.fragment_courses_scrollView);

        mViewPager = (AutoScrollViewPager) mRootView.findViewById(R.id.main_catalog_view_pager);

        ContentProvider.getInstance().getViewPagerCourses(getContext(), new ContentProviderCallBack() {
            @Override
            public void onDataReady(ArrayList<? extends DBObject> data, Object extra) {
                mViewPagerCourses = (ArrayList<Course>) data;
                showImagesViewPager();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

        mCategoryHoldersIndexes = new ArrayList<>();
        Set<String> categories = ContentProvider.getInstance().getCoursesCategories(getContext());

        if (categories != null) {
            for (String category : categories) {
                String categoryValue = category.split(";")[0];
                mCategoryHoldersMap.put(categoryValue,
                        new CategoryHolder(categoryValue));
            }
        }

        mAdaptersBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ContentProvider.Actions.COURSE_RATING_UPDATED)) {
                    Course course = intent.getParcelableExtra(ContentProvider.Extras.COURSE);
                    mCategoryHoldersMap.get(course.getCategory()).notifyItemChanged(course);
                } else if (intent.getAction().equals(ContentProvider.Actions.COURSE_SUBSCRIPTION_UPDATED)) {
                    Course course = intent.getParcelableExtra(ContentProvider.Extras.COURSE);
                    mCategoryHoldersMap.get(course.getCategory()).changeCourseSubscriptionState(course);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ContentProvider.Actions.COURSE_RATING_UPDATED);
        getActivity().registerReceiver(mAdaptersBroadcastReceiver, intentFilter);

        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mAdaptersBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save ScrollView position
        outState.putInt(COURSES_SCROLL_X, mScrollView.getScrollX());
        outState.putInt(COURSES_SCROLL_Y, mScrollView.getScrollY());

        // Save SearchView query if possible
        if (mSearchView != null) {
            outState.putString(COURSES_PREVIOUS_QUERY, mSearchView.getQuery().toString());
        }
        outState.putInt(Constants.EXTRA_LAST_VIEWPAGER_POSITION, mViewPager.getCurrentItem());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore ScrollView position
            final int x = savedInstanceState.getInt(COURSES_SCROLL_X);
            final int y = savedInstanceState.getInt(COURSES_SCROLL_Y);
            mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mScrollView.scrollTo(x, y);
                }
            });
            // Restore previous SearchView query
            mPreviousQuery = savedInstanceState.getString(COURSES_PREVIOUS_QUERY);
        }
        if (mViewPager == null) {
            mViewPager = (AutoScrollViewPager)
                    mRootView.findViewById(R.id.main_catalog_view_pager);
        } else if (LocaleUtils.isRtl(getResources())) {
            mViewPager.setDirection(AutoScrollViewPager.LEFT);
        } else {
            mViewPager.setDirection(AutoScrollViewPager.RIGHT);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.sLastCoursesViewPagerIndex = mViewPager.getCurrentItem();
        // stop auto scroll when onPause
        mViewPager.stopAutoScroll();
        // Collapse search view after exiting fragment if there is no query
        if (mSearchView != null && mSearchView.getQuery().toString().isEmpty()) {
            if (mSearchItem != null)
                mSearchItem.collapseActionView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // start auto scroll when onResume
        mViewPager.startAutoScroll();
    }

    private void showImagesViewPager() {
        // Create the adapter that will return a fragment for each position
        HighlightsAdapter highlightsAdapter = new HighlightsAdapter(getChildFragmentManager());
        mViewPager = (AutoScrollViewPager) mRootView.findViewById(R.id.main_catalog_view_pager);
        mViewPager.setAdapter(highlightsAdapter);
        mViewPager.setPageTransformer(true, new Transformer());
        mViewPager.setCurrentItem(MainActivity.sLastCoursesViewPagerIndex);
        mInkPageIndicator = (InkPageIndicator) mRootView.findViewById(R.id.page_indicator);
        mInkPageIndicator.setViewPager(mViewPager);
        mViewPager.setInterval(5000);
        mViewPager.startAutoScroll();
        if (mViewPagerCourses != null && mViewPagerCourses.size() != 0) {
            mViewPager.setVisibility(View.VISIBLE);
            mInkPageIndicator.setVisibility(View.VISIBLE);
        }
    }

    private class Transformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View page, float position) {
            // Title and subtitle both translates in/out and fades in/out
            page.findViewById(R.id.highlight_overlay_title)
                    .setAlpha(1.0F - Math.abs(position) * 1.5F);
        }
    }

    public static class ArrayListFragment extends Fragment {
        final static String ARG_POSITION = "position";
        private int mPosition;
        final static String ARG_COURSES = "viewPagerCourses";
        private ArrayList<Course> mViewPagerCourses;

        static ArrayListFragment newInstance(int position, ArrayList<Course> viewPagerCourses) {
            ArrayListFragment fragment = new ArrayListFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_POSITION, position);
            args.putParcelableArrayList(ARG_COURSES, viewPagerCourses);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mPosition = getArguments().getInt(ARG_POSITION);
                mViewPagerCourses = getArguments().getParcelableArrayList(ARG_COURSES);
            } else {
                mPosition = 1;
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.highlights_banner_fullbleed_item, container, false);

            if (mViewPagerCourses != null && mViewPagerCourses.size() != 0) {
                TextView title = (TextView) rootView.findViewById(R.id.li_title);
                title.setText(mViewPagerCourses.get(mPosition).getName());

                TextView subtitle = (TextView) rootView.findViewById(R.id.li_subtitle);
                Cycle firstCycle = mViewPagerCourses.get(mPosition).getFirstCycle();
                if (firstCycle != null && firstCycle.getStartDate() != null &&
                        firstCycle.getEndDate() != null) {
                    String cycleDates = String.format(getString(R.string.course_cycle_format),
                            DateHelper.dateToString(firstCycle.getStartDate()),
                            DateHelper.dateToString(firstCycle.getEndDate()));
                    subtitle.setText(cycleDates);
                } else subtitle.setVisibility(View.GONE);

                ImageView image = (ImageView) rootView.findViewById(R.id.li_thumbnail);
                Glide.with(getActivity())
                        .load(mViewPagerCourses.get(mPosition).getImageUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(image);

                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), CourseActivity.class);
                        intent.putExtra(Constants.EXTRA_COURSE, mViewPagerCourses.get(mPosition));
                        getActivity().startActivityForResult(intent, MainActivity.RC_COURSE_ACTIVITY);
                    }
                });
            }

            return rootView;
        }
    }

    public class HighlightsAdapter extends FragmentPagerAdapter {

        HighlightsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ArrayListFragment.newInstance(position, mViewPagerCourses);
        }

        @Override
        public int getCount() {
            return mViewPagerCourses.size();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        SearchManager searchManager = (SearchManager)
                getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchItem = menu.findItem(R.id.m_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(
                getActivity().getComponentName()));
        mSearchView.setQueryRefinementEnabled(true);

        // Very dirty hack to allow empty query submit
        EditText searchAutoComplete = (EditText) mSearchView.findViewById(R.id.search_src_text);
        if (searchAutoComplete != null) {
            searchAutoComplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (mSearchView.getQuery().toString().isEmpty()) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            mSearchView.setQuery("*", true);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        // Collapse search view after text submit
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Collapse search view after clicking suggestion
        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {
                mSearchItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }
        });

        // Collapse search view and close keyboard together
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                mSearchItem.collapseActionView();
            }
        });

        if (mPreviousQuery != null && !mPreviousQuery.isEmpty()) {
            mSearchItem.expandActionView();
            mSearchView.setQuery(mPreviousQuery, false);
        }
    }

    private class CategoryHolder {

        private LinearLayout mContainer;
        private RecyclerView mRecyclerView;
        private LinearLayoutManager mLinearLayoutManager;
        private CoursesRecyclerAdapter mRecyclerAdapter;
        private LinearLayout mBtnShowAll;
        private TextView mCategoryTextView;

        private ArrayList<Course> mCourses;

        private String mCategory;

        CategoryHolder(String category) {
            this.mCategory = category;
            initAndShow();
        }

        private void initAndShow() {
            ContentProvider.getInstance().getCoursesListByCategory(getContext(), mCategory,
                    new ContentProviderCallBack() {
                        @Override
                        public void onDataReady(ArrayList<? extends DBObject> data, Object extra) {
                            mCourses = (ArrayList<Course>) data;
                            addToMainContainer();
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        private void initUI() {
            mContainer = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.course_category_template, null);
            mCategoryTextView = (TextView) mContainer.findViewById(R.id.course_category_categoryTextView);
            mCategoryTextView.setText(LocaleUtils.getCategoryLocaleTitle(mCategory, getContext()));
            mBtnShowAll = (LinearLayout) mContainer.findViewById(R.id.course_category_see_all);
            mBtnShowAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                    intent.putParcelableArrayListExtra(Constants.EXTRA_COURSES_LIST, mCourses);
                    intent.putExtra(Constants.EXTRA_COURSE_CATEGORY, LocaleUtils.getCategoryLocaleTitle(mCategory, getContext()));
                    startActivity(intent);
                }
            });
            mRecyclerView = (RecyclerView) mContainer.findViewById(R.id.course_category_recyclerView);
            mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            mRecyclerView.setLayoutManager(mLinearLayoutManager);
            mRecyclerAdapter = new CoursesRecyclerAdapter(getActivity(), mCourses,
                    CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mRecyclerAdapter);
            if (mCourses != null) {
                if (mCourses.size() > 0) {
                    mContainer.findViewById(R.id.course_category_see_all).setVisibility(View.VISIBLE);
                    mContainer.findViewById(R.id.course_category_recyclerView).setVisibility(View.VISIBLE);
                } else {
                    mContainer.findViewById(R.id.course_category_see_all).setVisibility(View.GONE);
                    mContainer.findViewById(R.id.course_category_recyclerView).setVisibility(View.GONE);
                }
            }
        }

        private void addToMainContainer() {
            initUI();
            int categoryIndex = getCategoryIndex();
            int insertIndex = getInsertIndex(categoryIndex);
            mMainContainer.addView(mContainer, insertIndex);
            mCategoryHoldersIndexes.add(categoryIndex);
        }

        private int getInsertIndex(int categoryNumberToInsert) {
            int index = 1;
            for (Integer currCategoryOrder : mCategoryHoldersIndexes) {
                if (categoryNumberToInsert > currCategoryOrder)
                    index++;
            }
            return index;
        }

        private int getCategoryIndex() {
            int index = 0;
            Set<String> categoriesSet = ContentProvider.getInstance().getCoursesCategories(getContext());
            for (String category : categoriesSet) {
                if (mCategory.equals(category.split(";")[0]))
                    return Integer.parseInt(category.split(";")[3]) - 1;
                index++;
            }
            return index;
        }

        void notifyItemChanged(int position) {
            mRecyclerAdapter.notifyItemChanged(position);
        }

        void notifyItemChanged(Course course) {
            int position = ContentProvider.Utils.getCoursePositionInList(mCourses, course);
            notifyItemChanged(position);
        }

        void changeCourseSubscriptionState(Course course) {
            int position = ContentProvider.Utils.getCoursePositionInList(mCourses, course);
            mCourses.get(position).setIsUserSubscribe(course.getIsUserSubscribe());
            notifyItemChanged(position);
        }
    }
}