package com.basmapp.marshal.ui;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Rating;
import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmapp.marshal.ui.adapters.RatingsRecyclerAdapter;

import java.util.List;

public class RatingsActivity extends BaseActivity {

    private List<Rating> mRatings;
    private RecyclerView mRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ratings);

        Course course = getIntent().getParcelableExtra(Constants.EXTRA_COURSE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(course.getName());
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView mTextViewRatingAverage = (TextView) findViewById(R.id.activity_ratings_textView_average_value);
        TextView mTextViewRatingsAmount = (TextView) findViewById(R.id.course_content_textView_ratingsAmount);
        RatingBar mRatingBar = (RatingBar) findViewById(R.id.summary_rating_bar);

        mRecycler = (RecyclerView) findViewById(R.id.activity_ratings_recyclerView);
        mRecycler.setLayoutManager(new LinearLayoutManager(RatingsActivity.this));
        mRecycler.setItemAnimator(new DefaultItemAnimator());
//        mRecycler.setNestedScrollingEnabled(false);

        mTextViewRatingAverage.setText(getIntent().getStringExtra(Constants.EXTRA_RATING_AVERAGE));
        mTextViewRatingsAmount.setText(getIntent().getStringExtra(Constants.EXTRA_RATING_AMOUNT));
        mRatingBar.setRating(getIntent().getFloatExtra(Constants.EXTRA_RATING_BAR_STARS, 0));

        Rating.getByColumnInBackground(true, DBConstants.COL_COURSE_CODE, course.getCourseCode(),
                DBConstants.COL_LAST_MODIFIED, RatingsActivity.this, Rating.class, new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        mRatings = (List) data;
                        initializeRecycler();
                    }

                    @Override
                    public void onError(String error) {

                    }
                });
    }

    private void initializeRecycler() {
        RatingsRecyclerAdapter ratingsRecyclerAdapter = new RatingsRecyclerAdapter(RatingsActivity.this, mRatings);
        mRecycler.setAdapter(ratingsRecyclerAdapter);
    }
}
