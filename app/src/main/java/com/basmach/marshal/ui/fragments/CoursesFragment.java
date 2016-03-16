package com.basmach.marshal.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
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
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.ui.ShowAllCoursesActivity;
import com.basmach.marshal.ui.utils.CoursesRecyclerAdapter;
import com.basmach.marshal.ui.utils.InkPageIndicator;
import com.basmach.marshal.ui.utils.ViewPagerAdapter;
import com.basmach.marshal.utils.DateHelper;

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

    private RecyclerView mRecyclerSoftware;
    private LinearLayoutManager mLinearLayoutManagerSoftware;
    private CoursesRecyclerAdapter mRecyclerAdapterSoftware;
    private Button mBtnShowAllSoftware;

    private RecyclerView mRecyclerCyber;
    private LinearLayoutManager mLinearLayoutManagerCyber;
    private CoursesRecyclerAdapter mRecyclerAdapterCyber;
    private Button mBtnShowAllCyber;

    private RecyclerView mRecyclerIT;
    private LinearLayoutManager mLinearLayoutManagerIT;
    private CoursesRecyclerAdapter mRecyclerAdapterIT;
    private Button mBtnShowAllIT;

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
        courseCyber.setDescription("ההשתלמות היא השתלמות מתקדמת בשפת C++, באה להעלות תוכניתן C++ רמה אחת למעלה. ");
        courseCyber.setTargetPopulation("אקדמאים בוגרי מקצועות המחשב, או תוכניתנים אשר מתוכננים לפתח פרוייקטים ב- ++C.");
        courseCyber.setSyllabus("History & Future C++ Recap Copying and Conversions Scope Subscripting Techniques Delegation Techniques Templates Iterators and Algorithms Exceptions Memory Management Inheritance Techniques Template Techniques Miscellaneous Techniques ");
        courseCyber.setDayTime("בוקר-צהריים-ערב");
        courseCyber.setDurationInDays(5);
        courseCyber.setDurationInHours(45);
        courseCyber.setCourseCode("1951");
        Cycle cyberCycle1 = new Cycle();
        cyberCycle1.setStartDate(DateHelper.stringToDate("23/06/16"));
        cyberCycle1.setEndDate(DateHelper.stringToDate("01/07/16"));
        cyberCycle1.setName(courseCyber.getName());
        cyberCycle1.setDescription(courseCyber.getDescription());
        courseCyber.addCycle(cyberCycle1);
        Cycle cyberCycle2 = new Cycle();
        cyberCycle2.setStartDate(DateHelper.stringToDate("05/09/16"));
        cyberCycle2.setEndDate(DateHelper.stringToDate("10/09/16"));
        cyberCycle2.setName(courseCyber.getName());
        cyberCycle2.setDescription(courseCyber.getDescription());
        courseCyber.addCycle(cyberCycle2);
        Cycle cyberCycle3 = new Cycle();
        cyberCycle3.setStartDate(DateHelper.stringToDate("08/05/17"));
        cyberCycle3.setEndDate(DateHelper.stringToDate("13/05/17"));
        cyberCycle3.setName(courseCyber.getName());
        cyberCycle3.setDescription(courseCyber.getDescription());
        courseCyber.addCycle(cyberCycle3);

        courseCyber.addCycle(cyberCycle1);
        courseCyber.addCycle(cyberCycle2);
        courseCyber.addCycle(cyberCycle3);
        courseCyber.addCycle(cyberCycle1);
        courseCyber.addCycle(cyberCycle2);
        courseCyber.addCycle(cyberCycle3);
        courseCyber.addCycle(cyberCycle1);
        courseCyber.addCycle(cyberCycle2);
        courseCyber.addCycle(cyberCycle3);
        courseCyber.addCycle(cyberCycle1);
        courseCyber.addCycle(cyberCycle2);
        courseCyber.addCycle(cyberCycle3);
        COURSES.add(courseCyber);

        Course courseAngular = new Course();
        courseAngular.setPhotoUrl(IMAGES.get(1));
        courseAngular.setName("Angular JS");
        courseAngular.setIsMooc(true);
        Cycle angularCycle = new Cycle();
        angularCycle.setStartDate(DateHelper.stringToDate("17/05/16"));
        courseAngular.addCycle(angularCycle);
        COURSES.add(courseAngular);

        Course courseAndroid = new Course();
        courseAndroid.setPhotoUrl(IMAGES.get(2));
        courseAndroid.setName("Android Applications Development");
        courseAndroid.setIsMooc(false);
        Cycle androidCycle = new Cycle();
        androidCycle.setStartDate(DateHelper.stringToDate("04/09/16"));
        courseAndroid.addCycle(angularCycle);
        COURSES.add(courseAndroid);

        Course courseFrontend = new Course();
        courseFrontend.setPhotoUrl(IMAGES.get(3));
        courseFrontend.setName("Frontend - HTML5, CSS, JavaScript");
        courseFrontend.setIsMooc(false);
        Cycle frontendCycle = new Cycle();
        frontendCycle.setStartDate(DateHelper.stringToDate("27/07/16"));
        courseFrontend.addCycle(frontendCycle);
        COURSES.add(courseFrontend);

        Course coursePhotshop = new Course();
        coursePhotshop.setPhotoUrl(IMAGES.get(4));
        coursePhotshop.setName("Advanced Photoshop");
        coursePhotshop.setIsMooc(true);
        Cycle photoshopCycle = new Cycle();
        photoshopCycle.setStartDate(DateHelper.stringToDate("11/04/16"));
        coursePhotshop.addCycle(photoshopCycle);
        COURSES.add(coursePhotshop);

        //////////////////////////////////////////////////////////////////////////////////

        mViewPager = (ViewPager) rootView.findViewById(R.id.main_catalog_view_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity(), IMAGES);
        mViewPager.setAdapter(adapter);

        InkPageIndicator inkPageIndicator = (InkPageIndicator) rootView.findViewById(R.id.main_catalog_indicator);
        inkPageIndicator.setViewPager(mViewPager);

        startViewPagerTimer();
        stopViewPagerTimerOnTouch();

        initializeSoftwareComponents(rootView);
        initializeCyberComponents(rootView);
        initializeITComponents(rootView);
        return rootView;
    }

    private void initializeSoftwareComponents(View rootView) {
        mBtnShowAllSoftware = (Button) rootView.findViewById(R.id.fragment_courses_software_seeAll);
        mBtnShowAllSoftware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST, COURSES);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_software));
                startActivity(intent);
            }
        });
        mRecyclerSoftware = (RecyclerView) rootView.findViewById(R.id.fragment_courses_software_recyclerView);
        mLinearLayoutManagerSoftware = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerSoftware.setLayoutManager(mLinearLayoutManagerSoftware);
        mRecyclerAdapterSoftware = new CoursesRecyclerAdapter(getActivity(), COURSES, CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerSoftware.setItemAnimator(new DefaultItemAnimator());
        mRecyclerSoftware.setAdapter(mRecyclerAdapterSoftware);
    }

    private void initializeCyberComponents(View rootView) {
        mBtnShowAllCyber = (Button) rootView.findViewById(R.id.fragment_courses_cyber_seeAll);
        mBtnShowAllCyber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST, COURSES);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_cyber));
                startActivity(intent);
            }
        });
        mRecyclerCyber = (RecyclerView) rootView.findViewById(R.id.fragment_courses_cyber_recyclerView);
        mLinearLayoutManagerCyber = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerCyber.setLayoutManager(mLinearLayoutManagerCyber);
        mRecyclerAdapterCyber = new CoursesRecyclerAdapter(getActivity(), COURSES, CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerCyber.setItemAnimator(new DefaultItemAnimator());
        mRecyclerCyber.setAdapter(mRecyclerAdapterCyber);
    }

    private void initializeITComponents(View rootView) {
        mBtnShowAllIT = (Button) rootView.findViewById(R.id.fragment_courses_it_seeAll);
        mBtnShowAllIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST, COURSES);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_it));
                startActivity(intent);
            }
        });
        mRecyclerIT = (RecyclerView) rootView.findViewById(R.id.fragment_courses_it_recyclerView);
        mLinearLayoutManagerIT = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerIT.setLayoutManager(mLinearLayoutManagerIT);
        mRecyclerAdapterIT = new CoursesRecyclerAdapter(getActivity(), COURSES, CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerIT.setItemAnimator(new DefaultItemAnimator());
        mRecyclerIT.setAdapter(mRecyclerAdapterIT);
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