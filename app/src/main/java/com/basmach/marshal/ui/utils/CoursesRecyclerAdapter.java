package com.basmach.marshal.ui.utils;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.ui.CourseActivity;
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
    public void onBindViewHolder(final CourseVH holder, final int position) {

        // Set card onClickListener
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(mContext, CourseActivity.class);
//                intent.putExtra(CourseActivity.EXTRA_COURSE,mCourses.get(position));
//                mContext.startActivity(intent);
                CourseActivity.navigate((AppCompatActivity)mContext, holder.courseImage, mCourses.get(position));
            }
        });

        // Set course title
        holder.courseName.setText(mCourses.get(position).getName());

        // Set course starting Date
        if (mCourses.get(position).getCycles().size() > 0) {
            holder.courseStartDateTime
                    .setText(DateHelper.dateToString(((Cycle)
                            (mCourses.get(position).getCycles().get(0))).getStartDate()));
        }

        // Set course image
        mCourses.get(position).getPhotoViaPicasso(mContext, holder.courseImage, holder.courseProgressBar);

        // Check if MOOC
        if(mCourses.get(position).getIsMooc()){
            // if (holder.courseImage.getVisibility() == View.VISIBLE)
            holder.moocFlag.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    public class CourseVH extends RecyclerView.ViewHolder{

        FrameLayout frameLayout;
        CardView cardView;
        ImageView courseImage;
        ImageView moocFlag;
        TextView courseName;
        TextView courseStartDateTime;
        ProgressBar courseProgressBar;

        public CourseVH(View itemView) {
            super(itemView);

            frameLayout = (FrameLayout) itemView.findViewById(R.id.course_cardview_mainFrame);
            cardView = (CardView) itemView.findViewById(R.id.course_cardview_widget) ;
            courseImage = (ImageView) itemView.findViewById(R.id.course_cardview_image);
            moocFlag = (ImageView) itemView.findViewById(R.id.course_cardview_moocFlag);
            courseName = (TextView) itemView.findViewById(R.id.course_cardview_name);
            courseStartDateTime = (TextView) itemView.findViewById(R.id.course_cardview_startDateTime);
            courseProgressBar = (ProgressBar) itemView.findViewById(R.id.course_cardview_progressBar);
        }
    }
}
