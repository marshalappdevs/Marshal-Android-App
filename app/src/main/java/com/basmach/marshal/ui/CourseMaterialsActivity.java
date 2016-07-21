package com.basmach.marshal.ui;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.ui.fragments.MaterialsFragment;
import com.basmach.marshal.ui.utils.LocaleUtils;
import com.basmach.marshal.ui.utils.ThemeUtils;

import java.util.ArrayList;

public class CourseMaterialsActivity extends AppCompatActivity {

    public static final String EXTRA_TOOLBAR_COLOR = "EXTRA_TOOLBAR_COLOR";
    public static final String EXTRA_COURSE_MATERIALS_LIST = "EXTRA_COURSE_MATERIALS_LIST";

    private Toolbar mToolbar;
    private String mCourseCode;
    private String mCourseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        LocaleUtils.updateLocale(this);

        setContentView(R.layout.activity_course_materials);

        supportPostponeEnterTransition();

        final View decor = getWindow().getDecorView();
        decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                decor.getViewTreeObserver().removeOnPreDrawListener(this);
                supportStartPostponedEnterTransition();
                return true;
            }
        });

        mCourseName = getIntent().getStringExtra(MainActivity.EXTRA_COURSE_NAME);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(mCourseName);
        mCourseCode = getIntent().getStringExtra(MainActivity.EXTRA_COURSE_CODE);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayList<MaterialItem> materials = getIntent().getParcelableArrayListExtra(EXTRA_COURSE_MATERIALS_LIST);

        if (mCourseCode != null && materials != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.course_materials_container,
                    MaterialsFragment.newInstanceForCourse(mCourseCode, materials), null).commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleUtils.updateLocale(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_materials, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
