package com.basmach.marshal.ui;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.basmach.marshal.Constants;
import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.ui.fragments.MaterialsFragment;
import com.basmach.marshal.ui.utils.LocaleUtils;
import com.basmach.marshal.ui.utils.ThemeUtils;

import java.util.ArrayList;

public class CourseMaterialsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Course mCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        LocaleUtils.updateLocale(this);

        setContentView(R.layout.activity_course_materials);

        mCourse = getIntent().getParcelableExtra(Constants.EXTRA_COURSE);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mCourse.getName() != null)
            mToolbar.setTitle(mCourse.getName());

        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_bottom);
            }
        });

        ArrayList<MaterialItem> materials = getIntent().getParcelableArrayListExtra(Constants.EXTRA_COURSE_MATERIALS_LIST);

        if (mCourse.getCourseCode() != null && materials != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.course_materials_container,
                    MaterialsFragment.newInstanceForCourse(mCourse.getCourseCode(), materials), null).commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_bottom);
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
