package com.basmach.marshal.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.ui.CourseActivity;
import com.basmach.marshal.ui.utils.CyclesRecyclerAdapter;

import java.util.ArrayList;

/**
 * Created by Ido on 3/14/2016.
 */
public class CyclesBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public static CyclesBottomSheetDialogFragment newInstance(Course course){
        CyclesBottomSheetDialogFragment instance = new CyclesBottomSheetDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(CourseActivity.EXTRA_COURSE, course);
        instance.setArguments(bundle);

        return instance;
    }

    RecyclerView mRecyclerView;
    LinearLayoutManager mLinearLayoutManager;
    CyclesRecyclerAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_cycles, container, false);

        try {
            Course course = getArguments().getParcelable(CourseActivity.EXTRA_COURSE);
            ArrayList<Cycle> cycles = course.getCycles();

            // Initialize RecyclerView
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.cycle_activity_recyclerView);
            mLinearLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLinearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mAdapter = new CyclesRecyclerAdapter(getActivity(), cycles);
            mRecyclerView.setAdapter(mAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }
}
