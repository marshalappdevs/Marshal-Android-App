package com.basmapp.marshal.ui;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Rating;
import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmapp.marshal.ui.adapters.RatingsRecyclerAdapter;
import com.basmapp.marshal.ui.utils.LocaleUtils;
import com.basmapp.marshal.ui.utils.ThemeUtils;

import java.util.List;

public class RatingsActivity extends AppCompatActivity {

    Toolbar mToolbar;
    Course mCourse;
    List<Rating> mRatings;
    RecyclerView mRecycler;
    RatingsRecyclerAdapter mAdapter;

    String mRatingsAmount;
    String mRatingAverage;
    float mRatingBarStars;

    TextView mTextViewRatingsAmount;
    TextView mTextViewRatingAverage;
    RatingBar mRatingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.updateTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ratings);

        mCourse = getIntent().getParcelableExtra(Constants.EXTRA_COURSE);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(mCourse.getName());
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mTextViewRatingAverage = (TextView) findViewById(R.id.activity_ratings_textView_average_value);
        mTextViewRatingsAmount = (TextView) findViewById(R.id.course_content_textView_ratingsAmount);
        mRatingBar = (RatingBar) findViewById(R.id.summary_rating_bar);

        mRecycler = (RecyclerView) findViewById(R.id.activity_ratings_recyclerView);
        mRecycler.setLayoutManager(new LinearLayoutManager(RatingsActivity.this));
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        mRecycler.setNestedScrollingEnabled(false);

        mRatingAverage = getIntent().getStringExtra(Constants.EXTRA_RATING_AVERAGE);
        mRatingsAmount = getIntent().getStringExtra(Constants.EXTRA_RATING_AMOUNT);
        mRatingBarStars = getIntent().getFloatExtra(Constants.EXTRA_RATING_BAR_STARS, 0);

        mTextViewRatingAverage.setText(mRatingAverage);
        mTextViewRatingsAmount.setText(mRatingsAmount);
        mRatingBar.setRating(mRatingBarStars);

        if (mCourse != null) {
            Rating.getByColumnInBackground(true, DBConstants.COL_COURSE_CODE, mCourse.getCourseCode(),
                    DBConstants.COL_LAST_MODIFIED, RatingsActivity.this, Rating.class, new BackgroundTaskCallBack() {
                        @Override
                        public void onSuccess(String result, List<Object> data) {
                            mRatings = (List)data;
                            initializeRecycler();
                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
        }
    }

    private void initializeRecycler() {
        mAdapter = new RatingsRecyclerAdapter(RatingsActivity.this, mRatings);
        mRecycler.setAdapter(mAdapter);
    }
}
