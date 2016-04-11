package com.basmach.marshal.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
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

public class CoursesSearchRecyclerAdapter extends RecyclerView.Adapter<CoursesSearchRecyclerAdapter.CourseVH> {

    private Context mContext;
    private ArrayList<Course> mCourses;

    public CoursesSearchRecyclerAdapter(Context context, ArrayList<Course> courses) {
        this.mCourses = courses;
        this.mContext = context;
    }

    @Override
    public CourseVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_cardview_searchable, parent, false);
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
                intent.putExtra(CourseActivity.EXTRA_COURSE, mCourses.get(position));
                List<Pair<View, String>> pairs = new ArrayList<>();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    View decor = ((Activity)mContext).getWindow().getDecorView();
                    View statusBar = decor.findViewById(android.R.id.statusBarBackground);
                    View navigationBar = decor.findViewById(android.R.id.navigationBarBackground);
                    if (statusBar != null) pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
                    if (navigationBar != null) pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
                }
                pairs.add(Pair.create(view.findViewById(R.id.course_searchable_imageView), "course_image"));
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
        } else {
            holder.courseStartDateTime.setVisibility(View.GONE);
        }

        // Check if MOOC
        if(mCourses.get(position).getIsMooc()){
            // if (holder.courseImage.getVisibility() == View.VISIBLE)
            holder.moocFlag.setVisibility(View.VISIBLE);
        } else {
            holder.moocFlag.setVisibility(View.GONE);
        }

        // Set course image
        mCourses.get(position).getPhotoViaPicasso(mContext, holder.courseImage,  new Callback() {
            @Override public void onSuccess() {
                holder.courseImage.setVisibility(View.VISIBLE);
            }

            @Override public void onError() {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    public void animateTo(ArrayList<Course> coursesList) {
        applyAndAnimateRemovals(coursesList);
        applyAndAnimateAdditions(coursesList);
        applyAndAnimateMovedItems(coursesList);
        for(int i = 0; i < mCourses.size(); i++) {
            notifyItemChanged(i);
        }
    }

    private void applyAndAnimateRemovals(ArrayList<Course> newItems) {
        for (int i = mCourses.size() - 1; i >= 0; i--) {
            final Course item = mCourses.get(i);
            if (!newItems.contains(item)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(ArrayList<Course> newItems) {
        for (int i = 0, count = newItems.size(); i < count; i++) {
            final Course item = newItems.get(i);
            if (!mCourses.contains(item)) {
                addItem(i, item);
            }
        }
    }

    private void applyAndAnimateMovedItems(ArrayList<Course> newItems) {
        for (int toPosition = newItems.size() - 1; toPosition >= 0; toPosition--) {
            final Course item = newItems.get(toPosition);
            final int fromPosition = mCourses.indexOf(item);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public Course removeItem(int position) {
        final Course item = mCourses.remove(position);
        notifyItemRemoved(position);
        return item;
    }

    public void addItem(int position, Course item) {
        mCourses.add(position, item);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Course item = mCourses.remove(fromPosition);
        mCourses.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    public class CourseVH extends RecyclerView.ViewHolder{

        CardView cardView;
        ImageView courseImage;
        ImageView moocFlag;
        TextView courseName;
        TextView courseStartDateTime;

        public CourseVH(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.course_searchable_cardView) ;
            courseImage = (ImageView) itemView.findViewById(R.id.course_searchable_imageView);
            moocFlag = (ImageView) itemView.findViewById(R.id.course_searchable_moocFlag);
            courseName = (TextView) itemView.findViewById(R.id.course_searchable_title);
            courseStartDateTime = (TextView) itemView.findViewById(R.id.course_searchable_subTitle);
        }
    }
}
