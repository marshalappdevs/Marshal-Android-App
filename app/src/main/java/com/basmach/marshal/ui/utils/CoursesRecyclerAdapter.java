package com.basmach.marshal.ui.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Ido on 3/9/2016.
 */
public class CoursesRecyclerAdapter extends RecyclerView.Adapter<CoursesRecyclerAdapter.CourseVH> {

    private Context mContext;
    private ArrayList<Course> mCourses;

    public CoursesRecyclerAdapter(Context context, ArrayList<Course> courses) {
        this.mCourses = courses;
        this.mContext = context;
    }

    @Override
    public CourseVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.courses_list_item, null);
        CourseVH viewHolder = new CourseVH(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CourseVH holder, int position) {

        // Set course title
        holder.textView.setText(mCourses.get(position).getName());

        // Set course image
        mCourses.get(position).getPhotoViaPicasso(mContext, holder.imageView);

        // Check if MOOC
        if(mCourses.get(position).getIsMooc()){
            holder.moocFlag.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    public class CourseVH extends RecyclerView.ViewHolder{

        RoundedImageView imageView;
        TextView textView;
        ImageView moocFlag;

        public CourseVH(View itemView) {
            super(itemView);

            imageView = (RoundedImageView) itemView.findViewById(R.id.courses_list_item_imageView);
            moocFlag = (ImageView) itemView.findViewById(R.id.courses_list_item_moocFlag);
            textView = (TextView) itemView.findViewById(R.id.courses_list_item_textView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, "Course Activity", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
