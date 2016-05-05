package com.basmach.marshal.ui;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.ActionMenuView;
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
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.basmach.marshal.BuildConfig;
import com.basmach.marshal.Constants;
import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Rating;
import com.basmach.marshal.interfaces.UpdateServiceListener;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.recievers.UpdateBroadcastReceiver;
import com.basmach.marshal.services.UpdateIntentService;
import com.basmach.marshal.ui.fragments.CoursesFragment;
import com.basmach.marshal.ui.fragments.CoursesSearchableFragment;
import com.basmach.marshal.ui.fragments.DiscussionsFragment;
import com.basmach.marshal.ui.fragments.MalshabFragment;
import com.basmach.marshal.ui.fragments.MaterialsFragment;
import com.basmach.marshal.ui.fragments.MeetupsFragment;
import com.basmach.marshal.utils.MockDataProvider;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
//    private static final int REQUEST_CONTACTS = 0;
//    private static final int REQUEST_CALENDAR = 1;
//    private static String[] PERMISSIONS_CALENDAR = {Manifest.permission.READ_CALENDAR,
//            Manifest.permission.WRITE_CALENDAR};
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
//    private ProgressDialog mProgressDialog;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;
    private SearchView mSearchView;
    private SharedPreferences mSharedPreferences;
    private TextView mNameTextView, mEmailTextView;
    private ImageView mProfileImageView, mCoverImageView;
    private boolean signedIn = false;

    // Fragments
    private CoursesFragment mCourseFragment;
    private MaterialsFragment mMaterialsFragment;
    private MenuItem mRefreshMenuItem;

    private UpdateBroadcastReceiver updateReceiver;

    private boolean mIsRefreshAnimationRunning = false;
    private ProgressDialog mUpdateProgressDialog;

    private List<Object> mCoursesData;
    private List<Object> mRatingsData;

    public static int lastCoursesViewPagerIndex = 0;
    public static ArrayList<Course> allCourses;
    public static ArrayList<Rating> allRatings;
    public static String userEmailAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateTheme();
        super.onCreate(savedInstanceState);
        updateLocale();
        // enable on final release
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        // translucent navigation bar
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_main);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_courses));
        mNavigationView.setCheckedItem(R.id.nav_courses);

        initializeUpdateProgressBar();

        mNameTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_name_text);
        mEmailTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_email_text);
        mCoverImageView = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_cover_image);
        mProfileImageView = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_image);
        mProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileImageClicked();
            }
        });

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
//        animateToolbar();

        initializeGoogleSignIn();

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

//        MockDataProvider mockDataProvider = new MockDataProvider(this);
//        mockDataProvider.insertAllMaterialItems();
//        mockDataProvider.insertAllCycles();
//        mockDataProvider.insertAllCourses();

        updateReceiver = new UpdateBroadcastReceiver(MainActivity.this, new UpdateServiceListener() {
            @Override
            public void onFinish() {
                if (mRefreshMenuItem != null) {

                    Drawable drawable = mRefreshMenuItem.getIcon();

                    if (drawable instanceof Animatable) {
                        ((Animatable) drawable).stop();
                    }

                    mIsRefreshAnimationRunning = false;

                    mCoursesData = null;
                    allCourses = null;
                    allRatings = null;

                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                    if (currentFragment instanceof CoursesFragment) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new CoursesFragment()).commit();
                    } else if (currentFragment instanceof MaterialsFragment) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new MaterialsFragment()).commit();
                    }

                    mUpdateProgressDialog.dismiss();
                    initializeUpdateProgressBar();

                    if(mSharedPreferences != null) {
                        if (mSharedPreferences.getBoolean(Constants.PREF_IS_FIRST_RUN, false)) {
                            mSharedPreferences.edit().putBoolean(Constants.PREF_IS_FIRST_RUN, false).apply();
                        }
                    }
                }
            }

            @Override
            public void onProgressUpdate(String message, int progressPercent) {
                if (progressPercent > 0) {
                    mUpdateProgressDialog.setIndeterminate(false);
                }

                mUpdateProgressDialog.setMessage(message);
                mUpdateProgressDialog.setProgress(progressPercent);
                mUpdateProgressDialog.setSecondaryProgress(progressPercent);
            }
        });

        checkIfFirstRun();
    }

    private void initializeUpdateProgressBar() {
        mUpdateProgressDialog = new ProgressDialog(MainActivity.this);
        mUpdateProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mUpdateProgressDialog.setCanceledOnTouchOutside(false);
        mUpdateProgressDialog.setIndeterminate(true);
        mUpdateProgressDialog.setMessage(getString(R.string.refresh_checking_for_updates));
        mUpdateProgressDialog.setProgress(0);
    }

    private void checkIfFirstRun() {
        if(mSharedPreferences != null) {
            if (mSharedPreferences.getBoolean(Constants.PREF_IS_FIRST_RUN, true)) {
                updateData();
                mSharedPreferences.edit().putBoolean(Constants.PREF_IS_FIRST_RUN, true).apply();
            }
        }
    }

    //// TODO: 11/04/2016 replace search fragment with search activity and handle it there, right now MainActivity set to singleTop
    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if(!isFinishing()) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame,
                        CoursesSearchableFragment.newInstance(query, CoursesFragment.mCoursesList, allRatings))
                        .commitAllowingStateLoss();
            }
        }
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
        registerUpdateReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(updateReceiver);
    }

    private void registerInternetCheckReceiver() {
        IntentFilter internetFilter = new IntentFilter();
        internetFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(broadcastReceiver, internetFilter);
    }

    private void registerUpdateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(UpdateIntentService.ACTION_CHECK_FOR_UPDATE);
        filter.addAction(UpdateIntentService.ACTION_UPDATE_DATA);
        filter.addAction(UpdateIntentService.ACTION_UPDATE_DATA_PROGRESS_CHANGED);
        registerReceiver(updateReceiver, filter);
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
                snackbar.setDuration(10000);
                snackbar.show();
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

    private void animateToolbar() {

        View t = mToolbar.getChildAt(0);
        if (t != null && t instanceof TextView) {
            TextView title = (TextView) t;

            title.setAlpha(0f);
            title.setScaleX(0.8f);

            title.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .setStartDelay(300)
                    .setDuration(900);
        }
        View amv = mToolbar.getChildAt(1);
        if (amv != null & amv instanceof ActionMenuView) {
            ActionMenuView actions = (ActionMenuView) amv;
            popAnim(actions.getChildAt(0), 500, 200); // filter
            popAnim(actions.getChildAt(1), 700, 200); // overflow
        }
    }

    private void popAnim(View v, int startDelay, int duration) {
        if (v != null) {
            v.setAlpha(0f);
            v.setScaleX(0f);
            v.setScaleY(0f);

            v.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(startDelay)
                    .setDuration(duration)
                    .setInterpolator(AnimationUtils.loadInterpolator(this,
                            android.R.interpolator.overshoot));
        }
    }

//    private void requestContactsPermission() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)) {
//            // Provide an additional rationale to the user if the permission was not granted
//            // and the user would benefit from additional context for the use of the permission.
//            // For example, if the request has been denied previously.
//            new AlertDialog.Builder(this)
//                    .setMessage(R.string.permission_contacts_access_for_gplus)
//                    .setPositiveButton(R.string.permission_continue, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_CONTACTS);
//                        }
//                    })
//                    .setNegativeButton(R.string.permission_cancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    })
//                    .show();
//        } else {
//            // Contact permission has not been granted yet. Request it directly.
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_CONTACTS);
//        }
//    }

//    private void requestCalendarPermission() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR)
//                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR)) {
//            // Provide an additional rationale to the user if the permission was not granted
//            // and the user would benefit from additional context for the use of the permission.
//            // For example, if the request has been denied previously.
//            new AlertDialog.Builder(this)
//                    .setMessage(R.string.permission_calendar_access_for_meetups)
//                    .setPositiveButton(R.string.permission_continue, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_CALENDAR, REQUEST_CALENDAR);
//                        }
//                    })
//                    .setNegativeButton(R.string.permission_cancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    })
//                    .show();
//        } else {
//            // Calendar permissions have not been granted yet. Request them directly.
//            ActivityCompat.requestPermissions(this, PERMISSIONS_CALENDAR, REQUEST_CALENDAR);
//        }
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CONTACTS) {
//            boolean contactsNeverAskAgain = mSharedPreferences.getBoolean("contactsNeverAskAgain", false);
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // User granted permissions dialog
//                initializeGoogleSignIn();
//                signIn();
//            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)) {
//                // User denied permissions dialog
//            } else {
//                // User denied permissions dialog and checked never ask again
//                if (contactsNeverAskAgain) {
//                    new AlertDialog.Builder(this)
//                            .setMessage(R.string.permission_contacts_access)
//                            .setPositiveButton(R.string.permission_settings_open, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    Intent intent = new Intent();
//                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                    Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
//                                    intent.setData(uri);
//                                    startActivity(intent);
//                                    finish();
//                                }
//                            })
//                            .setNegativeButton(R.string.permission_cancel, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            })
//                            .show();
//                }
//                mSharedPreferences.edit().putBoolean("contactsNeverAskAgain", true).apply();
//            }
//        }
//        if (requestCode == REQUEST_CALENDAR) {
//            boolean calendarNeverAskAgain = mSharedPreferences.getBoolean("calendarNeverAskAgain", false);
//            if (PermissionUtil.verifyPermissions(grantResults)) {
//                // User granted permissions dialog
//            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR)
//                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR)) {
//                // User denied permissions dialog
//            } else {
//                // User denied permissions dialog and checked never ask again
//                if (calendarNeverAskAgain) {
//                    new AlertDialog.Builder(this)
//                            .setMessage(R.string.permission_calendar_access)
//                            .setPositiveButton(R.string.permission_settings_open, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    Intent intent = new Intent();
//                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                    Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
//                                    intent.setData(uri);
//                                    startActivity(intent);
//                                    finish();
//                                }
//                            })
//                            .setNegativeButton(R.string.permission_cancel, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            })
//                            .show();
//                }
//                mSharedPreferences.edit().putBoolean("calendarNeverAskAgain", true).apply();
//            }
//        } else {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }

    private void initializeGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
    }

    @Override
    public void onStart() {
        super.onStart();
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
//                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
//                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            signedIn = true;

            if (acct != null) {
                mNameTextView.setText(acct.getDisplayName());
                mEmailTextView.setText(acct.getEmail());
                MainActivity.userEmailAddress = acct.getEmail();
                Uri uri = acct.getPhotoUrl();
                Picasso.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_profile_none)
                        .into(mProfileImageView, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                Bitmap bitmap = ((BitmapDrawable) mProfileImageView.getDrawable()).getBitmap();
                                RoundedBitmapDrawable rounded = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                                rounded.setCornerRadius(bitmap.getWidth());
                                mProfileImageView.setImageDrawable(rounded);
                            }

                            @Override
                            public void onError() {

                            }
                        });

                Plus.PeopleApi.load(mGoogleApiClient, acct.getId()).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(@NonNull People.LoadPeopleResult peopleData) {
                        if (peopleData.getStatus().isSuccess()) {
                            PersonBuffer personBuffer = peopleData.getPersonBuffer();
                            if (personBuffer != null && personBuffer.getCount() > 0) {
                                Person person = personBuffer.get(0);
                                personBuffer.release();
                                if (person.getCover() != null) {
                                    Picasso.with(MainActivity.this)
                                            .load(person.getCover().getCoverPhoto().getUrl())
                                            .placeholder(R.drawable.bg_default_profile_art)
                                            .into(mCoverImageView);
                                }
                            } // else Log.e(TAG, "Plus response was empty! Failed to load profile.");
                        } // else Log.e(TAG, "Failed to load plus proflie, error " + loadPeopleResult.getStatus().getStatusCode());
                    }
                });
            }
        } else {
            // Signed out, show unauthenticated UI.
            signedIn = false;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

//    private void signOut() {
//        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
//                new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(@NonNull Status status) {
//                        // [START_EXCLUDE]
//                        signedIn = false;
//                        // [END_EXCLUDE]
//                    }
//                });
//    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // [START_EXCLUDE]
                        signedIn = false;
                        // [END_EXCLUDE]
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
//        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

//    private void showProgressDialog() {
//        if (mProgressDialog == null) {
//            mProgressDialog = new ProgressDialog(this);
//            mProgressDialog.setMessage(getString(R.string.loading));
//            mProgressDialog.setIndeterminate(true);
//        }
//        mProgressDialog.show();
//    }
//
//    private void hideProgressDialog() {
//        if (mProgressDialog != null && mProgressDialog.isShowing()) {
//            mProgressDialog.hide();
//        }
//    }

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

    public void profileImageClicked() {
        if (signedIn) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            new AlertDialog.Builder(this)
                    .setMessage(R.string.sign_out_confirm)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                revokeAccess();
                                MainActivity.userEmailAddress = null;
                                recreate();
                            }
                        })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                    .show();
        } else {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            signIn();
        }
    }

    public String debugInfo() {
        long freeBytesInternal = new File(getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
        String freeGBInternal = String.format(Locale.getDefault(), "%.2f", freeBytesInternal / Math.pow(2, 30));
        String debugInfo="--Support Info--";
        debugInfo += "\n Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
        debugInfo += "\n Manufacturer: " + Build.MANUFACTURER;
        debugInfo += "\n Model: " + Build.MODEL;
        debugInfo += "\n Locale: " + getBaseContext().getResources().getConfiguration().locale.toString();
        debugInfo += "\n OS: " + Build.VERSION.RELEASE + " ("+android.os.Build.VERSION.SDK_INT+")";
        debugInfo += "\n Free Space: " + freeBytesInternal + " (" + freeGBInternal + " GB)";
        float density = getResources().getDisplayMetrics().density;
        String densityName = null;
        if (density == 4.0) densityName = "xxxhdpi";
        if (density == 3.0) densityName = "xxhdpi";
        if (density == 2.0) densityName = "xhdpi";
        if (density == 1.5) densityName = "hdpi";
        if (density == 1.0) densityName = "mdpi";
        if (density == 0.75) densityName = "ldpi";
        debugInfo += "\n Screen Density: " + density + " (" + densityName + ")";
        debugInfo += "\n Target: " + BuildConfig.BUILD_TYPE;
        return debugInfo;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_main_searchView);
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
                String searchResult = String.format(getString(R.string.search_result), query);
                Toast.makeText(getApplicationContext(), searchResult, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        mRefreshMenuItem = menu.findItem(R.id.menu_main_refresh);
        mRefreshMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!mIsRefreshAnimationRunning) {
                    Drawable drawable = menuItem.getIcon();
                    if (drawable instanceof Animatable) {
                        ((Animatable) drawable).start();
                    }
                    updateData();
                }
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        mRefreshMenuItem.setVisible(true);
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        mRefreshMenuItem.setVisible(false);
                        return true; // Return true to expand action view
                    }
                });

        return true;
    }

    private void updateData() {
        mUpdateProgressDialog.show();
        mIsRefreshAnimationRunning = true;
        Intent updateServiceIntent = new Intent(MainActivity.this, UpdateIntentService.class);
        updateServiceIntent.setAction(UpdateIntentService.ACTION_CHECK_FOR_UPDATE);
        startService(updateServiceIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_main_searchView) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_courses) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new CoursesFragment()).commit();
//            fragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.content_frame, new CoursesFragment()).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_materials) {
            if(mMaterialsFragment == null) {
                mMaterialsFragment = new MaterialsFragment();
            }
            fragmentManager.beginTransaction().replace(R.id.content_frame, mMaterialsFragment).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_meetups) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
//                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
//                requestCalendarPermission();
//            }
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MeetupsFragment()).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_discussions) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new DiscussionsFragment()).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_malshab) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MalshabFragment()).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_contact_us) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"marshaldevs@gmail.com" });
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_subject));
            File tempFile = new File(getBaseContext().getExternalCacheDir() + "/" + "log.txt") ;
            try {
                FileWriter writer = new FileWriter(tempFile);
                writer.write(debugInfo());
                writer.close();
                Uri log = Uri.fromFile(tempFile);
                emailIntent.putExtra(Intent.EXTRA_STREAM, log);
            } catch (IOException e) {
                e.printStackTrace();
            }
            startActivity(Intent.createChooser(emailIntent, getResources().getText(R.string.send_to)));
        } else if (id == R.id.nav_about) {
            String url = "https://goo.gl/s6thV1";
            Boolean cct = mSharedPreferences.getBoolean("CCT", true);
            if (cct) {
                new CustomTabsIntent.Builder()
                        .setToolbarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                        .setShowTitle(true)
                        .addDefaultShareMenuItem()
                        .setCloseButtonIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back_wht))
                        .build()
                        .launchUrl(this, Uri.parse(url));
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        }
        if (mDrawerLayout != null) mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void getCoursesDataAsync(Boolean showProgressBar, final BackgroundTaskCallBack callBack) {
        if(mCoursesData != null) {
            callBack.onSuccess("", mCoursesData);
        } else {
            Course.getAllInBackground(DBConstants.COL_NAME, Course.class, MainActivity.this,
                    showProgressBar,
                    new BackgroundTaskCallBack() {
                @Override
                public void onSuccess(String result, List<Object> data) {
                    if (data != null && data.size() > 0) {
                        mCoursesData = data;
                    }

                    callBack.onSuccess(result, mCoursesData);
                }

                @Override
                public void onError(String error) {
                    callBack.onError(error);
                }
            });
        }
    }

    public void getRatingsDataAsync(Boolean showProgressBar, final BackgroundTaskCallBack callBack) {
        if (mRatingsData != null) {
            callBack.onSuccess("", mRatingsData);
        } else {
            Rating.getAllInBackground(DBConstants.COL_COURSE_CODE, Rating.class, MainActivity.this,
                    showProgressBar, new BackgroundTaskCallBack() {
                        @Override
                        public void onSuccess(String result, List<Object> data) {
                            if (data != null && data.size() > 0) {
                                mRatingsData = data;
                            }

                            callBack.onSuccess(result, mRatingsData);
                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
        }
    }
}
