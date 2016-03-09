package com.basmach.marshal.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.ui.utils.CoursesRecyclerAdapter;
import com.basmach.marshal.ui.utils.InkPageIndicator;
import com.basmach.marshal.ui.utils.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class CoursesFragment extends Fragment {
    public ArrayList<String> IMAGES;
    public ArrayList<Course> COURSES;

    private ViewPager mViewPager;
    private TimerTask mTimerTask;
    private Timer mTimer;
    private Handler mTimerTaskHandler = new Handler();

    private RecyclerView mRecycler;
    private LinearLayoutManager mLinearLayoutManager;
    private CoursesRecyclerAdapter mRecyclerAdapter;
    private Button mBtnShowAll;

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

        ////////////////////////////////////////////////////////////////////////

        COURSES = new ArrayList<>();
        Course courseCyber = new Course();
        courseCyber.setPhotoUrl(IMAGES.get(0));
        courseCyber.setName("Cyber Warfare Techniques");
        courseCyber.setIsMooc(false);
        COURSES.add(courseCyber);
        Course courseAngular = new Course();
        courseAngular.setPhotoUrl(IMAGES.get(1));
        courseAngular.setName("Angular JS");
        courseAngular.setIsMooc(true);
        COURSES.add(courseAngular);
        Course courseAndroid = new Course();
        courseAndroid.setPhotoUrl(IMAGES.get(2));
        courseAndroid.setName("Android Applications Development");
        courseAndroid.setIsMooc(false);
        COURSES.add(courseAndroid);
        Course courseFrontend = new Course();
        courseFrontend.setPhotoUrl(IMAGES.get(3));
        courseFrontend.setName("Frontend - HTML5, CSS, JavaScript");
        courseFrontend.setIsMooc(false);
        COURSES.add(courseFrontend);
        Course coursePhotshop = new Course();
        coursePhotshop.setPhotoUrl(IMAGES.get(4));
        coursePhotshop.setName("Advanced Photoshop");
        coursePhotshop.setIsMooc(true);
        COURSES.add(coursePhotshop);

        //////////////////////////////////////////////////////////////////////////////////

        mViewPager = (ViewPager) rootView.findViewById(R.id.main_catalog_view_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity(), IMAGES);
        mViewPager.setAdapter(adapter);

        InkPageIndicator inkPageIndicator = (InkPageIndicator) rootView.findViewById(R.id.main_catalog_indicator);
        inkPageIndicator.setViewPager(mViewPager);

        startViewPagerTimer();
        stopViewPagerTimerOnTouch();

        // Recycler
        mBtnShowAll = (Button) rootView.findViewById(R.id.fragment_courses_programming_btnShowAll);
        mBtnShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Show all", Toast.LENGTH_LONG).show();
            }
        });
        mRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_courses_programming_recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecycler.setLayoutManager(mLinearLayoutManager);
        mRecyclerAdapter = new CoursesRecyclerAdapter(getActivity(), COURSES);
        mRecycler.setAdapter(mRecyclerAdapter);
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