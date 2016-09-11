package com.basmapp.marshal.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.ui.CourseActivity;
import com.basmapp.marshal.ui.MainActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {
    Context context;
    private ArrayList<Course> COURSES = new ArrayList<>();

    public ViewPagerAdapter(Context context, ArrayList<Course> COURSES) {
        this.COURSES = COURSES;
        this.context=context;
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
                .diskCacheStrategy(DiskCacheStrategy.ALL)
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
            intent.putExtra(Constants.EXTRA_COURSE, COURSES.get(position));
            ((Activity) context).startActivityForResult(intent, MainActivity.RC_COURSE_ACTIVITY);
        }
    };

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}