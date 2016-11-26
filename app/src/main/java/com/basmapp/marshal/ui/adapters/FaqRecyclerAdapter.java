package com.basmapp.marshal.ui.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FaqRecyclerAdapter extends RecyclerView.Adapter<FaqRecyclerAdapter.FaqVH> {

    private Context mContext;
    private ArrayList<FaqItem> mFaq;
    private SharedPreferences mSharedPreferences;
    private Boolean mIsDataFiltered = false;

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
                holder.answerTextView.setVisibility(
                        answerExpanded ? View.VISIBLE : View.GONE);
                holder.answerImageView.setVisibility(
                        answerExpanded ? View.VISIBLE : View.GONE);
                holder.faqForm.setVisibility(
                        answerExpanded ? View.VISIBLE : View.GONE);
            }
        });

        if (mFaq.get(position).getQuestion() != null) {
            holder.questionTextView.setText(mFaq.get(position).getQuestion());
        }
        if (mFaq.get(position).getAnswer() != null) {
            holder.answerTextView.setText(mFaq.get(position).getAnswer());
        }
        if (mFaq.get(position).getAnswerImageUrl() != null) {
            Glide.with(mContext).load(mFaq.get(position).getAnswerImageUrl()).into(holder.answerImageView);
        }

        holder.faqFormPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, R.string.faq_helpful_complete_toast, Toast.LENGTH_LONG).show();
                holder.faqForm.setVisibility(View.GONE);
                // TODO: send feedback to server and save in local db
            }
        });

        holder.faqFormNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, R.string.faq_helpful_complete_toast, Toast.LENGTH_LONG).show();
                holder.faqForm.setVisibility(View.GONE);
                // TODO: send feedback to server and save in local db
            }
        });

        holder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mFaq.size();
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
        TextView questionIndexTextView;
        TextView questionTextView;
        ImageButton expandAnswerArrow;
        TextView answerTextView;
        ImageView answerImageView;
        LinearLayout faqForm;
        Button faqFormPositive;
        Button faqFormNegative;
        ProgressBar progressBar;

        public FaqVH(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.faq_card);
            questionContainer = (LinearLayout) itemView.findViewById(R.id.faq_question_container);
            questionIndexTextView = (TextView) itemView.findViewById(R.id.faq_index_number);
            questionTextView = (TextView) itemView.findViewById(R.id.faq_question);
            expandAnswerArrow = (ImageButton) itemView.findViewById(R.id.faq_expand_arrow);
            answerTextView = (TextView) itemView.findViewById(R.id.faq_answer_text);
            answerImageView = (ImageView) itemView.findViewById(R.id.faq_answer_image);
            faqForm = (LinearLayout) itemView.findViewById(R.id.faq_form);
            faqFormPositive = (Button) itemView.findViewById(R.id.faq_helpful_positive);
            faqFormNegative = (Button) itemView.findViewById(R.id.faq_helpful_negative);
            progressBar = (ProgressBar) itemView.findViewById(R.id.faq_progressBar);
        }
    }
}
