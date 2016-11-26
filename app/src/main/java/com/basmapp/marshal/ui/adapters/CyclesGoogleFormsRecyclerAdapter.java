package com.basmapp.marshal.ui.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.ui.CourseActivity;
import com.basmapp.marshal.ui.WebViewActivity;
import com.basmapp.marshal.util.DateHelper;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class CyclesGoogleFormsRecyclerAdapter extends RecyclerView.Adapter<CyclesGoogleFormsRecyclerAdapter.CycleVH> {

    private Context mContext;
    private ArrayList<Cycle> mCycles;

    public CyclesGoogleFormsRecyclerAdapter(Context context, ArrayList<Cycle> cycles) {
        this.mContext = context;
        this.mCycles = cycles;
    }

    @Override
    public CycleVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cycles_list_item, parent, false);
        return new CycleVH(view);
    }

    @Override
    public void onBindViewHolder(final CycleVH holder, int position) {
        try {
            final String cycleDates = mCycles.get(position).toDatesRangeString(mContext);
            holder.mDateTextView.setText(cycleDates);
            holder.mDateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, WebViewActivity.class);
                    intent.putExtra(Constants.EXTRA_WEB_VIEW_TITLE, cycleDates);
                    intent.putExtra(Constants.EXTRA_WEB_VIEW_URL, mCycles.get(holder.getAdapterPosition()).getGoogleFormUrl());
                    mContext.startActivity(intent);
                }
            });

            if (mCycles.get(position).isRunningNow()) {
                holder.mDateTextView.setTypeface(Typeface.DEFAULT_BOLD);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.mDateTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimary, mContext.getTheme()));
                } else {
                    holder.mDateTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mCycles.size();
    }


    class CycleVH extends RecyclerView.ViewHolder {

        TextView mDateTextView;
        LinearLayout mMainLayout;

        CycleVH(View itemView) {
            super(itemView);

            mMainLayout = (LinearLayout) itemView.findViewById(R.id.cycles_list_item_linearLayout_mainLayout);
            mDateTextView = (TextView) itemView.findViewById(R.id.cycles_list_item_textView);
        }
    }
}
