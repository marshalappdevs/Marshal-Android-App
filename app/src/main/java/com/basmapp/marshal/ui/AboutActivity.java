package com.basmapp.marshal.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.BuildConfig;
import com.basmapp.marshal.R;
import com.basmapp.marshal.util.URLSpanNoUnderline;

public class AboutActivity extends BaseActivity {
    private int mTapCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_about);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
            }
        });

        findViewById(R.id.logo_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTapCount == 7) {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Easter Egg!!! " + ("\ud83d\udc83"), Snackbar.LENGTH_LONG);
                    TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snackbar.show();
                    mTapCount = 0;
                }
                mTapCount++;
            }
        });

        ((TextView) findViewById(R.id.about_version_text)).setText(BuildConfig.VERSION_NAME);

        TextView librariesTextView = (TextView) findViewById(R.id.activity_about_libraries);
        removeUnderlinesFromLinks(librariesTextView);

        TextView appLicenseTextView = (TextView)findViewById(R.id.about_app_license);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            appLicenseTextView.setText(Html.fromHtml(getString(R.string.about_app_license), Html.FROM_HTML_MODE_LEGACY));
        } else {
            appLicenseTextView.setText(Html.fromHtml(getString(R.string.about_app_license)));
        }
        removeUnderlinesFromLinks(appLicenseTextView);

        findViewById(R.id.rate_app_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });

        TextView aboutBasmachText = (TextView)findViewById(R.id.about_basmach);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            aboutBasmachText.setText(Html.fromHtml(getString(R.string.about_basmach), Html.FROM_HTML_MODE_LEGACY));
        } else {
            aboutBasmachText.setText(Html.fromHtml(getString(R.string.about_basmach)));
        }
        removeUnderlinesFromLinks(aboutBasmachText);

        makeEverythingClickable((ViewGroup) findViewById(R.id.about_container));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
    }

    public static void removeUnderlinesFromLinks(@NonNull TextView textView) {
        CharSequence text = textView.getText();
        if (text instanceof Spanned) {
            Spannable spannable = new SpannableString(text);
            removeUnderlinesFromLinks(spannable, spannable.getSpans(0, spannable.length(), URLSpan.class));
            textView.setText(spannable);
        }
    }

    public static void removeUnderlinesFromLinks(@NonNull Spannable spannable,
                                                 @NonNull URLSpan[] spans) {
        for (URLSpan span: spans) {
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            spannable.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            spannable.setSpan(span, start, end, 0);
        }
    }

    private void makeEverythingClickable(ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            if (vg.getChildAt(i) instanceof ViewGroup) {
                makeEverythingClickable((ViewGroup)vg.getChildAt(i));
            } else if (vg.getChildAt(i) instanceof TextView) {
                TextView tv = (TextView) vg.getChildAt(i);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }
}
