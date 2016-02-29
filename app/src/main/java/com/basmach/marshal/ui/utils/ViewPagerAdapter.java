package com.basmach.marshal.ui.utils;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {
    Context context;
    private ArrayList<String> IMAGES = new ArrayList<>();

    public ViewPagerAdapter(Context context, ArrayList<String> IMAGES) {
        this.IMAGES = IMAGES;
        this.context=context;
    }

    @Override
    public int getCount() {
        return IMAGES.size();
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
                .load(IMAGES.get(position))
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
            final int tag = (Integer) v.getTag();
            switch (tag) {
                case 0:
                    Toast.makeText(context, "Item 1 Clicked", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(context, "Item 2 Clicked", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(context, "Item 3 Clicked", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(context, "Item 4 Clicked", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(context, "Item 5 Clicked", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}