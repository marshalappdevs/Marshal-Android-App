package com.basmapp.marshal.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;


public class WebViewActivity extends BaseActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        CharSequence title = getIntent().getCharSequenceExtra(Constants.EXTRA_FORM_TITLE);
        if (title != null) {
            setTitle(getString(R.string.details_secondary_action_google_form) + ": " + title);
        }

        String url = getIntent().getStringExtra(Constants.EXTRA_FORM_URL);
        if (url == null) {
            finish();
            return;
        }

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // hide progress bar
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
            }
        });
        mWebView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public static Intent intent(@NonNull Context context, @NonNull String str, CharSequence charSequence) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(Constants.EXTRA_FORM_URL, str);
        intent.putExtra(Constants.EXTRA_FORM_TITLE, charSequence);
        return intent;
    }
}
