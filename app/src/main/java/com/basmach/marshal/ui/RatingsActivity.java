package com.basmach.marshal.ui;

import android.animation.Animator;
import android.os.Build;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Rating;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.ui.adapters.RatingsRecyclerAdapter;
import com.basmach.marshal.ui.utils.LocaleUtils;
import com.basmach.marshal.ui.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

public class RatingsActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE = "extra_course";
    public static final String EXTRA_RATING_AMOUNT = "extra_rating_amount";
    public static final String EXTRA_RATING_AVERAGE = "extra_rating_average";
    public static final String EXTRA_RATING_BAR_STARS = "extra_rating_bar_stars";

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
        LocaleUtils.updateLocale(this);

        setContentView(R.layout.activity_ratings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }

        mTextViewRatingAverage = (TextView) findViewById(R.id.activity_ratings_textView_average_value);
        mTextViewRatingsAmount = (TextView) findViewById(R.id.course_content_textView_ratingsAmount);
        mRatingBar = (RatingBar) findViewById(R.id.summary_rating_bar);

        mRecycler = (RecyclerView) findViewById(R.id.activity_ratings_recyclerView);
        mRecycler.setLayoutManager(new LinearLayoutManager(RatingsActivity.this));
        mRecycler.setItemAnimator(new DefaultItemAnimator());

        mRatingAverage = getIntent().getStringExtra(EXTRA_RATING_AVERAGE);
        mRatingsAmount = getIntent().getStringExtra(EXTRA_RATING_AMOUNT);
        mRatingBarStars = getIntent().getFloatExtra(EXTRA_RATING_BAR_STARS, 0);

        mTextViewRatingAverage.setText(mRatingAverage);
        mTextViewRatingsAmount.setText(mRatingsAmount);
        mRatingBar.setRating(mRatingBarStars);

        mCourse = getIntent().getParcelableExtra(EXTRA_COURSE);

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
