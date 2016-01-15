package com.basmach.marshal;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_courses));
        navigationView.setCheckedItem(R.id.nav_courses);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkForGetAccountsPremission();

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

//    public void headerClicked(View viewClicked) {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        Snackbar.make(viewClicked, "Header Clicked", Snackbar.LENGTH_LONG).setAction("Action", null).show();
//    }

    private void checkForGetAccountsPremission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.GET_ACCOUNTS)) {

            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
            }
        } else {
            initializeGoogleApiClient();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeGoogleApiClient();
                } else {
                    Snackbar mySnackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.permission_denied, Snackbar.LENGTH_LONG);
                    mySnackbar.setAction(R.string.undo_string, new PermissionDeniedListener());
                    mySnackbar.show();
                }
                return;
            }
        }
    }

    public class PermissionDeniedListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
        }
    }

    private void initializeGoogleApiClient() {
        mGoogleApiClient = buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    public GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    long lastPress;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Fragment currentFragment = getFragmentManager().findFragmentById(R.id.content_frame);
            if (currentFragment instanceof CoursesFragment) {
                long currentTime = System.currentTimeMillis();
                if(currentTime - lastPress > 3000){
                    Toast.makeText(this, R.string.confirm_exit, Toast.LENGTH_SHORT).show();
                    lastPress = currentTime;
                }else{
                    super.onBackPressed();
                }
            }
            else {
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.setNavigationItemSelectedListener(this);
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_courses));
                navigationView.setCheckedItem(R.id.nav_courses);
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        } else {
            mResolvingError = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Person user = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        if (user != null) {
            if (user.getImage().hasUrl()) {
                String portrait = user.getImage().getUrl();
                Picasso.with(this)
                        .load(portrait.split("\\?")[0])
                        .into((CircleImageView) findViewById(R.id.profile_image));
            }

            if (user.getCover() != null) {
                Picasso.with(this)
                        .load(user.getCover().getCoverPhoto().getUrl())
                        .into((ImageView) findViewById(R.id.banner_image));
            }

            TextView tvMail = (TextView) findViewById(R.id.profile_email);
            if(tvMail != null)
                tvMail.setText(Plus.AccountApi.getAccountName(mGoogleApiClient));

            Person.Name userName = user.getName();
            ((TextView) findViewById(R.id.profile_name))
                    .setText(String.format("%s %s", userName.getGivenName(), userName.getFamilyName()));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
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
            setTitle(item.getTitle());
        } else if (id == R.id.nav_materials) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MaterialsFragment()).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_meetups) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MeetupsFragment()).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_discussions) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new DiscussionsFragment()).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_malshab) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MalshabFragment()).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_settings) {
            Intent getSettingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(getSettingsIntent);
        } else if (id == R.id.nav_contact_us) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"marshaldevs@gmail.com" });
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_subject));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else if (id == R.id.nav_about) {
            CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();
            customTabsIntent.setShowTitle(true);
            int color = getResources().getColor(R.color.colorPrimary);
            customTabsIntent.setToolbarColor(color);
            Configuration config = getResources().getConfiguration();
            if(config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                Bitmap closeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back_rtl);
                customTabsIntent.setCloseButtonIcon(closeIcon);
            }else {
                    Bitmap closeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back);
                    customTabsIntent.setCloseButtonIcon(closeIcon);
            }
            String url = "https://goo.gl/s6thV1";
            customTabsIntent.build().launchUrl(this, Uri.parse(url));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
