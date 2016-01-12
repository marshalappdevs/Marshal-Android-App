package com.basmach.marshal;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private long mLastPress = 0;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Fragment currentFragment = getFragmentManager().findFragmentById(R.id.content_frame);
            if (currentFragment instanceof CoursesFragment) {
                Toast onBackPressedToast = Toast.makeText(this, R.string.confirm_exit, Toast.LENGTH_SHORT);
                long currentTime = System.currentTimeMillis();
                if (currentTime - mLastPress > 2500) {
                    onBackPressedToast.show();
                    mLastPress = currentTime;
                } else {
                    onBackPressedToast.cancel();
                    super.onBackPressed();
                }
            }
            else {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, new CoursesFragment()).commit();
                getSupportActionBar().setTitle(R.string.navigation_drawer_courses);
            }
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getFragmentManager();

        if (id == R.id.nav_courses) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new CoursesFragment()).commit();
            getSupportActionBar().setTitle(item.getTitle());
        } else if (id == R.id.nav_materials) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MaterialsFragment()).commit();
            getSupportActionBar().setTitle(item.getTitle());
        } else if (id == R.id.nav_meetups) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MeetupsFragment()).commit();
            getSupportActionBar().setTitle(item.getTitle());
        } else if (id == R.id.nav_discussions) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new DiscussionsFragment()).commit();
            getSupportActionBar().setTitle(item.getTitle());
        } else if (id == R.id.nav_malshab) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MalshabFragment()).commit();
            getSupportActionBar().setTitle(item.getTitle());
        } else if (id == R.id.nav_settings) {
            Intent getSettingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(getSettingsIntent);
        } else if (id == R.id.nav_contact_us) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"marshaldevs@gmail.com" });
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else if (id == R.id.nav_about) {
            CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();
            customTabsIntent.setShowTitle(true);
            int color = getResources().getColor(R.color.colorPrimary);
            customTabsIntent.setToolbarColor(color);
            String url = "https://goo.gl/s6thV1";
            customTabsIntent.build().launchUrl(this, Uri.parse(url));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
