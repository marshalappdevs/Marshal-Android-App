package com.basmapp.marshal.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Rating;
import com.basmapp.marshal.util.DateHelper;

import java.util.List;

public class RatingsRecyclerAdapter extends RecyclerView.Adapter<RatingsRecyclerAdapter.RatingVH> {

    private Context mContext;
    private List<Rating> mRatings;

    public RatingsRecyclerAdapter(Context context, List<Rating> ratings) {
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