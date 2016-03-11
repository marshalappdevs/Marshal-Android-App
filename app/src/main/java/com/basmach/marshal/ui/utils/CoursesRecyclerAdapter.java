package com.basmach.marshal.ui.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;

import java.util.ArrayList;

public class CoursesRecyclerAdapter extends RecyclerView.Adapter<CoursesRecyclerAdapter.CourseVH> {

    private Context mContext;
    private ArrayList<Course> mCourses;

    public CoursesRecyclerAdapter(Context context, ArrayList<Course> courses) {
        this.mCourses = courses;
        this.mContext = context;
    }

    @Override
    public CourseVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_cardview, null);
        CourseVH viewHolder = new CourseVH(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CourseVH holder, int position) {

        // Set course title
        holder.courseName.setText(mCourses.get(position).getName());

        // Set course starting Date
        holder.courseStartDateTime.setText(mCourses.get(position).getDayTime());

        // Set course image
        mCourses.get(position).getPhotoViaPicasso(mContext, holder.courseImage);

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

        ImageView courseImage;
        ImageView moocFlag;
        TextView courseName;
        TextView courseStartDateTime;

        public CourseVH(View itemView) {
            super(itemView);

            courseImage = (ImageView) itemView.findViewById(R.id.course_cardview_image);
            moocFlag = (ImageView) itemView.findViewById(R.id.course_cardview_moocFlag);
            courseName = (TextView) itemView.findViewById(R.id.course_cardview_name);
            courseStartDateTime = (TextView) itemView.findViewById(R.id.course_cardview_startDateTime);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, "Syllabus Activity", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
