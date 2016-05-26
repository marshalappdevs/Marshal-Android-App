package com.basmach.marshal.ui.adapters;

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

import com.basmach.marshal.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public final class MalshabCoursesAdapter extends BaseAdapter {
    private Context mContext;
    private final List<Item> mItems = new ArrayList<>();
    private final LayoutInflater mInflater;
    private SharedPreferences mSharedPreferences;

    public MalshabCoursesAdapter(Context context) {
        this.mContext = context;
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mInflater = LayoutInflater.from(context);

        mItems.add(new Item(R.string.programmer,
                "http://www.tikshuv.idf.il/Sip_Storage/FILES/2/1532.jpg",
                "http://www.tikshuv.idf.il/1048-7486-HE/tikshuv.aspx"));
        mItems.add(new Item(R.string.programing_course,
                "http://i.imgur.com/NlOBP30.png",
                "http://basmach.github.io/tichnut/"));
        mItems.add(new Item(R.string.cyber_defender,
                "http://www.tikshuv.idf.il/Sip_Storage/FILES/3/1523.jpg",
                "http://www.tikshuv.idf.il/1048-7536-HE/tikshuv.aspx"));
        mItems.add(new Item(R.string.cyber_defender_course,
                "http://i.imgur.com/NlOBP30.png",
                "http://basmach.github.io/cyber/"));
        mItems.add(new Item(R.string.infrastructure_manager,
                "http://www.tikshuv.idf.il/Sip_Storage/FILES/9/1539.jpg",
                "http://www.tikshuv.idf.il/1048-7490-HE/tikshuv.aspx"));
        mItems.add(new Item(R.string.software_qa,
                "http://www.tikshuv.idf.il/Sip_Storage/FILES/3/1533.jpg",
                "http://www.tikshuv.idf.il/1048-7504-HE/tikshuv.aspx"));
        mItems.add(new Item(R.string.educational_tools_developer,
                "http://www.tikshuv.idf.il/Sip_Storage/FILES/4/1554.jpg",
                "http://www.tikshuv.idf.il/1048-7537-HE/tikshuv.aspx"));
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Item getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).name;
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

        final Item item = getItem(position);

        text.setText(item.name);

        Picasso.with(mContext)
                .load(item.image)
                .into(picture);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext, item.name, Toast.LENGTH_SHORT).show();
                Boolean cct = mSharedPreferences.getBoolean("CCT", true);
                if (cct) {
                    new CustomTabsIntent.Builder()
                            .setToolbarColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                            .setShowTitle(true)
                            .addDefaultShareMenuItem()
                            .setCloseButtonIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_arrow_back_wht))
                            .build()
                            .launchUrl((Activity) mContext, Uri.parse(item.url));
                } else {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.url)));
                }
            }
        });
//        view.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_DIAL);
//                intent.setData(Uri.parse("tel:*3808"));
//                mContext.startActivity(intent);
//                return false;
//            }
//        });
        return view;
    }

    private static class Item {
        public final int name;
        public final String image;
        public final String url;

        Item(int name, String image, String url) {
            this.name = name;
            this.image = image;
            this.url = url;
        }
    }
}
