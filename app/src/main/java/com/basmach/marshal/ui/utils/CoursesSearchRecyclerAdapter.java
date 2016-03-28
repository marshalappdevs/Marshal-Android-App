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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.ui.CourseActivity;
import com.squareup.picasso.Callback;
import java.util.ArrayList;

public class CoursesSearchRecyclerAdapter extends RecyclerView.Adapter<CoursesSearchRecyclerAdapter.CourseVH> {

    private Context mContext;
    private ArrayList<Course> mCourses;

    public CoursesSearchRecyclerAdapter(Context context, ArrayList<Course> courses) {
        this.mCourses = courses;
        this.mContext = context;
    }

    @Override
    public CourseVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_cardview_searchable, null);
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
                Pair p1 = Pair.create(view.findViewById(R.id.course_cardview_searchable_imageView), "course_image");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, p1);
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

        // Set course image
        mCourses.get(position).getPhotoViaPicasso(mContext, holder.courseImage,  new Callback() {
            @Override public void onSuccess() {
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

    public void animateTo(ArrayList<Course> materilsList) {
        applyAndAnimateRemovals(materilsList);
        applyAndAnimateAdditions(materilsList);
        applyAndAnimateMovedItems(materilsList);
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

        public CourseVH(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.course_cardview_searchable_cardView) ;
            courseImage = (ImageView) itemView.findViewById(R.id.course_cardview_searchable_imageView);
            moocFlag = (ImageView) itemView.findViewById(R.id.course_cardview_searchable_moocFlag);
            courseName = (TextView) itemView.findViewById(R.id.course_cardview_searchable_textView);
        }
    }
}
