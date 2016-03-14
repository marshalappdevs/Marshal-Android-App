package com.basmach.marshal.ui.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

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
        return null;
    }

    @Override
    public void onBindViewHolder(CycleVH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class CycleVH extends RecyclerView.ViewHolder {

        public CycleVH(View itemView) {
            super(itemView);
        }
    }
}
