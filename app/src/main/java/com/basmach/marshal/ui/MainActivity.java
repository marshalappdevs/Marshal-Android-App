package com.basmach.marshal.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.basmach.marshal.BuildConfig;
import com.basmach.marshal.R;
import com.basmach.marshal.ui.fragments.CoursesFragment;
import com.basmach.marshal.ui.fragments.DiscussionsFragment;
import com.basmach.marshal.ui.fragments.MalshabFragment;
import com.basmach.marshal.ui.fragments.MaterialsFragment;
import com.basmach.marshal.ui.fragments.MeetupsFragment;
import com.basmach.marshal.ui.utils.InkPageIndicator;
import com.basmach.marshal.ui.utils.PermissionUtil;
import com.basmach.marshal.ui.utils.CatalogImagesPagerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements CoursesFragment.OnFragmentInteractionListener, DiscussionsFragment.OnFragmentInteractionListener, MalshabFragment.OnFragmentInteractionListener, MaterialsFragment.OnFragmentInteractionListener, MeetupsFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_CONTACTS = 0;
    private static final int REQUEST_CALENDAR = 1;
    private static String[] PERMISSIONS_CALENDAR = {Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR};
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;
    private SearchView mSearchView;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateTheme();
        super.onCreate(savedInstanceState);
        updateLocale();
        // enable on final release
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_courses));
        mNavigationView.setCheckedItem(R.id.nav_courses);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        boolean isFirstRun = mSharedPreferences.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            requestContactsPermission();
            mSharedPreferences.edit().putBoolean("isFirstRun", false).apply();
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        ViewPager mViewPager = (ViewPager) findViewById(R.id.main_catalog_images_viewPager);
        CatalogImagesPagerAdapter adapter = new CatalogImagesPagerAdapter(MainActivity.this);
        mViewPager.setAdapter(adapter);

        InkPageIndicator inkPageIndicator = (InkPageIndicator) findViewById(R.id.main_catalog_images_indicator);
        inkPageIndicator.setViewPager(mViewPager);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLocale();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerInternetCheckReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void registerInternetCheckReceiver() {
        IntentFilter internetFilter = new IntentFilter();
        internetFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(broadcastReceiver, internetFilter);
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (!isConnected()) {
                final Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.offline_snackbar_network_unavailable, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.offline_snackbar_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isConnected()) snackbar.dismiss();
                                else onReceive(context, intent);
                            }
                        });
                snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_light));
                snackbar.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        snackbar.dismiss();
                    }
                }, 10000);
            }
        }
    };

    private void updateTheme() {
        String theme = mSharedPreferences.getString("THEME", "light");
        if (theme.equals("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        if (theme.equals("auto")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        }
        getDelegate().applyDayNight();
        setTheme(R.style.AppTheme);
    }

    private void updateLocale() {
        Configuration config = getBaseContext().getResources().getConfiguration();
        String lang = mSharedPreferences.getString("LANG", "iw");
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            Locale locale = new Locale(lang);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.setLocale(locale);
            Locale.setDefault(locale);
            res.updateConfiguration(conf, dm);
        }
    }

    private void requestContactsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            new AlertDialog.Builder(this)
                    .setMessage(R.string.permission_contacts_access_for_gplus)
                    .setPositiveButton(R.string.permission_continue, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_CONTACTS);
                        }
                    })
                    .setNegativeButton(R.string.permission_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            // Contact permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_CONTACTS);
        }
    }

    private void requestCalendarPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            new AlertDialog.Builder(this)
                    .setMessage(R.string.permission_calendar_access_for_meetups)
                    .setPositiveButton(R.string.permission_continue, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_CALENDAR, REQUEST_CALENDAR);
                        }
                    })
                    .setNegativeButton(R.string.permission_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            // Calendar permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, PERMISSIONS_CALENDAR, REQUEST_CALENDAR);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CONTACTS) {
            boolean contactsNeverAskAgain = mSharedPreferences.getBoolean("contactsNeverAskAgain", false);
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted permissions dialog
                initializeGoogleApiClient();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)) {
                // User denied permissions dialog
            } else {
                // User denied permissions dialog and checked never ask again
                if (contactsNeverAskAgain) {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.permission_contacts_access)
                            .setPositiveButton(R.string.permission_settings_open, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.permission_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
                mSharedPreferences.edit().putBoolean("contactsNeverAskAgain", true).apply();
            }
        }
        if (requestCode == REQUEST_CALENDAR) {
            boolean calendarNeverAskAgain = mSharedPreferences.getBoolean("calendarNeverAskAgain", false);
            if (PermissionUtil.verifyPermissions(grantResults)) {
                // User granted permissions dialog
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR)) {
                // User denied permissions dialog
            } else {
                // User denied permissions dialog and checked never ask again
                if (calendarNeverAskAgain) {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.permission_calendar_access)
                            .setPositiveButton(R.string.permission_settings_open, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.permission_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
                mSharedPreferences.edit().putBoolean("calendarNeverAskAgain", true).apply();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                        .into((ImageView) findViewById(R.id.profile_image), new Callback() {
                            @Override
                            public void onSuccess() {
                                ImageView imageView = (ImageView) findViewById(R.id.profile_image);
                                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                RoundedBitmapDrawable rounded = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                                rounded.setCornerRadius(bitmap.getWidth());
                                imageView.setImageDrawable(rounded);
                            }
                            @Override
                            public void onError() {
                            }
                        });
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
            if (tvMail != null)
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

    long lastPress;

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (currentFragment instanceof CoursesFragment) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastPress > 3000) {
                    Toast.makeText(this, R.string.confirm_exit, Toast.LENGTH_SHORT).show();
                    lastPress = currentTime;
                } else {
                    super.onBackPressed();
                }
            } else {
                mNavigationView.setNavigationItemSelectedListener(this);
                onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_courses));
                mNavigationView.setCheckedItem(R.id.nav_courses);
            }
        }
    }

    public void avatarClicked(View v) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            requestContactsPermission();
        } else {
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected()) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    mGoogleApiClient.clearDefaultAccountAndReconnect();
                } else {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // Text has changed, apply filtering?
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform the final search
                mSearchView.clearFocus();
                Toast.makeText(getApplicationContext(), "מציג תוצאות עבור: " + query, Toast.LENGTH_LONG).show();
                return true;
            }
        });
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

    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.catalog_preview);

        if (id == R.id.nav_courses) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new CoursesFragment()).commit();
//            fragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.content_frame, new CoursesFragment()).commit();
            relativeLayout.setVisibility(View.VISIBLE);
            setTitle(item.getTitle());
        } else if (id == R.id.nav_materials) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MaterialsFragment()).commit();
            relativeLayout.setVisibility(View.INVISIBLE);
            setTitle(item.getTitle());
        } else if (id == R.id.nav_meetups) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                requestCalendarPermission();
            }
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MeetupsFragment()).commit();
            relativeLayout.setVisibility(View.INVISIBLE);
            setTitle(item.getTitle());
        } else if (id == R.id.nav_discussions) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new DiscussionsFragment()).commit();
            relativeLayout.setVisibility(View.INVISIBLE);
            setTitle(item.getTitle());
        } else if (id == R.id.nav_malshab) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MalshabFragment()).commit();
            relativeLayout.setVisibility(View.INVISIBLE);
            setTitle(item.getTitle());
        } else if (id == R.id.nav_settings) {
            Intent getSettingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(getSettingsIntent);
        } else if (id == R.id.nav_contact_us) {
            String versionName = BuildConfig.VERSION_NAME;
            int versionCode = BuildConfig.VERSION_CODE;
            Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
            sendIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"marshaldevs@gmail.com" });
            sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_subject) + " (v" + versionName + " / " + versionCode + ")");
            if (sendIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
            }
        } else if (id == R.id.nav_about) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary)).setShowTitle(true);
            builder.setCloseButtonIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back_wht));
            String url = "https://goo.gl/s6thV1";
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(url));
        }
        registerInternetCheckReceiver();
        if (mDrawerLayout != null) mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
