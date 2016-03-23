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
    private int lastPosition = -1;
    private SharedPreferences mSharedPreferences;

    public MaterialsRecyclerAdapter(Context activity, ArrayList<MaterialItem> materials) {
        this.mMaterials = materials;
        this.mContext = activity;

        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public MaterialVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.material_cardview, null);
        return new MaterialVH(view);
    }

    @Override
    public void onBindViewHolder(final MaterialVH holder, final int position) {

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

//        if(mMaterials.get(position).getLinkContent() == null) {
//            holder.linkPreviewUtil.getData(mMaterials.get(position).getUrl(), position, new com.basmach.marshal.interfaces.LinkPreviewCallback() {
//                @Override
//                public void onFailure() {
//
//                }
//
//                @Override
//                public void onSuccess(LinkContent linkContent, final int itemPosition) {
//                    mMaterials.get(itemPosition).setLinkContent(linkContent);
//
//                    ((Activity)mContext).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            holder.showContent(mMaterials.get(itemPosition).getLinkContent());
//                        }
//                    });
//                }
//            });
//        } else {
//            holder.showContent(mMaterials.get(position).getLinkContent());
//        }
//        if (mMaterials.get(position).getSourceContent() == null) {
//            holder.textCrawler.makePreview(new LinkPreviewCallback() {
//                @Override
//                public void onPre() {
//
//                }
//
//                @Override
//                public void onPos(SourceContent sourceContent, boolean b) {
//
//                    mMaterials.get(position).setSourceContent(sourceContent);
//
//                    holder.showContent(mMaterials.get(position).getSourceContent());
//                    //                setAnimation(holder.cardView, position);
//                }
//            }, mMaterials.get(position).getUrl());
//        } else {
//            holder.showContent(mMaterials.get(position).getSourceContent());
//        }
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

//    @Override
//    public void onViewDetachedFromWindow(MaterialVH holder) {
//        super.onViewDetachedFromWindow(holder);
//
//        holder.clearAppearAnimation();
//    }

    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public class MaterialVH extends RecyclerView.ViewHolder{

        CardView cardView;
        ImageView imageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView siteUrlTextView;
        ProgressBar progressBar;
        TextView tags;

//        LinkPreviewUtil linkPreviewUtil;
        TextCrawler textCrawler;

        public MaterialVH(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.link_preview_cardView);
            imageView = (ImageView) itemView.findViewById(R.id.thumb);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
            descriptionTextView = (TextView) itemView.findViewById(R.id.description);
            siteUrlTextView = (TextView) itemView.findViewById(R.id.url);
            progressBar = (ProgressBar) itemView.findViewById(R.id.link_preview_progressBar);
            tags = (TextView) itemView.findViewById(R.id.tags);

//            linkPreviewUtil = new LinkPreviewUtil(mContext);
            textCrawler = new TextCrawler();

            Log.i("MATERIAL_RECYCLER", "CTOR");
        }

        public void clearAppearAnimation() {
            cardView.clearAnimation();
        }

        public void showContent(SourceContent sourceContent) {
            progressBar.setVisibility(View.GONE);

            titleTextView.setText(sourceContent.getTitle());
            descriptionTextView.setText(sourceContent.getDescription());
            siteUrlTextView.setText(sourceContent.getCannonicalUrl());

            if (sourceContent.getImages() != null &&
                    sourceContent.getImages().size() > 0) {
                Picasso.with(mContext).load(sourceContent.getImages().get(0))
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }

            cardView.setVisibility(View.VISIBLE);
        }

//        public void showContent(LinkContent linkContent) {
//            progressBar.setVisibility(View.GONE);
//
//            titleTextView.setText(linkContent.getTitle());
//            descriptionTextView.setText(linkContent.getDescription());
//            siteUrlTextView.setText(linkContent.getSiteName());
//
//            Picasso.with(mContext).load(linkContent.getImageUrl())
//                    .into(imageView, new Callback() {
//                        @Override
//                        public void onSuccess() {
//                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                        }
//
//                        @Override
//                        public void onError() {
//
//                        }
//                    });
//
//            cardView.setVisibility(View.VISIBLE);
//        }
    }
}
