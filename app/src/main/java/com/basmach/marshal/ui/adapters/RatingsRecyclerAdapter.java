package com.basmach.marshal.ui.adapters;

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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.entities.Rating;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.ui.CourseActivity;
import com.basmach.marshal.utils.DateHelper;
import com.leocardz.link.preview.library.TextCrawler;
import com.squareup.picasso.Callback;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class RatingsRecyclerAdapter extends RecyclerView.Adapter<RatingsRecyclerAdapter.RatingVH> {

    private Context mContext;
    private ArrayList<Rating> mRatings;

    public RatingsRecyclerAdapter(Context context, ArrayList<Rating> ratings) {
        this.mRatings = ratings;
        this.mContext = context;
    }

    @Override
    public RatingVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rating_list_item, null);
        return new RatingVH(view);
    }

    @Override
    public void onBindViewHolder(RatingVH holder, int position) {
        holder.ratingBar.setRating((float) mRatings.get(position).getRating());
        holder.textViewLastModifiedDate.setText(DateHelper.dateToString(mRatings.get(position).getLastModified()));
        holder.textViewComment.setText(mRatings.get(position).getComment());
    }

    @Override
    public int getItemCount() {
        return mRatings.size();
    }

    public class RatingVH extends RecyclerView.ViewHolder {

        TextView textViewComment;
        TextView textViewLastModifiedDate;
        RatingBar ratingBar;

        public RatingVH(View itemView) {
            super(itemView);

            textViewComment = (TextView) itemView.findViewById(R.id.rating_list_item_textView_comment);
            textViewLastModifiedDate = (TextView) itemView.findViewById(R.id.rating_list_item_textView_lastModifiedDate);
            ratingBar = (RatingBar) itemView.findViewById(R.id.rating_list_item_rating_bar);
        }
    }
}