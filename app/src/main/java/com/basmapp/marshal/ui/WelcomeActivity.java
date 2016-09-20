package com.basmapp.marshal.ui;

import android.animation.ArgbEvaluator;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.ui.widget.InkPageIndicator;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private ImageButton mNextBtn;
    private Button mSkipBtn, mDoneBtn;

    int page = 0;   //  to track page position

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black_trans80));
        }

        setContentView(R.layout.activity_welcome);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mNextBtn = (ImageButton) findViewById(R.id.next);
        mSkipBtn = (Button) findViewById(R.id.skip);
        mDoneBtn = (Button) findViewById(R.id.done);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(sectionsPagerAdapter);
        InkPageIndicator inkPageIndicator = (InkPageIndicator) findViewById(R.id.intro_indicator);
        inkPageIndicator.setViewPager(mViewPager);
        mViewPager.setCurrentItem(page);

        final int color1 = ContextCompat.getColor(this, R.color.cyan_primary_color);
        final int color2 = ContextCompat.getColor(this, R.color.orange_primary_color);
        final int color3 = ContextCompat.getColor(this, R.color.green_primary_color);

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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);

            String[] titles = new String[]{getActivity().getResources().getString(R.string.welcome_courses_title),
                    getActivity().getResources().getString(R.string.welcome_materials_title),
                    getActivity().getResources().getString(R.string.welcome_malshab_title)};

            TextView title = (TextView) rootView.findViewById(R.id.heading);
            title.setText(titles[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);

            String[] subtitles = new String[]{getActivity().getResources().getString(R.string.welcome_courses_subtitle),
                    getActivity().getResources().getString(R.string.welcome_materials_subtitle),
                    getActivity().getResources().getString(R.string.welcome_malshab_subtitle)};

            TextView subtitle = (TextView) rootView.findViewById(R.id.blurb);
            subtitle.setText(subtitles[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);

            int[] backgrounds = new int[]{R.drawable.warm_welcome_student,
                    R.drawable.warm_welcome_backpack,
                    R.drawable.warm_welcome_teach
            };

            ImageView images = (ImageView) rootView.findViewById(R.id.welcome_image);
            images.setImageResource(backgrounds[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
