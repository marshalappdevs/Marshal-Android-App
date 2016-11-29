package com.basmapp.marshal.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.FaqItem;
import com.basmapp.marshal.util.AuthUtil;
import com.basmapp.marshal.util.MarshalServiceProvider;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FaqRecyclerAdapter extends RecyclerView.Adapter<FaqRecyclerAdapter.FaqVH> {

    private Context mContext;
    private ArrayList<FaqItem> mFaq;
    private SharedPreferences mSharedPreferences;
    private Boolean mIsDataFiltered = false;
    private final HashSet<MapView> mMaps = new HashSet<MapView>();

    public FaqRecyclerAdapter(Context activity, ArrayList<FaqItem> faq) {
        this.mFaq = faq;
        this.mContext = activity;
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public Boolean getIsDataFiltered() {
        return mIsDataFiltered;
    }

    public void setIsDataFiltered(Boolean mIsDataFiltered) {
        this.mIsDataFiltered = mIsDataFiltered;
    }

    public void setItems(ArrayList<FaqItem> items) {
        this.mFaq = items;
        notifyDataSetChanged();
    }

    @Override
    public FaqVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.faq_card_view, parent, false);
        return new FaqVH(view);
    }

    @Override
    public void onBindViewHolder(final FaqVH holder, int position) {
        holder.questionContainer.setOnClickListener(new View.OnClickListener() {
            boolean answerExpanded = false;

            @Override
            public void onClick(View view) {
                answerExpanded = !answerExpanded;
                ViewCompat.animate(holder.expandAnswerArrow).rotation(
                        answerExpanded ? 180 : 0).start();
                if (answerExpanded) {
                    holder.answerTextView.setVisibility(mFaq.get(holder.getAdapterPosition())
                            .getAnswer() != null ? View.VISIBLE : View.GONE);
                    holder.answerLink.setVisibility(mFaq.get(holder.getAdapterPosition())
                            .getAnswerLink() != null ? View.VISIBLE : View.GONE);
                    holder.answerImageView.setVisibility(mFaq.get(holder.getAdapterPosition())
                            .getAnswerImageUrl() != null ? View.VISIBLE : View.GONE);
                    if (holder.mapView != null && mFaq.get(holder.getAdapterPosition()).getAnswerAddress() != null) {
                        holder.mapView.onCreate(null);
                        holder.mapView.onResume();
                        holder.mapView.setVisibility(View.VISIBLE);
                        holder.mapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                googleMap.clear();
                                LatLng coordinates = getCoordinatesFromAddress(mContext,
                                        mFaq.get(holder.getAdapterPosition()).getAnswerAddress());
                                if (coordinates != null) {
                                    googleMap.addMarker(new MarkerOptions().position(coordinates));
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 13f));
                                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                }
                            }
                        });
                        // Keep track of MapView
                        mMaps.add(holder.mapView);
                    }
                    holder.answerMoreMenu.setVisibility(mFaq.get(holder.getAdapterPosition())
                            .getAnswerPhoneNumber() != null ? View.VISIBLE : View.GONE);
                    holder.faqForm.setVisibility(mFaq.get(holder.getAdapterPosition())
                            .getIsRated() ? View.GONE : View.VISIBLE);
                } else {
                    holder.answerTextView.setVisibility(View.GONE);
                    holder.answerLink.setVisibility(View.GONE);
                    holder.answerImageView.setVisibility(View.GONE);
                    holder.mapView.setVisibility(View.GONE);
                    holder.answerMoreMenu.setVisibility(View.GONE);
                    holder.faqForm.setVisibility(View.GONE);
                }
            }
        });

        if (mFaq.get(position).getQuestion() != null) {
            holder.questionTextView.setText(mFaq.get(position).getQuestion());
        }
        if (mFaq.get(position).getAnswer() != null) {
            holder.answerTextView.setText(mFaq.get(position).getAnswer());
        }
        if (mFaq.get(position).getAnswerImageUrl() != null) {
            Glide.with(mContext)
                    .load(mFaq.get(position).getAnswerImageUrl())
                    .placeholder(R.drawable.inline_image_placeholder)
                    .into(holder.answerImageView);
        }
        if (mFaq.get(position).getAnswerLink() != null) {
            holder.answerLink.setText(mFaq.get(position).getAnswerLink());
        }

        if (mFaq.get(position).getAnswerPhoneNumber() != null) {
            holder.answerMoreMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(mContext, view, Gravity.START);
                    MenuInflater menuInflater = popupMenu.getMenuInflater();
                    menuInflater.inflate(R.menu.faq_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.faq_more:
                                    mContext.startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse(
                                            "tel:" + mFaq.get(holder.getAdapterPosition()).getAnswerPhoneNumber())));
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        holder.faqFormPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendFaqIsUsefulRequest("useful", mFaq.get(holder.getAdapterPosition()), holder.faqForm).execute();
            }
        });

        holder.faqFormNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendFaqIsUsefulRequest("unuseful", mFaq.get(holder.getAdapterPosition()), holder.faqForm).execute();
            }
        });

        holder.progressBar.setVisibility(View.GONE);
    }

    public LatLng getCoordinatesFromAddress(Context context, String addressName) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> address;
        LatLng latLng = null;
        try {
            address = geocoder.getFromLocationName(addressName, 1);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latLng;
    }

    @Override
    public void onViewRecycled(FaqVH holder) {
        super.onViewRecycled(holder);
        if (holder != null && holder.map != null) {
            // Clear the map and free up resources by changing the map type to none
            holder.map.clear();
            holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
    }

    @Override
    public int getItemCount() {
        return mFaq.size();
    }

    public HashSet<MapView> getMaps() {
        return mMaps;
    }

    public void animateTo(ArrayList<FaqItem> faqList) {
        applyAndAnimateRemovals(faqList);
        applyAndAnimateAdditions(faqList);
        applyAndAnimateMovedItems(faqList);
    }

    private void applyAndAnimateRemovals(ArrayList<FaqItem> newItems) {
        for (int i = mFaq.size() - 1; i >= 0; i--) {
            final FaqItem item = mFaq.get(i);
            if (!newItems.contains(item)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(ArrayList<FaqItem> newItems) {
        for (int i = 0, count = newItems.size(); i < count; i++) {
            final FaqItem item = newItems.get(i);
            if (!mFaq.contains(item)) {
                addItem(i, item);
            }
        }
    }

    private void applyAndAnimateMovedItems(ArrayList<FaqItem> newItems) {
        for (int toPosition = newItems.size() - 1; toPosition >= 0; toPosition--) {
            final FaqItem item = newItems.get(toPosition);
            final int fromPosition = mFaq.indexOf(item);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public FaqItem removeItem(int position) {
        final FaqItem item = mFaq.remove(position);
        notifyItemRemoved(position);
        return item;
    }

    public void addItem(int position, FaqItem item) {
        mFaq.add(position, item);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final FaqItem item = mFaq.remove(fromPosition);
        mFaq.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
        notifyItemChanged(toPosition);
    }

    public class FaqVH extends RecyclerView.ViewHolder {

        CardView cardView;
        LinearLayout questionContainer;
        TextView questionTextView, answerTextView, answerLink;
        ImageButton expandAnswerArrow;
        ImageView answerImageView, answerMoreMenu;
        LinearLayout faqForm;
        Button faqFormPositive;
        Button faqFormNegative;
        ProgressBar progressBar;
        MapView mapView;
        GoogleMap map;

        public FaqVH(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.faq_card);
            questionContainer = (LinearLayout) itemView.findViewById(R.id.faq_question_container);
            questionTextView = (TextView) itemView.findViewById(R.id.faq_question);
            expandAnswerArrow = (ImageButton) itemView.findViewById(R.id.faq_expand_arrow);
            answerTextView = (TextView) itemView.findViewById(R.id.faq_answer_text);
            answerLink = (TextView) itemView.findViewById(R.id.faq_answer_link);
            answerImageView = (ImageView) itemView.findViewById(R.id.faq_answer_image);
            answerMoreMenu = (ImageView) itemView.findViewById(R.id.li_overflow);
            faqForm = (LinearLayout) itemView.findViewById(R.id.faq_form);
            faqFormPositive = (Button) itemView.findViewById(R.id.faq_helpful_positive);
            faqFormNegative = (Button) itemView.findViewById(R.id.faq_helpful_negative);
            progressBar = (ProgressBar) itemView.findViewById(R.id.faq_progressBar);
            mapView = (MapView) itemView.findViewById(R.id.lite_recycler_view_map);
        }
    }

    private class SendFaqIsUsefulRequest extends AsyncTask<Void, Void, Boolean> {
        private String response;
        private FaqItem faqItem;
        private LinearLayout faqFormContainer;

        SendFaqIsUsefulRequest(String respond, FaqItem faqItem, LinearLayout faqFormContainer) {
            this.response = respond;
            this.faqItem = faqItem;
            this.faqFormContainer = faqFormContainer;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String apiToken = AuthUtil.getApiToken();
                if (MarshalServiceProvider.getInstance(apiToken).
                        postIsUseful(response, faqItem).execute().isSuccessful()) {
                    faqItem.setIsRated(true);
                    faqItem.save();
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Toast.makeText(mContext, R.string.faq_helpful_complete_toast, Toast.LENGTH_LONG).show();
                faqFormContainer.setVisibility(View.GONE);
            } else {
                Toast.makeText(mContext, R.string.faq_helpful_failed_toast, Toast.LENGTH_LONG).show();
            }
        }
    }
}
