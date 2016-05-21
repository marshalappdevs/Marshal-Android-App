package com.basmach.marshal.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public final class MalshabCoursesAdapter extends BaseAdapter {
    private Context mContext;
    private final List<Item> mItems = new ArrayList<>();
    private final LayoutInflater mInflater;

    public MalshabCoursesAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);

        mItems.add(new Item(R.string.programmer,
                "http://www.tikshuv.idf.il/Sip_Storage/FILES/2/1532.jpg"));
        mItems.add(new Item(R.string.infrastructure_manager,
                "http://www.tikshuv.idf.il/Sip_Storage/FILES/9/1539.jpg"));
        mItems.add(new Item(R.string.software_qa,
                "http://www.tikshuv.idf.il/Sip_Storage/FILES/3/1533.jpg"));
        mItems.add(new Item(R.string.cyber_defender,
                "http://www.tikshuv.idf.il/Sip_Storage/FILES/3/1523.jpg"));
        mItems.add(new Item(R.string.educational_tools_developer,
                "http://www.tikshuv.idf.il/Sip_Storage/FILES/4/1554.jpg"));
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
        return Long.parseLong(mItems.get(position).image);
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        View v = view;
        ImageView picture;
        TextView name;

        if (v == null) {
            v = mInflater.inflate(R.layout.malshab_course_gridview, viewGroup, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
            v.setTag(R.id.text, v.findViewById(R.id.text));
        }

        picture = (ImageView) v.getTag(R.id.picture);
        name = (TextView) v.getTag(R.id.text);

        final Item item = getItem(position);

        name.setText(item.name);

        int width= mContext.getResources().getDisplayMetrics().widthPixels;
        Picasso.with(mContext)
                .load(item.image)
                .centerCrop().resize(width/2,width/2)
                .into(picture);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, item.name, Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    private static class Item {
        public final int name;
        public final String image;

        Item(int name, String image) {
            this.name = name;
            this.image = image;
        }
    }
}
