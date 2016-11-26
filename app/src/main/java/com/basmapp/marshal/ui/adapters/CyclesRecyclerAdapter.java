package com.basmapp.marshal.ui.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.provider.CalendarContract.Events;
import android.widget.Toast;

import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.util.DateHelper;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class CyclesRecyclerAdapter extends RecyclerView.Adapter<CyclesRecyclerAdapter.CycleVH> {

    private Course mCourse;
    private Context mContext;
    private ArrayList<Cycle> mCycles;

    public CyclesRecyclerAdapter(Context context, ArrayList<Cycle> cycles, Course course) {
        this.mContext = context;
        this.mCycles = cycles;
        this.mCourse = course;
    }

    @Override
    public CycleVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cycles_list_item, parent, false);
        return new CycleVH(view);
    }

    @Override
    public void onBindViewHolder(CycleVH holder, final int position) {
        try {
            String cycleDates = String.format(mContext.getString(R.string.course_cycle_format),
                    DateHelper.dateToString(mCycles.get(position).getStartDate()),
                    DateHelper.dateToString(mCycles.get(position).getEndDate()));
            holder.mDateTextView.setText(cycleDates);

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


    class CycleVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mDateTextView;
        LinearLayout mMainLayout;

        CycleVH(View itemView) {
            super(itemView);

            mMainLayout = (LinearLayout) itemView.findViewById(R.id.cycles_list_item_linearLayout_mainLayout);
            mDateTextView = (TextView) itemView.findViewById(R.id.cycles_list_item_textView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            new AlertDialog.Builder(mContext, R.style.Cycle_DialogAlert)
                    .setTitle(mContext.getString(R.string.cycle_alert_title))
                    .setMessage(R.string.cycle_alert_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                GregorianCalendar startDate = new GregorianCalendar();
                                startDate.setTime(mCycles.get(getAdapterPosition()).getStartDate());

                                GregorianCalendar endDate = new GregorianCalendar();
                                endDate.setTime(mCycles.get(getAdapterPosition()).getEndDate());
                                endDate.set(GregorianCalendar.DAY_OF_MONTH, ((endDate.get(GregorianCalendar.DAY_OF_MONTH)) + 1));

                                Intent calendarIntent = new Intent(Intent.ACTION_INSERT);
                                calendarIntent.setType("vnd.android.cursor.item/event");
                                calendarIntent.putExtra(Events.TITLE, mCourse.getName());
                                calendarIntent.putExtra(Events.DESCRIPTION, mCourse.getDescription());
                                calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                                calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate.getTimeInMillis());
                                calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate.getTimeInMillis());
                                mContext.startActivity(calendarIntent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(mContext,
                                        mContext.getString(R.string.course_cycle_item_onclick_error),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .show();
        }
    }
}
