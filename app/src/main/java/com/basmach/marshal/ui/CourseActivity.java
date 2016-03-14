package com.basmach.marshal.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class CourseActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE = "course_extra";

    private Toolbar mToolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private SharedPreferences mSharedPreferences;

    private Course mCourse;

    private TextView mTextViewDescription;
    private TextView mTextViewSyllabus;
    private TextView mTextViewCourseCode;
    private TextView mTextViewTargetPopulation;
    private TextView mTextViewDayTime;
    private TextView mTextViewDaysDuration;
    private TextView mTextViewHoursDuration;

    private int contentColor = -1;
    private int scrimColor = -1;

    private FloatingActionButton mFabCycles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateTheme();
        super.onCreate(savedInstanceState);
        updateLocale();
        initActivityTransitions();
        setContentView(R.layout.activity_course);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supportFinishAfterTransition();
            }
        });

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        // hide toolbar expanded title
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));

        //Initialize Cycles FAB
        mFabCycles = (FloatingActionButton) findViewById(R.id.course_activity_fab_cycles);

        mCourse = getIntent().getParcelableExtra(EXTRA_COURSE);
        if (mCourse != null) {
            Log.i("Course Activity", "course passed");

            // Initialize Cycles FAB onClick event
            mFabCycles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(CourseActivity.this, CyclesActivity.class);
                    intent.putExtra(EXTRA_COURSE, mCourse);
                    startActivity(intent);
                }
            });

            // Set the course photo
            final ImageView header = (ImageView) findViewById(R.id.header);
            Picasso.with(this).load(mCourse.getPhotoUrl()).into(header, new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap bitmap = ((BitmapDrawable) header.getDrawable()).getBitmap();
                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            contentColor = palette.getMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            scrimColor = ContextCompat.getColor(getApplicationContext(), R.color.black_trans80);
                            collapsingToolbarLayout.setStatusBarScrimColor(scrimColor);
                            collapsingToolbarLayout.setContentScrimColor(contentColor);
//                            paintTitlesTextColor(contentColor);
//                            collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimaryDark)));
                        }
                    });
                }

                @Override
                public void onError() {

                }
            });

            // Set the course title
            collapsingToolbarLayout.setTitle(mCourse.getName());

            mTextViewDescription = (TextView) findViewById(R.id.course_content_textView_description);
            mTextViewSyllabus = (TextView) findViewById(R.id.course_content_textView_syllabus);
            mTextViewCourseCode = (TextView) findViewById(R.id.course_content_textView_courseCode);
            mTextViewTargetPopulation = (TextView) findViewById(R.id.course_content_textView_targetPopulation);
            mTextViewDayTime = (TextView) findViewById(R.id.course_content_textView_dayTime);
            mTextViewDaysDuration = (TextView) findViewById(R.id.course_content_textView_daysDuration);
            mTextViewHoursDuration = (TextView) findViewById(R.id.course_content_textView_hoursDuration);

            initializeTextViews();
        }
    }

    private void initializeTextViews() {
        // Set course's Description
        if ((mCourse.getDescription() != null) &&
                (!mCourse.getDescription().equals(""))) {
            mTextViewDescription.setText(mCourse.getDescription());
        } else {
            findViewById(R.id.course_content_relativeLayout_description).setVisibility(View.GONE);
        }

        // Set course's Syllabus
        if ((mCourse.getSyllabus() != null) &&
                (!mCourse.getSyllabus().equals(""))) {
            mTextViewSyllabus.setText(mCourse.getSyllabus());
        } else {
            findViewById(R.id.course_content_relativeLayout_syllabus).setVisibility(View.GONE);
        }

        // Set course's Code
        if ((mCourse.getCourseCode() != null) &&
                (!mCourse.getCourseCode().equals(""))) {
            mTextViewCourseCode.setText(mCourse.getCourseCode());
        } else {
            findViewById(R.id.course_content_relativeLayout_id).setVisibility(View.GONE);
        }

        // Set course's Target population
        if ((mCourse.getTargetPopulation() != null) &&
                (!mCourse.getTargetPopulation().equals(""))) {
            mTextViewTargetPopulation.setText(mCourse.getTargetPopulation());
        } else {
            findViewById(R.id.course_content_relativeLayout_targetPopulation).setVisibility(View.GONE);
        }

        // Set course's DayTime
        if ((mCourse.getDayTime() != null) &&
                (!mCourse.getDayTime().equals(""))) {
            mTextViewDayTime.setText(mCourse.getDayTime());
        } else {
            findViewById(R.id.course_content_relativeLayout_dayTime).setVisibility(View.GONE);
        }

        // Set course's Days duration
        if (mCourse.getDurationInDays() != 0) {
            mTextViewDaysDuration.setText(String.valueOf(mCourse.getDurationInDays()));
        } else {
            findViewById(R.id.course_content_relativeLayout_dayDuration).setVisibility(View.GONE);
        }

        // Set course's Hours duration
        if (mCourse.getDurationInHours() != 0) {
            mTextViewHoursDuration.setText(String.valueOf(mCourse.getDurationInHours()));
        } else {
            findViewById(R.id.course_content_relativeLayout_hoursDuration).setVisibility(View.GONE);
        }
    }

    private void paintTitlesTextColor(int contentColor) {
        if (contentColor != -1) {
            ((TextView) (findViewById(R.id.course_content_textView_descriptionTitle))).setTextColor(contentColor);
            ((TextView) (findViewById(R.id.course_content_textView_syllabusTitle))).setTextColor(contentColor);
            ((TextView) (findViewById(R.id.course_content_textView_idTitle))).setTextColor(contentColor);
            ((TextView) (findViewById(R.id.course_content_textView_targetPopulationTitle))).setTextColor(contentColor);
            ((TextView) (findViewById(R.id.course_content_textView_dayTimeTitle))).setTextColor(contentColor);
            ((TextView) (findViewById(R.id.course_content_textView_daysDurationTitle))).setTextColor(contentColor);
            ((TextView) (findViewById(R.id.course_content_textView_hoursDurationTitle))).setTextColor(contentColor);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLocale();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_course_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.course_menu_item_related_materials) {
            Toast.makeText(CourseActivity.this, "Related Materials", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.course_menu_item_share) {
            Toast.makeText(CourseActivity.this, "Share", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
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

    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Fade fade = new Fade();
            fade.excludeTarget(android.R.id.statusBarBackground, true);
            fade.excludeTarget(android.R.id.navigationBarBackground, true);
            getWindow().setEnterTransition(fade);
            getWindow().setReturnTransition(fade);
        }
    }
}
