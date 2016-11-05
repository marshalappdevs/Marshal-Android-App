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
import com.basmapp.marshal.R;


public class WebViewActivity extends BaseActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view_layout);

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

        CharSequence title = getIntent().getCharSequenceExtra("extra_title");
        if (title != null) {
            setTitle(title);
        }

        String url = getIntent().getStringExtra("extra_url");
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
        intent.putExtra("extra_url", str);
        intent.putExtra("extra_title", charSequence);
        return intent;
    }
}
