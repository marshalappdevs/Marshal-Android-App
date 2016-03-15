package com.basmach.marshal.ui;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.ui.utils.CoursesRecyclerAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class ShowAllCoursesActivity extends AppCompatActivity {

    public static final String EXTRA_COURSES_LIST = "courses_list";
    public static final String EXTRA_COURSE_TYPE = "course_type";

    RecyclerView mRecyclerView;
    GridLayoutManager mGridLayoutManager;
    CoursesRecyclerAdapter mAdapter;

    String mCoursesType;
    ArrayList<Course> mCourses;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateTheme();
        super.onCreate(savedInstanceState);
        updateLocale();
        setContentView(R.layout.activity_show_all_courses);

        mCoursesType = getIntent().getStringExtra(EXTRA_COURSE_TYPE);
        mCourses = getIntent().getParcelableArrayListExtra(EXTRA_COURSES_LIST);

        Toolbar toolbar = (Toolbar) findViewById(R.id.showAllCourses_activity_toolbar);

        if (mCoursesType != null) toolbar.setTitle(mCoursesType);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (mCourses.size() > 0) {
            mRecyclerView = (RecyclerView) findViewById(R.id.showAllCourses_activity_recyclerView);
            mGridLayoutManager = new GridLayoutManager(ShowAllCoursesActivity.this, 3);
            mRecyclerView.setLayoutManager(mGridLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setHasFixedSize(true);
            mAdapter = new CoursesRecyclerAdapter(ShowAllCoursesActivity.this, mCourses, CoursesRecyclerAdapter.LAYOUT_TYPE_GRID);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLocale();
    }

    private void updateTheme() {
        String theme = mSharedPreferences.getString("THEME", "light");
        if (theme.equals("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        if (theme.equals("auto")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        }
        getDelegate().applyDayNight();
        setTheme(R.style.AppTheme);
    }

    private void updateLocale() {
        Configuration config = getBaseContext().getResources().getConfiguration();
        String lang = mSharedPreferences.getString("LANG", "iw");
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            Locale locale = new Locale(lang);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.setLocale(locale);
            Locale.setDefault(locale);
            res.updateConfiguration(conf, dm);
        }
    }
}
