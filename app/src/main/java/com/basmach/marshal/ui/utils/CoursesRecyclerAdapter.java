package com.basmach.marshal.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
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
import com.squareup.picasso.Callback;

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
                Intent intent = new Intent(mContext, CourseActivity.class);
                intent.putExtra(CourseActivity.EXTRA_COURSE, mCourses.get(position));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ImageView courseImage = (ImageView) view.findViewById(R.id.course_cardview_image);
                    TextView courseName = (TextView) view.findViewById(R.id.course_cardview_name);
                    Pair<View, String> p1 = Pair.create((View) courseImage, "course_image");
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, p1);
                    mContext.startActivity(intent, options.toBundle());
                } else {
                    mContext.startActivity(intent);
                }
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
        mCourses.get(position).getPhotoViaPicasso(mContext, holder.courseImage,  new Callback() {
            @Override public void onSuccess() {
                holder.courseProgressBar.setVisibility(View.GONE);
                holder.courseImage.setVisibility(View.VISIBLE);

                // Check if MOOC
                if(mCourses.get(position).getIsMooc()){
                    // if (holder.courseImage.getVisibility() == View.VISIBLE)
                    holder.moocFlag.setVisibility(View.VISIBLE);
                }
            }

            @Override public void onError() {

            }
        });
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
