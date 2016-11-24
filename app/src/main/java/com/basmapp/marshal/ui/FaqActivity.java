package com.basmapp.marshal.ui;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.FaqItem;
import com.basmapp.marshal.interfaces.ContentProviderCallBack;
import com.basmapp.marshal.localdb.DBObject;
import com.basmapp.marshal.ui.adapters.FaqRecyclerAdapter;
import com.basmapp.marshal.util.ContentProvider;
import com.basmapp.marshal.util.LocaleUtils;

import java.util.ArrayList;

public class FaqActivity extends BaseActivity {
    private ProgressBar mProgressBar;
    private RecyclerView mRecycler;
    private LinearLayoutManager mLayoutManager;
    private FaqRecyclerAdapter mAdapter;
    private ArrayList<FaqItem> mFaqList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_faq);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_faq);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.faq_progressBar);
        mRecycler = (RecyclerView) findViewById(R.id.faq_recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        if (mAdapter != null && mRecycler != null) {
            mRecycler.setAdapter(mAdapter);
        }

        if (mFaqList == null) {
            ContentProvider.getInstance().getFaqItems(this, new ContentProviderCallBack() {
                @Override
                public void onDataReady(ArrayList<? extends DBObject> data, Object extra) {
                    mFaqList = (ArrayList<FaqItem>) data;
                    setProgressBarVisibility(View.GONE);
                    showData();
                }

                @Override
                public void onError(Exception e) {
                    setProgressBarVisibility(View.GONE);
                }
            });
        }
    }

    private void setProgressBarVisibility(int visibility) {
        mProgressBar.setVisibility(visibility);
    }

    private void showData() {
        if (mAdapter == null)
            mAdapter = new FaqRecyclerAdapter(this, mFaqList);
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            if (LocaleUtils.isRtl(getResources())) {
                overridePendingTransition(R.anim.activity_close_enter,
                        R.anim.activity_close_exit_rtl);
            } else {
                overridePendingTransition(R.anim.activity_close_enter,
                        R.anim.activity_close_exit);
            }
        }
    }
}
