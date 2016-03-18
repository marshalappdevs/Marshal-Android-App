package com.basmach.marshal.ui.utils;

import android.content.Context;
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

    public MaterialsRecyclerAdapter(Context context, ArrayList<MaterialItem> materials) {
        this.mMaterials = materials;
        this.mContext = context;
    }

    @Override
    public MaterialVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.material_cardview, null);
        return new MaterialVH(view);
    }

    @Override
    public void onBindViewHolder(final MaterialVH holder, final int position) {

        if (mMaterials.get(position).getSourceContent() == null) {
            holder.textCrawler.makePreview(new LinkPreviewCallback() {
                @Override
                public void onPre() {

                }

                @Override
                public void onPos(SourceContent sourceContent, boolean b) {

                    mMaterials.get(position).setSourceContent(sourceContent);

                    holder.showContent(sourceContent);
                    //                setAnimation(holder.cardView, position);
                }
            }, mMaterials.get(position).getUrl());
        } else {
            holder.showContent(mMaterials.get(position).getSourceContent());
        }
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

        TextCrawler textCrawler;

        public MaterialVH(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.link_preview_cardView);
            imageView = (ImageView) itemView.findViewById(R.id.thumb);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
            descriptionTextView = (TextView) itemView.findViewById(R.id.description);
            siteUrlTextView = (TextView) itemView.findViewById(R.id.url);
            progressBar = (ProgressBar) itemView.findViewById(R.id.link_preview_progressBar);

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

            cardView.setVisibility(View.VISIBLE);
        }
    }
}
