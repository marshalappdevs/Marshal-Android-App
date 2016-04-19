package com.basmach.marshal.ui.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.ui.CourseActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {
    Context context;
    private ArrayList<Course> COURSES = new ArrayList<>();

    public ViewPagerAdapter(Context context, ArrayList<Course> COURSES) {
        this.COURSES = new ArrayList<>(COURSES);
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
        Picasso.with(context)
                .load(COURSES.get(position).getImageUrl())
                .placeholder(R.drawable.highlights_bottom_overlay)
                .fit()
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

            Intent intent = new Intent(context, CourseActivity.class);
            intent.putExtra(CourseActivity.EXTRA_COURSE, COURSES.get(position));
            context.startActivity(intent);
        }
    };

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}