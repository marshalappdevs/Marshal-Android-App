package com.basmach.marshal.ui.adapters;

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

import com.basmach.marshal.Constants;
import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.entities.Rating;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.ui.CourseActivity;
import com.basmach.marshal.ui.MainActivity;
import com.basmach.marshal.utils.DateHelper;
import com.bumptech.glide.Glide;

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
                intent.putExtra(Constants.EXTRA_COURSE, mCourses.get(position));
                List<Pair<View, String>> pairs = new ArrayList<>();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    View decor = ((Activity)mContext).getWindow().getDecorView();
                    View statusBar = decor.findViewById(android.R.id.statusBarBackground);
                    View navigationBar = decor.findViewById(android.R.id.navigationBarBackground);
                    if (statusBar != null) pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
                    if (navigationBar != null) pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
                }
                pairs.add(Pair.create(view.findViewById(R.id.course_searchable_imageView), mContext.getString(R.string.transition_header_image)));
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, pairs.toArray(new Pair[pairs.size()]));
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
        holder.courseRating.setVisibility(View.GONE);
        holder.starIcon.setVisibility(View.GONE);

        Rating.getAverageByColumnInBackground(Rating.class, mContext, false,
                DBConstants.COL_RATING, DBConstants.COL_COURSE_CODE, mCourses.get(position).getCourseCode(),
                new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        try {
                            if ((Float) data.get(0) > 0) {
                                holder.starIcon.setVisibility(View.VISIBLE);
                                holder.courseRating.setVisibility(View.VISIBLE);
                                holder.courseRating.setText(String.valueOf(data.get(0)).substring(0,3));
                            } else {
                                holder.courseRating.setVisibility(View.GONE);
                                holder.starIcon.setVisibility(View.GONE);
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
        if(mCourses.get(holder.getAdapterPosition()).getIsMooc()){
            // if (holder.courseImage.getVisibility() == View.VISIBLE)
            holder.moocFlag.setVisibility(View.VISIBLE);
        } else {
            holder.moocFlag.setVisibility(View.GONE);
        }

        // Set course image
        if (mCourses.get(position).getImageUrl() != null) {
            Glide.with(mContext).load(mCourses.get(position).getImageUrl()).into(holder.courseImage);
        }

        String category = mCourses.get(position).getCategory();

        if (category != null) {
            holder.courseCategory.setVisibility(View.VISIBLE);

            switch (category) {
                case Course.CATEGORY_SOFTWARE:
                    holder.courseCategory.setText(mContext.getResources().getString(R.string.course_type_software));
                    break;
                case Course.CATEGORY_CYBER:
                    holder.courseCategory.setText(mContext.getResources().getString(R.string.course_type_cyber));
                    break;
                case Course.CATEGORY_IT:
                    holder.courseCategory.setText(mContext.getResources().getString(R.string.course_type_it));
                    break;
                case Course.CATEGORY_SYSTEM:
                    holder.courseCategory.setText(mContext.getResources().getString(R.string.course_type_system));
                    break;
                case Course.CATEGORY_TOOLS:
                    holder.courseCategory.setText(mContext.getResources().getString(R.string.course_type_tools));
                    break;
                default:
                    holder.courseCategory.setVisibility(View.GONE);
            }
        } else {
            holder.courseCategory.setVisibility(View.GONE);
        }
    }

    private Cycle getFirstCycle(ArrayList<Cycle> cycles) {
        Cycle firstCycle = cycles.get(0);

        for (Cycle cycle : cycles) {
            try {
                if (firstCycle.getStartDate().compareTo(cycle.getStartDate()) > 0) {
                    firstCycle = cycle;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return firstCycle;
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
        if (position - mCourses.size() > 0)
            position = mCourses.size();

        mCourses.add(position, item);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Course item = mCourses.remove(fromPosition);

        if (toPosition - mCourses.size() > 0)
            toPosition = mCourses.size();

        mCourses.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    public class CourseVH extends RecyclerView.ViewHolder{

        CardView cardView;
        ImageView courseImage;
        ImageView moocFlag;
        TextView courseName;
        TextView courseStartDateTime;
        TextView courseRating;
        TextView courseCategory;
        ImageView starIcon;

        public CourseVH(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.course_searchable_cardView) ;
            courseImage = (ImageView) itemView.findViewById(R.id.course_searchable_imageView);
            moocFlag = (ImageView) itemView.findViewById(R.id.course_searchable_moocFlag);
            courseName = (TextView) itemView.findViewById(R.id.course_searchable_title);
            courseStartDateTime = (TextView) itemView.findViewById(R.id.course_searchable_subTitle);
            courseRating = (TextView) itemView.findViewById(R.id.course_searchable_rating);
            starIcon = (ImageView) itemView.findViewById(R.id.course_searchable_ratingIcon);
            courseCategory = (TextView) itemView.findViewById(R.id.course_searchable_category);
        }
    }
}
