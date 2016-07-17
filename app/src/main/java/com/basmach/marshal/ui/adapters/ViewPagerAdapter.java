package com.basmach.marshal.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.ui.CourseActivity;
import com.basmach.marshal.ui.MainActivity;
import com.basmach.marshal.utils.DateHelper;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {

    private TextView subtitleTv;
    private TextView titleTv;
    Context context;
    private ArrayList<Course> COURSES = new ArrayList<>();

    public ViewPagerAdapter(Context context, ArrayList<Course> COURSES, TextView titleTV, TextView subtitleTV) {
        this.COURSES = COURSES;
        this.context=context;
        this.titleTv = titleTV;
        this.subtitleTv = subtitleTV;
    }

    @Override
    public int getCount() {
        return COURSES.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(context)
                .load(COURSES.get(position).getImageUrl())
                .into(imageView);
        imageView.setOnClickListener(imageClickListener);
        imageView.setTag(position);
        container.addView(imageView, 0);
        return imageView;
    }

    private View.OnClickListener imageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = (Integer) v.getTag();
            PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit().putBoolean("courseShared", false).apply();
            Intent intent = new Intent(context, CourseActivity.class);
            intent.putExtra(CourseActivity.EXTRA_COURSE, COURSES.get(position));
            ((Activity) context).startActivityForResult(intent, MainActivity.RC_COURSE_ACTIVITY);
        }
    };

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }


}