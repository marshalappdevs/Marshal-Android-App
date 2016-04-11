package com.basmach.marshal.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.ui.CourseActivity;
import com.basmach.marshal.utils.DateHelper;
import com.squareup.picasso.Callback;

import java.util.ArrayList;
import java.util.List;

public class CoursesRecyclerAdapter extends RecyclerView.Adapter<CoursesRecyclerAdapter.CourseVH> {

    public static final int LAYOUT_TYPE_LIST = 1;
    public static final int LAYOUT_TYPE_GRID = 2;

    private Context mContext;
    private ArrayList<Course> mCourses;
    private int mRecyclerLayoutType;

    public CoursesRecyclerAdapter(Context context, ArrayList<Course> courses, int layoutType) {
        this.mCourses = courses;
        this.mContext = context;
        this.mRecyclerLayoutType = layoutType;
    }

    @Override
    public CourseVH onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i("COURSES_RECYCLER", "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_cardview, null);
        return new CourseVH(view);
    }

    @Override
    public void onBindViewHolder(final CourseVH holder, final int position) {

        Log.i("COURSES_RECYCLER", "onBindViewHolder");

        // Set card onClickListener
        final long[] mLastClickTime = {0};
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime[0] < 1000) {
                    return;
                }
                mLastClickTime[0] = SystemClock.elapsedRealtime();
                Intent intent = new Intent(mContext, CourseActivity.class);
                intent.putExtra(CourseActivity.EXTRA_COURSE, mCourses.get(position));
                List<Pair<View, String>> pairs = new ArrayList<>();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    View decor = ((Activity)mContext).getWindow().getDecorView();
                    View statusBar = decor.findViewById(android.R.id.statusBarBackground);
                    View navigationBar = decor.findViewById(android.R.id.navigationBarBackground);
                    if (statusBar != null) pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
                    if (navigationBar != null) pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
                }
                pairs.add(Pair.create(view.findViewById(R.id.course_cardview_fake_space), "course_toolbar"));
                pairs.add(Pair.create(view.findViewById(R.id.course_cardview_image), "course_image"));
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, pairs.toArray(new Pair[pairs.size()]));
                mContext.startActivity(intent, options.toBundle());
            }
        });

        // Set card onLongClickListener
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(mContext, mCourses.get(position).getName(), Toast.LENGTH_LONG).show();
                return true;
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

            Log.i("COURSES_RECYCLER", "CourseVH Ctor");

            frameLayout = (FrameLayout) itemView.findViewById(R.id.course_cardview_mainFrame);
            cardView = (CardView) itemView.findViewById(R.id.course_cardview_widget) ;
            courseImage = (ImageView) itemView.findViewById(R.id.course_cardview_image);
            moocFlag = (ImageView) itemView.findViewById(R.id.course_cardview_moocFlag);
            courseName = (TextView) itemView.findViewById(R.id.course_cardview_name);
            courseStartDateTime = (TextView) itemView.findViewById(R.id.course_cardview_startDateTime);
            courseProgressBar = (ProgressBar) itemView.findViewById(R.id.course_cardview_progressBar);

            if (mRecyclerLayoutType == LAYOUT_TYPE_GRID) {
                CardView.LayoutParams cardLayoutParams = (CardView.LayoutParams) cardView.getLayoutParams();
                cardLayoutParams.width = CardView.LayoutParams.MATCH_PARENT;
                cardLayoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, mContext.getResources().getDisplayMetrics());
                int dp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
                cardLayoutParams.setMargins(dp4, dp4*3, dp4 ,dp4);

                courseName.setMaxLines(2);
                courseName.setEllipsize(TextUtils.TruncateAt.END);
            }
        }
    }
}
