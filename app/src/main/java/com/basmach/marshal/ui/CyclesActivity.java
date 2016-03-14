package com.basmach.marshal.ui;

import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.ui.utils.CyclesRecyclerAdapter;

import java.util.ArrayList;

public class CyclesActivity extends AppCompatActivity {

    CoordinatorLayout mCoordinatorLayout;
    RelativeLayout mRelativeLayout;

    RecyclerView mRecyclerView;
    LinearLayoutManager mLinearLayoutManager;
    CyclesRecyclerAdapter mAdapter;

    Course mCourse;
    ArrayList<Cycle> mCycles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cycles);

        mRelativeLayout = (RelativeLayout) findViewById(R.id.cycle_activity_mainRelativeLayout);

        // Initialize RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.cycle_activity_recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Get course from Intent
        mCourse = getIntent().getParcelableExtra(CourseActivity.EXTRA_COURSE);

        if (mCourse != null) {
            mCycles = mCourse.getCycles();
            if (mCycles.size() > 0) {
                mAdapter = new CyclesRecyclerAdapter(CyclesActivity.this, mCycles);
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    }
}
