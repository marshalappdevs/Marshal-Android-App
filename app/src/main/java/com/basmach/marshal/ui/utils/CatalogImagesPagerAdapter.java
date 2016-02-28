package com.basmach.marshal.ui.utils;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.basmach.marshal.R;

public class CatalogImagesPagerAdapter extends PagerAdapter {
    Context context;
    private int[] AllImages = new int[] {
            R.drawable.cyber,
            R.drawable.java,
            R.drawable.ux,
            R.drawable.cloud,
            R.drawable.photoshop
    };

    public CatalogImagesPagerAdapter(Context context){
        this.context=context;
    }

    @Override
    public int getCount() {
        return AllImages.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageResource(AllImages[position]);
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}
