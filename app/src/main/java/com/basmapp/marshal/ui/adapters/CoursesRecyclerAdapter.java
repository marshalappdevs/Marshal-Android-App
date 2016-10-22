package com.basmapp.marshal.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.entities.Rating;
import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmapp.marshal.ui.CourseActivity;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.util.DateHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class CoursesRecyclerAdapter extends RecyclerView.Adapter<CoursesRecyclerAdapter.CourseVH> {

    public static final int LAYOUT_TYPE_LIST = 1;
    public static final int LAYOUT_TYPE_GRID = 2;
    public static final String ACTION_ITEM_DATA_CHANGED = "com.basmapp.marshal.ACTION_courses_adapter_item_data_changed";

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_cardview, null);
        return new CourseVH(view);
    }

    @Override
    public void onBindViewHolder(final CourseVH holder, final int position) {

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
                intent.putExtra(Constants.EXTRA_COURSE, mCourses.get(holder.getAdapterPosition()));
                intent.putExtra(Constants.EXTRA_COURSE_POSITION_IN_LIST, mCourses.indexOf(mCourses.get(position)));
                List<Pair<View, String>> pairs = new ArrayList<>();
                // get status bar and navigation bar views and add them as shared elements
                // to prevent glitches
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    View decor = ((Activity) mContext).getWindow().getDecorView();
                    View statusBar = decor.findViewById(android.R.id.statusBarBackground);
                    View navigationBar = decor.findViewById(android.R.id.navigationBarBackground);
                    if (statusBar != null)
                        pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
                    if (navigationBar != null)
                        pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
                }
                // get the common element for the transition in this activity
                pairs.add(Pair.create(view.findViewById(R.id.course_cardview_image), mContext.getString(R.string.transition_header_image)));
                // create the transition animation
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, pairs.toArray(new Pair[pairs.size()]));
                // start the new activity
                ((Activity) mContext).startActivityForResult(intent, MainActivity.RC_COURSE_ACTIVITY, options.toBundle());
            }
        });

        // Set course title
        holder.courseName.setText(mCourses.get(position).getName());

        // Set course starting Date
        Cycle firstCycle = mCourses.get(position).getFirstCycle();
        if (firstCycle != null && firstCycle.getStartDate() != null) {
            holder.courseStartDateTime.setVisibility(View.VISIBLE);
            holder.courseStartDateTime
                    .setText(DateHelper.dateToString(firstCycle.getStartDate()));
        } else {
            holder.courseStartDateTime.setVisibility(View.GONE);
        }

        // Set course rating
        Rating.getAverageByColumnInBackground(Rating.class, mContext, false,
                DBConstants.COL_RATING, DBConstants.COL_COURSE_ID, mCourses.get(position).getCourseID(),
                new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        try {
                            if ((Float) data.get(0) > 0) {
                                holder.courseRatingText.setText(String.valueOf(data.get(0)).substring(0, 3));
                                holder.courseRating.setVisibility(View.VISIBLE);
                            } else {
                                holder.courseRating.setVisibility(View.INVISIBLE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String error) {

                    }
                });

        // Check if MOOC
        if (mCourses.get(holder.getAdapterPosition()).getIsMooc()) {
            holder.moocFlag.setVisibility(View.VISIBLE);
        } else {
            holder.moocFlag.setVisibility(View.GONE);
        }

        // Set course image
        if (mCourses.get(position).getImageUrl() != null) {
            Glide.with(mContext)
                    .load(mCourses.get(position).getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(holder.courseImage);
        }
    }

    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    public class CourseVH extends RecyclerView.ViewHolder {

        FrameLayout frameLayout;
        CardView cardView;
        ImageView courseImage;
        TextView moocFlag;
        TextView courseName;
        TextView courseStartDateTime;
        LinearLayout courseRating;
        TextView courseRatingText;

        public CourseVH(View itemView) {
            super(itemView);

            frameLayout = (FrameLayout) itemView.findViewById(R.id.course_cardview_mainFrame);
            cardView = (CardView) itemView.findViewById(R.id.course_cardview_widget);
            courseImage = (ImageView) itemView.findViewById(R.id.course_cardview_image);
            moocFlag = (TextView) itemView.findViewById(R.id.mooc_flag);
            courseName = (TextView) itemView.findViewById(R.id.course_cardview_name);
            courseStartDateTime = (TextView) itemView.findViewById(R.id.course_cardview_startDateTime);
            courseRating = (LinearLayout) itemView.findViewById(R.id.course_cardview_rating);
            courseRatingText = (TextView) itemView.findViewById(R.id.course_cardview_ratingText);

            if (mRecyclerLayoutType == LAYOUT_TYPE_GRID) {
                CardView.LayoutParams cardLayoutParams = (CardView.LayoutParams) cardView.getLayoutParams();
                cardLayoutParams.width = CardView.LayoutParams.MATCH_PARENT;
                cardLayoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, mContext.getResources().getDisplayMetrics());
                int dp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
                cardLayoutParams.setMargins(dp4, dp4 * 3, dp4, dp4);

                courseName.setMaxLines(2);
                courseName.setEllipsize(TextUtils.TruncateAt.END);
            }
        }
    }
}