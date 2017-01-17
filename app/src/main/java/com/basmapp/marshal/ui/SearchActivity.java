package com.basmapp.marshal.ui;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.interfaces.ContentProviderCallBack;
import com.simplite.orm.DBObject;
import com.basmapp.marshal.ui.adapters.CoursesSearchRecyclerAdapter;
import com.basmapp.marshal.util.ContentProvider;
import com.basmapp.marshal.util.SuggestionProvider;
import com.basmapp.marshal.util.ThemeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static android.app.SearchManager.QUERY;
import static android.content.Intent.ACTION_SEARCH;


public class SearchActivity extends BaseActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private Toolbar mToolbar;
    private SearchView mSearchView;
    private RecyclerView mRecycler;
    private CoursesSearchRecyclerAdapter mAdapter;

    private ArrayList<Course> mCoursesList;
    private ArrayList<Course> mFilteredCourseList;

    private String mSearchQuery;
    private LinearLayout mFilterNoticeGroup, mNoResultsContainer;
    private TextView mNoResults, mCourseFilterNotice;
    private Button mResetFilter;
    private boolean isEmptyResult = false;
    private SimpleDateFormat mFilterDateFormat;
    private MaterialTapTargetPrompt mFilterPrompt;
    private BroadcastReceiver mAdaptersBroadcastReceiver;

    private static final String SEARCH_PREVIOUS_QUERY = "SEARCH_PREVIOUS_QUERY";
    private static final String FILTER_PREVIOUS_START_DATE_FINAL = "FILTER_PREVIOUS_START_DATE_FINAL";
    private static final String FILTER_PREVIOUS_END_DATE_FINAL = "FILTER_PREVIOUS_END_DATE_FINAL";
    private static final String FILTER_PREVIOUS_TYPE_INDEX_FINAL = "FILTER_PREVIOUS_TYPE_INDEX_FINAL";
    private static final String FILTER_PREVIOUS_CATEGORY_INDEX_FINAL = "FILTER_PREVIOUS_CATEGORY_INDEX_FINAL";
    private long mFinalStartDate, mFinalEndDate;
    private int mFinalTypeIndex, mFinalCategoryIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.search_title);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // For white SearchView
        mToolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.quantum_grey_600));
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.toolbar_shadow).setVisibility(View.VISIBLE);
        }

        mRecycler = (RecyclerView) findViewById(R.id.search_activity_recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.card_bucket_columns), GridLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(gridLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());

        mFilterDateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

        mNoResults = (TextView) findViewById(R.id.search_activity_no_results);
        mNoResultsContainer = (LinearLayout) findViewById(R.id.no_results_container);
        mFilterNoticeGroup = (LinearLayout) findViewById(R.id.filter_notice_bar_group);
        mCourseFilterNotice = (TextView) findViewById(R.id.course_filter_notice);
        mResetFilter = (Button) findViewById(R.id.reset_course_filter_button);

        if (mCoursesList != null)
            mFilteredCourseList = new ArrayList<>(mCoursesList);
        else
            mFilteredCourseList = new ArrayList<>();

        if (mAdapter == null)
            mAdapter = new CoursesSearchRecyclerAdapter(this, mFilteredCourseList);

        if (mRecycler.getAdapter() == null)
            mRecycler.setAdapter(mAdapter);

        // Hide keyboard while scrolling
        mRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide only if there are results
                if (mFilteredCourseList != null && !mFilteredCourseList.isEmpty() && mSearchView != null) {
                    mSearchView.clearFocus();
                }
                return false;
            }
        });

        mAdaptersBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ContentProvider.Actions.COURSE_RATING_UPDATED)) {
                    Course course = intent.getParcelableExtra(ContentProvider.Extras.COURSE);
                    int itemPosition = ContentProvider.Utils.getCoursePositionInList(mCoursesList, course);

                    if (itemPosition > -1)
                        mAdapter.notifyItemChanged(itemPosition);
                } else if (intent.getAction().equals(ContentProvider.Actions.COURSE_SUBSCRIPTION_UPDATED)) {
                    Course course = intent.getParcelableExtra(ContentProvider.Extras.COURSE);
                    mCoursesList.get(ContentProvider.Utils.getCoursePositionInList(mCoursesList,
                            course)).setIsUserSubscribe(course.getIsUserSubscribe());
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ContentProvider.Actions.COURSE_SUBSCRIPTION_UPDATED);
        intentFilter.addAction(ContentProvider.Actions.COURSE_RATING_UPDATED);
        registerReceiver(mAdaptersBroadcastReceiver, intentFilter);

        handleIntent(getIntent());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save SearchView query if possible
        if (mSearchView != null) {
            outState.putString(SEARCH_PREVIOUS_QUERY, mSearchView.getQuery().toString());
        }
        // Save final filtered dates if available
        if (mFinalStartDate != 0)
            outState.putLong(FILTER_PREVIOUS_START_DATE_FINAL, mFinalStartDate);
        if (mFinalEndDate != 0)
            outState.putLong(FILTER_PREVIOUS_END_DATE_FINAL, mFinalEndDate);
        // Save final spinner selection
        if (mFinalTypeIndex != 0)
            outState.putInt(FILTER_PREVIOUS_TYPE_INDEX_FINAL, mFinalTypeIndex);
        if (mFinalCategoryIndex != 0)
            outState.putInt(FILTER_PREVIOUS_CATEGORY_INDEX_FINAL, mFinalCategoryIndex);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore previous SearchView query
        mSearchQuery = savedInstanceState.getString(SEARCH_PREVIOUS_QUERY);
        // Restore previous final filter dates
        mFinalStartDate = savedInstanceState.getLong(FILTER_PREVIOUS_START_DATE_FINAL);
        mFinalEndDate = savedInstanceState.getLong(FILTER_PREVIOUS_END_DATE_FINAL);
        mFinalTypeIndex = savedInstanceState.getInt(FILTER_PREVIOUS_TYPE_INDEX_FINAL);
        mFinalCategoryIndex = savedInstanceState.getInt(FILTER_PREVIOUS_CATEGORY_INDEX_FINAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAdaptersBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search, menu);

        MenuItem searchItem = menu.findItem(R.id.m_search);
        MenuItemCompat.expandActionView(searchItem);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // For white SearchView
        ((AutoCompleteTextView) mSearchView.findViewById(R.id.search_src_text)).setTextColor(
                ContextCompat.getColor(this, R.color.material_light_primary_text));
        ((AutoCompleteTextView) mSearchView.findViewById(R.id.search_src_text)).setHintTextColor(
                ContextCompat.getColor(this, R.color.material_light_hint_text));
        // Set suggestions to full screen width
        final AutoCompleteTextView searchEditText = (AutoCompleteTextView)
                mSearchView.findViewById(R.id.search_src_text);
        View dropDownAnchor = mSearchView.findViewById(searchEditText.getDropDownAnchor());
        if (dropDownAnchor != null) {
            dropDownAnchor.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    // Set DropDownView width
                    Point size = new Point();
                    getWindowManager().getDefaultDisplay().getSize(size);
                    searchEditText.setDropDownWidth(size.x);
                }
            });
        }
        // Set suggestions under toolbar
        ((AutoCompleteTextView) mSearchView.findViewById(R.id.search_src_text)).setDropDownAnchor(R.id.anchor_dropdown);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setQueryRefinementEnabled(true);


        // Show target prompt for filter
        showFilterTargetPrompt();

        // Close activity when collapsing SearchView
        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        if (mFilterPrompt != null) {
                            mFilterPrompt.finish();
                            mFilterPrompt = null;
                        } else {
                            finish();
                        }
                        return false; // Return false to prevent collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true; // Return true to expand action view
                    }
                });

        // Make sure that query will be set to SearchView
        mSearchView.post(new Runnable() {
            @Override
            public void run() {
                mSearchView.setQuery(mSearchQuery, false);
            }
        });
        mSearchView.clearFocus();

        // Show filtered search if dates are available (from saved instance for example)
        if (mFinalStartDate == 0 && mFinalEndDate == 0 && mFinalTypeIndex == 0 && mFinalCategoryIndex == 0) {
            return true;
        } else {
            advancedFilter(mFinalStartDate, mFinalEndDate, mFinalTypeIndex, mFinalCategoryIndex);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_filter:
                if (!isEmptyResult) {
                    CourseFilterDialog courseFilterDialog = CourseFilterDialog.newInstance(mFinalStartDate, mFinalEndDate, mFinalTypeIndex, mFinalCategoryIndex);
                    courseFilterDialog.show(getSupportFragmentManager(), Constants.DIALOG_FRAGMENT_FILTER_BY_DATE);
                } else {
                    Toast.makeText(this, R.string.filter_not_available,
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (ACTION_SEARCH.equals(intent.getAction())) {
            mSearchQuery = intent.getStringExtra(QUERY);

            // Use asterisk to show all search results
            if (mSearchQuery.equals("*"))
                mSearchQuery = "";

            if (mToolbar != null) {
                mToolbar.setTitle(mSearchQuery);
            }
            if (mSearchView != null) {
                mSearchView.setQuery(mSearchQuery, false);
                mSearchView.clearFocus();
            }

            if (mCoursesList == null) {
                fetchData(mSearchQuery);
            } else {
                filter(mSearchQuery);
            }

            if (!mSearchQuery.isEmpty()) {
                SuggestionProvider.save(this, mSearchQuery.trim());
            }
        }
    }

    private void fetchData(final String query) {
        ContentProvider.getInstance().getCourses(getApplicationContext(), new ContentProviderCallBack() {
            @Override
            public void onDataReady(ArrayList<? extends DBObject> data, Object extra) {
                mCoursesList = (ArrayList<Course>) data;
                mFilteredCourseList = (ArrayList<Course>) data;
                filter(query);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void showFilterTargetPrompt() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Constants.SHOW_FILTER_TAP_TARGET, true) && !isEmptyResult) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            mFilterPrompt = new MaterialTapTargetPrompt.Builder(this)
                    .setTarget(toolbar.getChildAt(0))
                    .setPrimaryText(R.string.filter_tip_title)
                    .setSecondaryText(R.string.filter_tip_subtitle)
                    .setBackgroundColour(ThemeUtils.getThemeColor(this, R.attr.colorPrimary))
                    .setIcon(R.drawable.ic_filter_vert)
                    .setIconDrawableColourFilter(ThemeUtils.getThemeColor(this, R.attr.colorPrimary))
                    .setAnimationInterpolator(new FastOutSlowInInterpolator())
                    .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                    .setAutoDismiss(false)
                    .setAutoFinish(false)
                    .setCaptureTouchEventOutsidePrompt(true)
                    .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                        @Override
                        public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                            if (tappedTarget) {
                                mFilterPrompt.finish();
                                mFilterPrompt = null;
                                PreferenceManager.getDefaultSharedPreferences(SearchActivity.this).edit()
                                        .putBoolean(Constants.SHOW_FILTER_TAP_TARGET, false).apply();
                            }
                        }

                        @Override
                        public void onHidePromptComplete() {
                        }
                    })
                    .show();
        }
    }

    public static class CourseFilterDialog extends DialogFragment {
        private Calendar mCalendar;
        private TextView mStartDatePicker, mEndDatePicker;
        private Spinner mCourseTypeSpinner, mCourseCategorySpinner;
        private SimpleDateFormat mFilterDateFormat;
        private long mStartDate, mEndDate = 0;
        private int mTypeIndex, mCategoryIndex;
        private static final String FILTER_PREVIOUS_START_DATE = "FILTER_PREVIOUS_START_DATE";
        private static final String FILTER_PREVIOUS_END_DATE = "FILTER_PREVIOUS_END_DATE";
        private static final String FILTER_PREVIOUS_TYPE_INDEX = "FILTER_PREVIOUS_TYPE_INDEX";
        private static final String FILTER_PREVIOUS_CATEGORY_INDEX = "FILTER_PREVIOUS_CATEGORY_INDEX";

        static CourseFilterDialog newInstance(long startDate, long endDate, int spinnerTypeIndex, int spinnerCategoryIndex) {
            CourseFilterDialog courseFilterDialog = new CourseFilterDialog();
            // Get filter dates as an argument
            Bundle args = new Bundle();
            args.putLong("start_date", startDate);
            args.putLong("end_date", endDate);
            args.putInt("spinner_type_index", spinnerTypeIndex);
            args.putInt("spinner_category_index", spinnerCategoryIndex);
            courseFilterDialog.setArguments(args);
            return courseFilterDialog;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(STYLE_NO_TITLE, R.style.CourseFilterDialogTheme);
            mStartDate = getArguments().getLong("start_date");
            mEndDate = getArguments().getLong("end_date");
            mTypeIndex = getArguments().getInt("spinner_type_index");
            mCategoryIndex = getArguments().getInt("spinner_category_index");
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            // Save filtered dates if available
            outState.putLong(FILTER_PREVIOUS_START_DATE, mStartDate);
            outState.putLong(FILTER_PREVIOUS_END_DATE, mEndDate);
            outState.putLong(FILTER_PREVIOUS_TYPE_INDEX, mTypeIndex);
            outState.putLong(FILTER_PREVIOUS_CATEGORY_INDEX, mCategoryIndex);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (savedInstanceState != null) {
                // Restore previous filter dates
                mStartDate = savedInstanceState.getLong(FILTER_PREVIOUS_START_DATE);
                mEndDate = savedInstanceState.getLong(FILTER_PREVIOUS_END_DATE);
                mTypeIndex = savedInstanceState.getInt(FILTER_PREVIOUS_TYPE_INDEX);
                mCategoryIndex = savedInstanceState.getInt(FILTER_PREVIOUS_CATEGORY_INDEX);
            }
            // Update TextViews again after date is restored
            if (mStartDate != 0 && mStartDatePicker != null) {
                mStartDatePicker.setText(mFilterDateFormat.format(mStartDate));
            }
            if (mEndDate != 0 && mEndDatePicker != null) {
                mEndDatePicker.setText(mFilterDateFormat.format(mEndDate));
            }
            // Update course type Spinner selection after it's restored
            mCourseTypeSpinner.setSelection(mTypeIndex);
            // Update course category Spinner selection after it's restored
            mCourseCategorySpinner.setSelection(mCategoryIndex);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.course_filter_dialog, container);
            WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
            layoutParams.gravity = Gravity.TOP;

            mFilterDateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

            mStartDatePicker = (TextView) rootView.findViewById(R.id.start_date_picker);
            mEndDatePicker = (TextView) rootView.findViewById(R.id.end_date_picker);

            // Workaround to set changeable primary color with custom dialog theme
            int primaryColor = ThemeUtils.getThemeColor(getActivity(), R.attr.colorPrimary);
            rootView.findViewById(R.id.course_filter_dialog_android_view).setBackgroundColor(primaryColor);
            mStartDatePicker.setTextColor(primaryColor);
            mEndDatePicker.setTextColor(primaryColor);

            // Course type spinner
            mCourseTypeSpinner = (Spinner) rootView.findViewById(R.id.search_type_spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> courseTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.filter_type_spinner, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            courseTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            mCourseTypeSpinner.setAdapter(courseTypeAdapter);
            // Set default value
            mCourseTypeSpinner.setSelection(mTypeIndex);
            // Set spinner item selection listener
            mCourseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    // Save spinner type index selection
                    mTypeIndex = i;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            // Course category spinner
            mCourseCategorySpinner = (Spinner) rootView.findViewById(R.id.search_category_spinner);
            // Get categories set from shared preference
            Set<String> categoriesSet = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getStringSet(Constants.PREF_CATEGORIES, new HashSet<String>());
            // Create a new array list and add categories by language
            ArrayList<String> categoriesArray = new ArrayList<>();
            for (String categoryValues : categoriesSet) {
                String[] values = categoryValues.split(";");
                categoriesArray.add(values[Locale.getDefault().toString().toLowerCase().equals("en") ? 1 : 2]);
            }
            // Add a title to the array list on top
            categoriesArray.add(0, getString(R.string.all_course_categories));
            // Create an ArrayAdapter using the categories array list and a default spinner layout
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, categoriesArray);
            // Specify the layout to use when the list of choices appears
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            mCourseCategorySpinner.setAdapter(categoryAdapter);
            // Set default value
            mCourseCategorySpinner.setSelection(mCategoryIndex);
            // Set spinner item selection listener
            mCourseCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    // Save spinner category index selection
                    mCategoryIndex = i;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            // Dismiss dialog button
            ImageButton dismiss = (ImageButton) rootView.findViewById(R.id.course_filter_dismiss);
            dismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getDialog().dismiss();
                }
            });

            // Apply filter button
            Button filter = (Button) rootView.findViewById(R.id.apply_filter);
            filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getDialog().dismiss();
                    ((SearchActivity) getActivity()).applyFilter(mStartDate, mEndDate, mTypeIndex, mCategoryIndex);
                }
            });

            // Reset filter button
            Button reset = (Button) rootView.findViewById(R.id.reset_filter);
            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    getDialog().dismiss();
                    mStartDate = mEndDate = 0;
                    mTypeIndex = mCategoryIndex = 0;
                    mCourseTypeSpinner.setSelection(0);
                    mCourseCategorySpinner.setSelection(0);
                    mStartDatePicker.setText(R.string.course_filter_select_date_action);
                    mEndDatePicker.setText(R.string.course_filter_select_date_action);
//                    ((SearchActivity) getActivity()).resetFilter();
                }
            });

            mCalendar = Calendar.getInstance();

            //  Start date dialog picker
            final DatePickerDialog.OnDateSetListener startDatePickerDialog = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    // Get dates from dialog picker
                    mCalendar.set(Calendar.YEAR, year);
                    mCalendar.set(Calendar.MONTH, monthOfYear);
                    mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    // Ignore the time
                    mCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    mCalendar.set(Calendar.MINUTE, 0);
                    mCalendar.set(Calendar.SECOND, 0);
                    mCalendar.set(Calendar.MILLISECOND, 0);
                    // Update TextView
                    mStartDatePicker.setText(mFilterDateFormat.format(mCalendar.getTime()));
                    // Save start date long
                    mStartDate = mCalendar.getTimeInMillis();
                }
            };

            // Open start date dialog picker
            rootView.findViewById(R.id.start_date_picker_clickable_area)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Open dialog picker with today's date
                            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), startDatePickerDialog, mCalendar
                                    .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                                    mCalendar.get(Calendar.DAY_OF_MONTH));
                            // Set minimum date to now
                            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                            // If start date already chosen, open dialog with this date
                            if (mStartDate != 0) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(mStartDate);
                                datePickerDialog.updateDate(calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH));
                            }
                            // Set maximum date to end date if already chosen
                            if (mEndDate != 0) {
                                datePickerDialog.getDatePicker().setMaxDate(mEndDate);
                            }
                            datePickerDialog.show();
                        }
                    });

            final DatePickerDialog.OnDateSetListener endDatePickerDialog = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    // Get dates from dialog picker
                    mCalendar.set(Calendar.YEAR, year);
                    mCalendar.set(Calendar.MONTH, monthOfYear);
                    mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    // Ignore the time
                    mCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    mCalendar.set(Calendar.MINUTE, 0);
                    mCalendar.set(Calendar.SECOND, 0);
                    mCalendar.set(Calendar.MILLISECOND, 0);
                    // Update TextView
                    mEndDatePicker.setText(mFilterDateFormat.format(mCalendar.getTime()));
                    // Save end date long
                    mEndDate = mCalendar.getTimeInMillis();
                }
            };

            rootView.findViewById(R.id.end_date_picker_clickable_area)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Open dialog picker with today's date
                            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), endDatePickerDialog, mCalendar
                                    .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                            // If end date already chosen, open dialog with this date
                            if (mEndDate != 0) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(mEndDate);
                                datePickerDialog.updateDate(calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH));
                            }
                            // Set minimum date to start date if already chosen
                            if (mStartDate != 0) {
                                datePickerDialog.getDatePicker().setMinDate(mStartDate);
                            } else {
                                // Set minimum date to now
                                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                            }
                            datePickerDialog.show();
                        }
                    });

            return rootView;
        }
    }

    public void applyFilter(long startDate, long endDate, int spinnerTypeIndex, int spinnerCategoryIndex) {
        // Save final filter parameters
        mFinalStartDate = startDate;
        mFinalEndDate = endDate;
        mFinalTypeIndex = spinnerTypeIndex;
        mFinalCategoryIndex = spinnerCategoryIndex;
        if (startDate == 0 && endDate == 0 && spinnerTypeIndex == 0 && spinnerCategoryIndex == 0) {
            resetFilter();
            return;
        }
        advancedFilter(startDate, endDate, spinnerTypeIndex, spinnerCategoryIndex);
    }

    public void resetFilter() {
        String query = mSearchView.getQuery() == null ? "" : mSearchView.getQuery().toString();
        if (mFilteredCourseList != null) {
            showResults(query, mFilteredCourseList, false);
        }
        mFinalTypeIndex = mFinalCategoryIndex = 0;
        mFinalEndDate = mFinalStartDate = 0;
    }

    private void advancedFilter(long startDate, long endDate, int spinnerTypeIndex, int spinnerCategoryIndex) {
        ArrayList<Course> currentFilteredList = new ArrayList<>();
        try {
            if (mFilteredCourseList != null && mFilteredCourseList.size() > 0) {
                for (Course course : mFilteredCourseList) {
                    if (course.getCycles() != null) {
                        for (Cycle cycle : course.getCycles()) {
                            try {
                                long cycleStartTime = cycle.getStartDate().getTime();
                                long cycleEndTime = cycle.getEndDate().getTime();

                                if (startDate != 0 && endDate != 0) {
                                    // Searching for courses in a date range
                                    if (cycleStartTime >= startDate && (cycleEndTime <= endDate)) {
                                        currentFilteredList.add(course);
                                    }
                                    break;
                                } else if (startDate != 0) {
                                    // Searching for courses after start date without end date limit
                                    if (cycleStartTime >= startDate) {
                                        currentFilteredList.add(course);
                                    }
                                    break;
                                } else if (endDate != 0) {
                                    // Searching for courses before end date without start date limit
                                    if (cycleEndTime <= endDate) {
                                        currentFilteredList.add(course);
                                    }
                                    break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // Filter courses by course type
                    if (spinnerTypeIndex != 0) {
                        if (startDate == 0 && endDate == 0) {
                            // no courses was added as search filter was not selected, add all
                            currentFilteredList.add(course);
                        }
                        if (spinnerTypeIndex == 1) {
                            if (course.getIsMooc()) {
                                // Add only regular courses - remove MOOCs
                                currentFilteredList.remove(course);
                            }
                        } else if (spinnerTypeIndex == 2) {
                            // Add only online courses - remove frontal
                            if (!course.getIsMooc()) {
                                currentFilteredList.remove(course);
                            }
                        }
                    }
                    // Filter courses by course category
                    Set<String> categories = PreferenceManager.getDefaultSharedPreferences(this)
                            .getStringSet(Constants.PREF_CATEGORIES, new HashSet<String>());
                    ArrayList<String> categoriesArray = new ArrayList<>();
                    for (String categoryValues : categories) {
                        String[] values = categoryValues.split(";");
                        categoriesArray.add(values[0]);
                    }
                    if (spinnerCategoryIndex != 0) {
                        if (startDate == 0 && endDate == 0 && spinnerTypeIndex == 0) {
                            currentFilteredList.add(course); // no previous filter was selected, add all
                        }
                        if (!course.getCategory().equals(categoriesArray.get(spinnerCategoryIndex - 1 /*don't count title index*/))) {
                            currentFilteredList.remove(course);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            showResults(mFilterDateFormat.format(startDate) + mFilterDateFormat.format(endDate), currentFilteredList, true);
        }
    }

    private void filter(String query) {
        if (query == null || query.equals("")) {
            mFilteredCourseList = new ArrayList<>(mCoursesList);
        } else {
            mFilteredCourseList = new ArrayList<>();
            for (Course item : mCoursesList) {
                if ((item.getName() != null && item.getName().toLowerCase().contains(query.toLowerCase().trim())) ||
                        (item.getDescription() != null && item.getDescription().toLowerCase().contains(query.toLowerCase().trim())) ||
                        (item.getSyllabus() != null && item.getSyllabus().toLowerCase().contains(query.toLowerCase().trim())) ||
                        isHasCycle(item, query.toLowerCase().trim())) {
                    mFilteredCourseList.add(item);
                }
            }
        }
        showResults(query, mFilteredCourseList, false);
    }

    private void showResults(String query, ArrayList<Course> listToShow, boolean filter) {
        if (listToShow.isEmpty()) {
            String searchResult;
            if (filter) {
                searchResult = getString(R.string.no_results_for_filter);
            } else {
                searchResult = String.format(getString(
                        R.string.no_results_for_query), query);
                isEmptyResult = true;
            }
            mNoResults.setText(searchResult);
            mNoResultsContainer.setVisibility(View.VISIBLE);
            mFilterNoticeGroup.setVisibility(View.GONE);
        } else {
            mNoResultsContainer.setVisibility(View.GONE);
            isEmptyResult = false;
            if (filter) {
                mFilterNoticeGroup.setVisibility(View.VISIBLE);

                int filterCount = mFinalStartDate != 0 || mFinalEndDate != 0 ? 1 : 0; // filtered by date
                if (mFinalTypeIndex != 0) filterCount++; // filtered by course type
                if (mFinalCategoryIndex != 0) filterCount++; // filtered by course category

                if (filterCount > 1) {
                    // 2 active filters
                    mCourseFilterNotice.setText(String.format(getString(R.string.active_filter_count_notice), filterCount));
                } else if (mFinalTypeIndex != 0) {
                    // Searching only courses by type
                    mCourseFilterNotice.setText(String.format(getString(R.string.active_filter_type_notice),
                            getResources().getStringArray(R.array.filter_type_spinner)[mFinalTypeIndex]));
                } else if (mFinalCategoryIndex != 0) {
                    // Searching only courses by type
                    Set<String> categories = PreferenceManager.getDefaultSharedPreferences(this)
                            .getStringSet(Constants.PREF_CATEGORIES, new HashSet<String>());
                    ArrayList<String> arrayList = new ArrayList<>(categories);
                    mCourseFilterNotice.setText(String.format(getString(R.string.active_filter_category_notice),
                            arrayList.get(mFinalCategoryIndex - 1).split(";")[
                                    Locale.getDefault().toString().toLowerCase().equals("en") ? 1 : 2]));
                } else {
                    if (mFinalStartDate != 0 && mFinalEndDate != 0) {
                        // Searching for courses in a date range
                        mCourseFilterNotice.setText(String.format(getString(R.string.active_filter_date_range_notice),
                                mFilterDateFormat.format(mFinalStartDate),
                                mFilterDateFormat.format(mFinalEndDate)));
                    } else if (mFinalStartDate != 0) {
                        // Searching for courses after start date without end date limit
                        mCourseFilterNotice.setText(String.format(getString(R.string.active_filter_beginning_date_notice),
                                mFilterDateFormat.format(mFinalStartDate)));
                    } else {
                        // Searching for courses before end date without start date limit
                        mCourseFilterNotice.setText(String.format(getString(R.string.active_filter_ending_date_notice),
                                mFilterDateFormat.format(mFinalEndDate)));
                    }
                }

                mResetFilter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resetFilter();
                    }
                });
            } else {
                mFilterNoticeGroup.setVisibility(View.GONE);
            }
        }
        mAdapter.animateTo(listToShow);
        mRecycler.scrollToPosition(0);
    }

    private boolean isHasCycle(Course course, String filterText) {

        if (course.getCycles() == null || course.getCycles().size() == 0) {
            return false;
        } else {
            for (Cycle cycle : course.getCycles()) {
                if (isTextIncludeInCycle(cycle, filterText)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isTextIncludeInCycle(Cycle cycle, String text) {

        int day, month;
        text = text.replace(".", "/");
        String[] textParts = text.split("/");

        if (textParts.length == 1) {
            try {
                Calendar startCalendar, endCalendar;
                int searchNumber, startDay, endDay, startMonth, endMonth;
                searchNumber = Integer.valueOf(text);

                startCalendar = Calendar.getInstance();
                startCalendar.setTime(cycle.getStartDate());
                startDay = startCalendar.get(Calendar.DAY_OF_MONTH);
                startMonth = startCalendar.get(Calendar.MONTH);

                endCalendar = Calendar.getInstance();
                endCalendar.setTime(cycle.getEndDate());
                endDay = startCalendar.get(Calendar.DAY_OF_MONTH);
                endMonth = startCalendar.get(Calendar.MONTH);

                if ((searchNumber >= startDay || searchNumber <= endDay) ||
                        searchNumber == endMonth || searchNumber == startMonth) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                day = Integer.valueOf(textParts[0]);
                month = Integer.valueOf(textParts[1]) - 1;
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.getInstance().get(Calendar.YEAR), month, day);
                long searchTimeStamp = calendar.getTime().getTime();
                long startTimeStamp = cycle.getStartDate().getTime();
                startTimeStamp -= (startTimeStamp % 86400000);
                long endTimeStamp = cycle.getEndDate().getTime();
                endTimeStamp -= (endTimeStamp % 86400000);

                if (searchTimeStamp >= startTimeStamp && searchTimeStamp <= endTimeStamp) {
                    return true;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        }
        return false;
    }
}