package com.basmapp.marshal.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.BuildConfig;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.util.LocaleUtils;
import com.basmapp.marshal.util.URLSpanNoUnderline;

public class AboutActivity extends BaseActivity {
    private static final String KEY_SCROLL_X = "KEY_SCROLL_X";
    private static final String KEY_SCROLL_Y = "KEY_SCROLL_Y";
    private ScrollView mScrollView;
    private int mTapCount, mLongClickCount;
    private ImageView mAppIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        mScrollView = (ScrollView) findViewById(R.id.about_scrollview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_about);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mAppIcon = (ImageView) findViewById(R.id.about_logo_image);

        mAppIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTapCount++;
                if (mTapCount == 7) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AboutActivity.this);
                    boolean debugMode = sharedPreferences.getBoolean(Constants.PREF_DEBUG_MODE, false);
                    sharedPreferences.edit().putBoolean(Constants.PREF_DEBUG_MODE, !debugMode).apply();
                    Toast.makeText(AboutActivity.this, !debugMode ? "Debug mode." : "Release mode.", Toast.LENGTH_LONG).show();
//                    Toast.makeText(AboutActivity.this, "Easter Egg!!! " + ("\ud83d\udc83"), Toast.LENGTH_LONG).show();
                    mTapCount = 0;
                }
            }
        });

        mAppIcon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mLongClickCount++;
                mAppIcon.clearAnimation();
                mAppIcon.setRotation(0);
                ViewCompat.animate(mAppIcon).rotation(360).setDuration(600).start();
                if (mLongClickCount == 3) {
                    Toast.makeText(AboutActivity.this, "I & O", Toast.LENGTH_LONG).show();
                    mLongClickCount = 0;
                }
                return true;
            }
        });

        ((TextView) findViewById(R.id.about_version_text)).setText(
                String.format(getString(R.string.about_version),
                        BuildConfig.VERSION_NAME,
                        Integer.toString(BuildConfig.VERSION_CODE)));

        TextView librariesTextView = (TextView) findViewById(R.id.activity_about_libraries);
        removeUnderlinesFromLinks(librariesTextView);

        TextView appLicenseTextView = (TextView) findViewById(R.id.about_app_license);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appLicenseTextView.setText(Html.fromHtml(getString(R.string.about_app_license), Html.FROM_HTML_MODE_LEGACY));
        } else {
            //noinspection deprecation
            appLicenseTextView.setText(Html.fromHtml(getString(R.string.about_app_license)));
        }
        removeUnderlinesFromLinks(appLicenseTextView);

        findViewById(R.id.rate_app_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + getPackageName())));
                } catch (android.content.ActivityNotFoundException ex) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                }
            }
        });

        TextView aboutBasmachText = (TextView) findViewById(R.id.about_basmach);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            aboutBasmachText.setText(Html.fromHtml(getString(R.string.about_basmach), Html.FROM_HTML_MODE_LEGACY));
        } else {
            //noinspection deprecation
            aboutBasmachText.setText(Html.fromHtml(getString(R.string.about_basmach)));
        }
        removeUnderlinesFromLinks(aboutBasmachText);

        makeEverythingClickable((ViewGroup) findViewById(R.id.about_container));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SCROLL_X, mScrollView.getScrollX());
        outState.putInt(KEY_SCROLL_Y, mScrollView.getScrollY());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int x = savedInstanceState.getInt(KEY_SCROLL_X);
        final int y = savedInstanceState.getInt(KEY_SCROLL_Y);
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.scrollTo(x, y);
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
        for (URLSpan span : spans) {
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
                makeEverythingClickable((ViewGroup) vg.getChildAt(i));
            } else if (vg.getChildAt(i) instanceof TextView) {
                TextView tv = (TextView) vg.getChildAt(i);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }
}
