package com.basmapp.marshal;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.basmapp.marshal.util.LocaleUtils;
import com.basmapp.marshal.util.ThemeUtils;

public class BaseActivity extends AppCompatActivity {
    public BaseActivity() {
        LocaleUtils.updateConfig(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.updateTheme(this);
        LocaleUtils.updateLocale(this);
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleUtils.updateConfig(this, newConfig);
    }
}
