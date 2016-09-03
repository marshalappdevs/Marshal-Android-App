package com.basmapp.marshal.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.MaterialItem;
import com.basmapp.marshal.ui.fragments.MaterialsFragment;
import com.basmapp.marshal.util.ThemeUtils;

import java.util.ArrayList;

public class CourseMaterialsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Course mCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.updateTheme(this);
        super.onCreate(savedInstanceState);

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
            }
        });

        ArrayList<MaterialItem> materials = getIntent().getParcelableArrayListExtra(Constants.EXTRA_COURSE_MATERIALS_LIST);

        if (mCourse.getCourseCode() != null && materials != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.course_materials_container,
                    MaterialsFragment.newInstanceForCourse(mCourse.getCourseCode(), materials), null).commit();
        }
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
