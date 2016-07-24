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

import com.basmach.marshal.Constants;
import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.ui.CourseActivity;
import com.basmach.marshal.ui.adapters.CyclesRecyclerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CyclesBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private ArrayList<Cycle> mCycles;

    public static CyclesBottomSheetDialogFragment newInstance(Course course){
        CyclesBottomSheetDialogFragment instance = new CyclesBottomSheetDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.EXTRA_COURSE, course);
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
            Course course = getArguments().getParcelable(Constants.EXTRA_COURSE);
            mCycles = course.getCycles();

            orderCyclesByAscending();

            // Initialize RecyclerView
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.cycle_activity_recyclerView);
            mLinearLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLinearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mAdapter = new CyclesRecyclerAdapter(getActivity(), mCycles, course);
            mRecyclerView.setAdapter(mAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    private void orderCyclesByAscending() {
        Collections.sort(mCycles, new Comparator<Cycle>() {
            public int compare(Cycle cycle1, Cycle cycle2) {
                if (cycle1.getStartDate() == null || cycle2.getStartDate() == null)
                    return 0;
                return cycle1.getStartDate().compareTo(cycle2.getStartDate());
            }
        });
    }
}
