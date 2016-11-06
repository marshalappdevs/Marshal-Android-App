package com.basmapp.marshal.ui;

import android.animation.ArgbEvaluator;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.ui.widget.InkPageIndicator;

public class WelcomeActivity extends BaseActivity {

    private ViewPager mViewPager;
    private ImageButton mNextBtn;
    private Button mSkipBtn, mDoneBtn;
    static final int NUM_ITEMS = 3;

    int page = 0;   //  to track page position

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        setContentView(R.layout.activity_welcome);

        // Create the adapter that will return a fragment for each position
        WelcomePagerAdapter welcomePagerAdapter = new WelcomePagerAdapter(getSupportFragmentManager());

        mNextBtn = (ImageButton) findViewById(R.id.next);
        mSkipBtn = (Button) findViewById(R.id.skip);
        mDoneBtn = (Button) findViewById(R.id.done);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(welcomePagerAdapter);
        InkPageIndicator inkPageIndicator = (InkPageIndicator) findViewById(R.id.intro_indicator);
        inkPageIndicator.setViewPager(mViewPager);
        mViewPager.setCurrentItem(page);

        final int color1 = ContextCompat.getColor(this, R.color.cyan_primary);
        final int color2 = ContextCompat.getColor(this, R.color.green_primary);
        final int color3 = ContextCompat.getColor(this, R.color.teal_primary);

        final int[] colorList = new int[]{color1, color2, color3};

        final ArgbEvaluator evaluator = new ArgbEvaluator();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Change color on viewpager scroll
                int colorUpdate = (Integer) evaluator.evaluate(positionOffset, colorList[position], colorList[position == 2 ? position : position + 1]);
                mViewPager.setBackgroundColor(colorUpdate);
            }

            @Override
            public void onPageSelected(int position) {

                page = position;

                switch (position) {
                    case 0:
                        mViewPager.setBackgroundColor(color1);
                        break;
                    case 1:
                        mViewPager.setBackgroundColor(color2);
                        break;
                    case 2:
                        mViewPager.setBackgroundColor(color3);
                        break;
                }

                mNextBtn.setVisibility(position == 2 ? View.GONE : View.VISIBLE);
                mSkipBtn.setVisibility(position == 2 ? View.GONE : View.VISIBLE);
                mDoneBtn.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page += 1;
                mViewPager.setCurrentItem(page, true);
            }
        });

        mSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //  Set show warm welcome shared preference to false so it will not show up again
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putBoolean(Constants.PREF_SHOW_WARM_WELCOME, false).apply();
    }

    public static class PlaceholderFragment extends Fragment {
        final static String ARG_POSITION = "position";
        int mPosition;

        public static PlaceholderFragment newInstance(int position) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_POSITION, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPosition = getArguments() != null ? getArguments().getInt(ARG_POSITION) : 1;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);

            String[] titles = new String[]{getActivity().getResources().getString(R.string.welcome_courses_title),
                    getActivity().getResources().getString(R.string.welcome_materials_title),
                    getActivity().getResources().getString(R.string.welcome_malshab_title)};

            TextView headline = (TextView) rootView.findViewById(R.id.welcome_headline);
            headline.setText(titles[mPosition]);

            String[] subtitles = new String[]{getActivity().getResources().getString(R.string.welcome_courses_subtitle),
                    getActivity().getResources().getString(R.string.welcome_materials_subtitle),
                    getActivity().getResources().getString(R.string.welcome_malshab_subtitle)};

            TextView subhead = (TextView) rootView.findViewById(R.id.welcome_subhead);
            subhead.setText(subtitles[mPosition]);

            int[] images = new int[]{R.drawable.warm_welcome_student,
                    R.drawable.warm_welcome_backpack,
                    R.drawable.warm_welcome_teach
            };

            ImageView image = (ImageView) rootView.findViewById(R.id.welcome_image);
            image.setImageResource(images[mPosition]);

            return rootView;
        }
    }

    public class WelcomePagerAdapter extends FragmentPagerAdapter {

        WelcomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }
}
