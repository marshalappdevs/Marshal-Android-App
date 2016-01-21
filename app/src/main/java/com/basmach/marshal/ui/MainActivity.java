package com.basmach.marshal.ui;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
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

import com.basmach.marshal.R;
import com.basmach.marshal.ui.fragments.CoursesFragment;
import com.basmach.marshal.ui.fragments.DiscussionsFragment;
import com.basmach.marshal.ui.fragments.MalshabFragment;
import com.basmach.marshal.ui.fragments.MaterialsFragment;
import com.basmach.marshal.ui.fragments.MeetupsFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_PERMISSIONS = 1;
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

        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isfirstrun", true);

        if (isFirstRun) {
            requestMultiplePermissions();
            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isfirstrun", false).commit();
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.GET_ACCOUNTS)== PackageManager.PERMISSION_GRANTED) {
            initializeGoogleApiClient();
        }


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

    private void requestMultiplePermissions() {
        String contactsPermission = Manifest.permission.GET_ACCOUNTS;
        String calendarPermission = Manifest.permission.WRITE_CALENDAR;
        int hasConPermission = ContextCompat.checkSelfPermission(this, contactsPermission);
        int hasCalPermission = ContextCompat.checkSelfPermission(this, calendarPermission);
        List<String> permissions = new ArrayList<>();
        if (hasConPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(contactsPermission);
        }
        if (hasCalPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(calendarPermission);
        }
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, REQUEST_PERMISSIONS);
        } else {
            // We already have permission, so handle as normal
            initializeGoogleApiClient();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                    // User granted permissions dialog
                    initializeGoogleApiClient();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR)) {
                    // User denied permissions dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle(R.string.permission_denied_title);
                    builder.setMessage(R.string.permission_denied_explanation);
                    builder.setPositiveButton(R.string.permission_dialog_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(R.string.permission_dialog_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            requestMultiplePermissions();
                        }
                    });
                    builder.show();
                } else {
                    // User denied permissions dialog and checked never ask again
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.mCoordinatorLayout), R.string.permission_denied_settings, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.undo_string, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                            System.exit(0);
                        }
                    });
                    snackbar.show();
                }
            }
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
            } else {
                Picasso.with(this)
                        .load(R.drawable.bg_empty_profile_art)
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

    public void avatarClicked(View v) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            requestMultiplePermissions();
        } else {
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected()) {
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    mGoogleApiClient.clearDefaultAccountAndReconnect();
                } else{
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
            Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
            sendIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"marshaldevs@gmail.com" });
            sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_subject));
            if (sendIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
            }
        } else if (id == R.id.nav_about) {
            CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();
            customTabsIntent.setShowTitle(true);
            int color = ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary);
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
