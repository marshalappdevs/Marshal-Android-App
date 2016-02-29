package com.basmach.marshal.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.basmach.marshal.R;
import com.basmach.marshal.ui.utils.InkPageIndicator;
import com.basmach.marshal.ui.utils.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CoursesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CoursesFragment extends Fragment {
    private ViewPager mViewPager;
    private TimerTask mTimerTask;
    private Timer mTimer;
    Handler mTimerTaskHandler = new Handler();
    public ArrayList<String> IMAGES;

    private OnFragmentInteractionListener mListener;

    public CoursesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        IMAGES = new ArrayList<>();
        IMAGES.add("http://cdn2.hubspot.net/hubfs/206683/cyber-security-training.jpg?t%5Cu003d1430137590751");
        IMAGES.add("http://tutorialedge.net/uploads/courses/angularjs.png");
        IMAGES.add("http://www.wingnity.com/uploads/Courses/1396070428_android-course.png");
        IMAGES.add("https://academy.mymagic.my/app/uploads/2015/08/FRONTEND_ma-01-700x400-c-default.jpg");
        IMAGES.add("https://udemy-images.udemy.com/course/750x422/352132_74cf_2.jpg");

        mViewPager = (ViewPager) getActivity().findViewById(R.id.main_catalog_view_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity(), IMAGES);
        mViewPager.setAdapter(adapter);

        InkPageIndicator inkPageIndicator = (InkPageIndicator) getActivity().findViewById(R.id.main_catalog_indicator);
        inkPageIndicator.setViewPager(mViewPager);

        startImagesTimer();
        stopTimerOnTouch();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }

    public void stopTimerOnTouch() {
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                stopImagesTimerTask();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer = null;
                        }
                        startImagesTimer();
                    }
                }, 2000);
                return false;
            }
        });
    }

    public void stopImagesTimerTask() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void startImagesTimer() {
        // Set a new timer
        mTimer = new Timer();
        // Initialize the TimerTask's job
        initializeImagesTimerTask();
        // Schedule the timer, after the first 5000ms the TimerTask will run every 5000ms
        mTimer.schedule(mTimerTask, 5000, 5000);
    }

    private void initializeImagesTimerTask() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mTimerTaskHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mViewPager.getCurrentItem() < mViewPager.getAdapter().getCount() - 1) {
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                        } else {
                            mViewPager.setCurrentItem(0);
                        }
                    }
                });
            }
        };
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopImagesTimerTask();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
