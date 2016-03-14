package com.basmach.marshal.ui.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Cycle;

import java.util.ArrayList;

/**
 * Created by Ido on 3/14/2016.
 */
public class CyclesRecyclerAdapter extends RecyclerView.Adapter<CyclesRecyclerAdapter.CycleVH>{

    private Context mContext;
    private ArrayList<Cycle> mCycles;

    public CyclesRecyclerAdapter(Context context, ArrayList<Cycle> cycles) {
        this.mContext = context;
        this.mCycles = cycles;
    }
    @Override
    public CycleVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cycles_list_item, parent, false);
        return new CycleVH(view);
    }

    @Override
    public void onBindViewHolder(CycleVH holder, int position) {
        holder.mStartDateTextView.setText(DateHelper.dateToString(mCycles.get(position).getStartDate()));
        holder.mEndDateTextView.setText(DateHelper.dateToString(mCycles.get(position).getEndDate()));
    }

    @Override
    public int getItemCount() {
        return mCycles.size();
    }


    public class CycleVH extends RecyclerView.ViewHolder {

        TextView mStartDateTextView;
        TextView mEndDateTextView;

        public CycleVH(View itemView) {
            super(itemView);

            mStartDateTextView = (TextView) itemView.findViewById(R.id.cycles_list_item_textView_startDate);
            mEndDateTextView = (TextView) itemView.findViewById(R.id.cycles_list_item_textView_endDate);
        }
    }
}
