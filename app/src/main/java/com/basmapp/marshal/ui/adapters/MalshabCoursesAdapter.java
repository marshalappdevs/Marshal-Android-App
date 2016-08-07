package com.basmapp.marshal.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.MalshabItem;
import com.bumptech.glide.Glide;

import java.util.List;

public final class MalshabCoursesAdapter extends BaseAdapter {
    private List<MalshabItem> mMalshabItems;
    private Context mContext;
    private final LayoutInflater mInflater;
    private SharedPreferences mSharedPreferences;

    public MalshabCoursesAdapter(Context context, List<MalshabItem> malshabItems) {
        this.mContext = context;
        this.mMalshabItems = malshabItems;
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mMalshabItems.size();
    }

    @Override
    public MalshabItem getItem(int position) {
        return mMalshabItems.get(position);
    }

    @Override
    public long getItemId(int i) {
        return mMalshabItems.get(i).getId();
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        ImageView picture;
        TextView text;

        if (view == null) {
            view = mInflater.inflate(R.layout.malshab_course_gridview, viewGroup, false);
            view.setTag(R.id.picture, view.findViewById(R.id.picture));
            view.setTag(R.id.text, view.findViewById(R.id.text));
        }

        picture = (ImageView) view.getTag(R.id.picture);
        text = (TextView) view.getTag(R.id.text);

        final MalshabItem item = getItem(position);

        if (item.getTitle() != null) {
            text.setText(item.getTitle());
        }

        if (item.getImageUrl() != null && !item.getImageUrl().equals("")) {
            Glide.with(mContext).load(item.getImageUrl()).into(picture);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean cct = mSharedPreferences.getBoolean("CCT", true);
                if (cct) {
                    new CustomTabsIntent.Builder()
                            .setToolbarColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                            .setShowTitle(true)
                            .addDefaultShareMenuItem()
                            .setCloseButtonIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_arrow_back_wht))
                            .build()
                            .launchUrl((Activity) mContext, Uri.parse(item.getUrl()));
                } else {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl())));
                }
            }
        });

        return view;
    }
}
