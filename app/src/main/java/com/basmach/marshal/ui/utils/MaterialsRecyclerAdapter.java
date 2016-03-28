package com.basmach.marshal.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.basmach.marshal.R;
import com.basmach.marshal.entities.MaterialItem;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class MaterialsRecyclerAdapter extends RecyclerView.Adapter<MaterialsRecyclerAdapter.MaterialVH> {

    private Context mContext;
    private ArrayList<MaterialItem> mMaterials;
    private SharedPreferences mSharedPreferences;
    private Boolean mIsDataFiltered = false;

    public MaterialsRecyclerAdapter(Context activity, ArrayList<MaterialItem> materials) {
        this.mMaterials = materials;
        this.mContext = activity;

        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public Boolean getIsDataFiltered() {
        return mIsDataFiltered;
    }

    public void setIsDataFiltered(Boolean mIsDataFiltered) {
        this.mIsDataFiltered = mIsDataFiltered;
    }

    public void setItems(ArrayList<MaterialItem> items) {
        this.mMaterials = items;
        notifyDataSetChanged();
    }

    @Override
    public MaterialVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.material_cardview, null);
        return new MaterialVH(view);
    }

    @Override
    public void onBindViewHolder(final MaterialVH holder, final int position) {

        Log.i("FILTER: ON BIND --> ", String.valueOf(position) + " : " + mIsDataFiltered);

        final long[] mLastClickTime = {0};
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime[0] < 1000) {
                    return;
                }
                mLastClickTime[0] = SystemClock.elapsedRealtime();
                String url = mMaterials.get(position).getUrl();
                Boolean cct = mSharedPreferences.getBoolean("CCT", true);
                if (cct) {
                    new CustomTabsIntent.Builder()
                            .setToolbarColor(ContextCompat.getColor(mContext.getApplicationContext(), R.color.colorPrimary))
                            .setShowTitle(true)
                            .addDefaultShareMenuItem()
                            .setCloseButtonIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_arrow_back_wht))
                            .build()
                            .launchUrl((Activity) mContext, Uri.parse(url));
                } else {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (holder.tags.getVisibility() != View.VISIBLE) {
                    holder.tags.setVisibility(View.VISIBLE);
                    holder.tags.animate().alpha(1.0f);
                } else {
                    holder.tags.setVisibility(View.GONE);
                    holder.tags.animate().alpha(0.0f);
                }
                return true;
            }
        });

        holder.titleTextView.setText(mMaterials.get(position).getTitle());
        holder.descriptionTextView.setText(mMaterials.get(position).getDescription());
        holder.siteUrlTextView.setText(mMaterials.get(position).getCannonicalUrl());
        holder.progressBar.setVisibility(View.GONE);

        Picasso.with(mContext).load(mMaterials.get(position).getImageUrl())
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return mMaterials.size();
    }

    public void animateTo(ArrayList<MaterialItem> materilsList) {
        applyAndAnimateRemovals(materilsList);
        applyAndAnimateAdditions(materilsList);
        applyAndAnimateMovedItems(materilsList);
    }

    private void applyAndAnimateRemovals(ArrayList<MaterialItem> newItems) {
        for (int i = mMaterials.size() - 1; i >= 0; i--) {
            final MaterialItem item = mMaterials.get(i);
            if (!newItems.contains(item)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(ArrayList<MaterialItem> newItems) {
        for (int i = 0, count = newItems.size(); i < count; i++) {
            final MaterialItem item = newItems.get(i);
            if (!mMaterials.contains(item)) {
                addItem(i, item);
            }
        }
    }

    private void applyAndAnimateMovedItems(ArrayList<MaterialItem> newItems) {
        for (int toPosition = newItems.size() - 1; toPosition >= 0; toPosition--) {
            final MaterialItem item = newItems.get(toPosition);
            final int fromPosition = mMaterials.indexOf(item);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public MaterialItem removeItem(int position) {
        final MaterialItem item = mMaterials.remove(position);
        notifyItemRemoved(position);
        return item;
    }

    public void addItem(int position, MaterialItem item) {
        mMaterials.add(position, item);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final MaterialItem item = mMaterials.remove(fromPosition);
        mMaterials.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    public class MaterialVH extends RecyclerView.ViewHolder{

        CardView cardView;
        ImageView imageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView siteUrlTextView;
        ProgressBar progressBar;
        TextView tags;

        public MaterialVH(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.link_preview_cardView);
            imageView = (ImageView) itemView.findViewById(R.id.thumb);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
            descriptionTextView = (TextView) itemView.findViewById(R.id.description);
            siteUrlTextView = (TextView) itemView.findViewById(R.id.url);
            progressBar = (ProgressBar) itemView.findViewById(R.id.link_preview_progressBar);
            tags = (TextView) itemView.findViewById(R.id.tags);
        }
    }
}
