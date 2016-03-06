package com.basmach.marshal.ui.fragments;

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

public class CoursesFragment extends Fragment {
    public ArrayList<String> IMAGES;
    private ViewPager mViewPager;
    private TimerTask mTimerTask;
    private Timer mTimer;
    private Handler mTimerTaskHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_courses, container, false);

        IMAGES = new ArrayList<>();
        IMAGES.add("http://cdn2.hubspot.net/hubfs/206683/cyber-security-training.jpg?t%5Cu003d1430137590751");
        IMAGES.add("https://www.dunebook.com/wp-content/uploads/2015/07/angular-dunebook.png");
        IMAGES.add("http://www.wingnity.com/uploads/Courses/1396070428_android-course.png");
        IMAGES.add("https://academy.mymagic.my/app/uploads/2015/08/FRONTEND_ma-01-700x400-c-default.jpg");
        IMAGES.add("https://udemy-images.udemy.com/course/750x422/352132_74cf_2.jpg");

        mViewPager = (ViewPager) rootView.findViewById(R.id.main_catalog_view_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity(), IMAGES);
        mViewPager.setAdapter(adapter);

        InkPageIndicator inkPageIndicator = (InkPageIndicator) rootView.findViewById(R.id.main_catalog_indicator);
        inkPageIndicator.setViewPager(mViewPager);

        startViewPagerTimer();
        stopViewPagerTimerOnTouch();

        return rootView;
    }

    private void startViewPagerTimer() {
        mTimer = new Timer();
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
        mTimer.schedule(mTimerTask, 5000, 5000);
    }

    private void stopViewPagerTimerOnTouch() {
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer = null;
                        }
                        startViewPagerTimer();
                    }
                }, 2000);
                return false;
            }
        });
    }
}