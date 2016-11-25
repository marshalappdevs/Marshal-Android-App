package com.basmapp.marshal.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.MaterialItem;
import com.basmapp.marshal.interfaces.OnHashTagClickListener;
import com.basmapp.marshal.util.HashTag;
import com.basmapp.marshal.util.ThemeUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaterialsRecyclerAdapter extends RecyclerView.Adapter<MaterialsRecyclerAdapter.MaterialVH> {

    private Context mContext;
    private ArrayList<MaterialItem> mMaterials;
    private SharedPreferences mSharedPreferences;
    private Boolean mIsDataFiltered = false;
    private OnHashTagClickListener hashTagClickListener;

    public MaterialsRecyclerAdapter(Context activity, ArrayList<MaterialItem> materials, OnHashTagClickListener hashtagClickListener) {
        this.mMaterials = materials;
        this.mContext = activity;
        this.hashTagClickListener = hashtagClickListener;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.material_cardview, parent, false);
        return new MaterialVH(view);
    }

    @Override
    public void onBindViewHolder(final MaterialVH holder, int position) {
        final long[] mLastClickTime = {0};
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime[0] < 1000) {
                    return;
                }
                mLastClickTime[0] = SystemClock.elapsedRealtime();
                String url = mMaterials.get(holder.getAdapterPosition()).getUrl();
                if (mSharedPreferences.getBoolean(Constants.PREF_CCT, true)) {
                    new CustomTabsIntent.Builder()
                            .setToolbarColor(ThemeUtils.getThemeColor(mContext, R.attr.colorPrimary))
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

        if (mMaterials.get(position).getTitle() != null) {
            holder.titleTextView.setText(mMaterials.get(position).getTitle());
        }
        if (mMaterials.get(position).getDescription() != null) {
            holder.descriptionTextView.setText(mMaterials.get(position).getDescription());
        } else {
            holder.descriptionTextView.setText(mMaterials.get(position).getUrl());
        }
        if (mMaterials.get(position).getBaseUrl() != null) {
            holder.siteUrlTextView.setText(mMaterials.get(position).getBaseUrl());
        }
        if (mMaterials.get(position).getImageUrl() != null) {
            Glide.with(mContext).load(mMaterials.get(position).getImageUrl())
                    .error(R.drawable.link_image_error)
                    .placeholder(R.drawable.link_image_placeholder)
                    .into(holder.imageView);
        }

        // Set HashTags
        if (mMaterials.get(position).getTags() != null) {
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

            holder.tags.setMovementMethod(LinkMovementMethod.getInstance());
            holder.tags.setText(holder.getHashTagsSpannableString(mMaterials.get(position).getTags()));
        } else {
            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext, R.string.no_hashtags, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        holder.progressBar.setVisibility(View.GONE);
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
        notifyItemChanged(toPosition);
    }

    public class MaterialVH extends RecyclerView.ViewHolder {

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

        private ArrayList<int[]> getSpans(String body, char prefix) {
            ArrayList<int[]> spans = new ArrayList<>();

            Pattern pattern = Pattern.compile(prefix + "\\w+");
            Matcher matcher = pattern.matcher(body);

            // Check all occurrences
            while (matcher.find()) {
                int[] currentSpan = new int[2];
                currentSpan[0] = matcher.start();
                currentSpan[1] = matcher.end();
                spans.add(currentSpan);
            }

            return spans;
        }

        private void setSpanTag(SpannableString tagsContent, ArrayList<int[]> hashTagSpans) {
            for (int i = 0; i < hashTagSpans.size(); i++) {
                int[] span = hashTagSpans.get(i);
                int hashTagStart = span[0];
                int hashTagEnd = span[1];

                tagsContent.setSpan(new HashTag(mContext, hashTagClickListener),
                        hashTagStart,
                        hashTagEnd, 0);
            }
        }

        public SpannableString getHashTagsSpannableString(String tags) {
            ArrayList<int[]> hashTagsSpans = getSpans(tags, '#');
            SpannableString spannableString = new SpannableString(tags);
            setSpanTag(spannableString, hashTagsSpans);

            return spannableString;
        }
    }
}
