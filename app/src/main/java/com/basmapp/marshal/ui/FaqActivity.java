package com.basmapp.marshal.ui;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.R;
import com.basmapp.marshal.util.LocaleUtils;

public class FaqActivity extends BaseActivity {
    private boolean answerExpanded = false;

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

        findViewById(R.id.faq_question_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answerExpanded = !answerExpanded;
                ViewCompat.animate(findViewById(R.id.faq_expand_arrow)).rotation(
                        answerExpanded ? 180 : 0).start();
                findViewById(R.id.faq_answer_text).setVisibility(
                        answerExpanded ? View.VISIBLE : View.GONE);
                findViewById(R.id.faq_answer_image).setVisibility(
                        answerExpanded ? View.VISIBLE : View.GONE);
//                findViewById(R.id.faq_form).setVisibility(
//                        answerExpanded ? View.VISIBLE : View.GONE);
            }
        });
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
