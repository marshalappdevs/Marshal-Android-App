package com.basmach.marshal.ui;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Rating;
import com.basmach.marshal.ui.adapters.CoursesRecyclerAdapter;
import com.basmach.marshal.ui.utils.LocaleUtils;
import com.basmach.marshal.ui.utils.ThemeUtils;

import java.util.ArrayList;

public class ShowAllCoursesActivity extends AppCompatActivity {

    public static final String EXTRA_COURSES_LIST = "courses_list";
    public static final String EXTRA_COURSE_TYPE = "course_type";
    public static final String EXTRA_RATINGS_LIST = "extra_ratings";

    RecyclerView mRecyclerView;
    GridLayoutManager mGridLayoutManager;
    CoursesRecyclerAdapter mAdapter;

    String mCoursesType;
    ArrayList<Course> mCourses;
    ArrayList<Rating> mRatings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        LocaleUtils.updateLocale(this);
        setContentView(R.layout.activity_show_all_courses);

        mCoursesType = getIntent().getStringExtra(EXTRA_COURSE_TYPE);
        mCourses = getIntent().getParcelableArrayListExtra(EXTRA_COURSES_LIST);
        mRatings = getIntent().getParcelableArrayListExtra(EXTRA_RATINGS_LIST);

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

        if (mCourses != null) {
            if (mCourses.size() > 0) {
                mRecyclerView = (RecyclerView) findViewById(R.id.showAllCourses_activity_recyclerView);
                mGridLayoutManager = new GridLayoutManager(ShowAllCoursesActivity.this, 3);
                mRecyclerView.setLayoutManager(mGridLayoutManager);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setHasFixedSize(true);
                mAdapter = new CoursesRecyclerAdapter(ShowAllCoursesActivity.this, mCourses, mRatings,
                        CoursesRecyclerAdapter.LAYOUT_TYPE_GRID);
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleUtils.updateLocale(this);
    }
}
