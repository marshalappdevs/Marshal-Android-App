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
import android.graphics.BitmapFactory;
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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.basmach.marshal.BuildConfig;
import com.basmach.marshal.Constants;
import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.MalshabItem;
import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.interfaces.UpdateServiceListener;
import com.basmach.marshal.localdb.LocalDBHelper;
import com.basmach.marshal.receivers.UpdateBroadcastReceiver;
import com.basmach.marshal.services.GcmRegistrationService;
import com.basmach.marshal.services.UpdateIntentService;
import com.basmach.marshal.ui.fragments.CoursesFragment;
import com.basmach.marshal.ui.fragments.CoursesSearchableFragment;
import com.basmach.marshal.ui.fragments.DiscussionsFragment;
import com.basmach.marshal.ui.fragments.MalshabFragment;
import com.basmach.marshal.ui.fragments.MaterialsFragment;
import com.basmach.marshal.ui.fragments.MeetupsFragment;
import com.basmach.marshal.ui.utils.LocaleUtils;
import com.basmach.marshal.ui.utils.ThemeUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    //    private static final int REQUEST_CONTACTS = 0;
//    private static final int REQUEST_CALENDAR = 1;
//    private static String[] PERMISSIONS_CALENDAR = {Manifest.permission.READ_CALENDAR,
//            Manifest.permission.WRITE_CALENDAR};
    private static final int RC_SIGN_IN = 9001;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String ACTION_SHOW_COURSE_MATERIALS = "com.basmach.marshal.ACTION_SHOW_COURSE_MATERIALS";
    public static final String EXTRA_COURSE_CODE = "EXTRA_COURSE_CODE";
    public static final int RESULT_SHOW_COURSE_MATERIALS = 8001;
    public static final int RC_COURSE_ACTIVITY = 8000;
    public static final int RC_SHOW_ALL_ACTIVITY = 7999;
    public static final String EXTRA_IS_RUN_FOR_COURSE = "EXTRA_IS_RUN_FOR_COURSE";

    private GoogleApiClient mGoogleApiClient;
    //    private ProgressDialog mProgressDialog;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;
    private SearchView mSearchView;
    private SharedPreferences mSharedPreferences;
    private TextView mNameTextView, mEmailTextView;
    private CircleImageView mProfileImageView;
    private FrameLayout mNavHeaderFrame;
    private ImageView mCoverImageView;
    private Button mButtonRetry;
    private boolean signedIn = false;
    private MenuItem mSearchItem;

    // Fragments
    private CoursesFragment mCourseFragment;
    private MaterialsFragment mMaterialsFragment;
    private MalshabFragment mMalshabFragment;
    private MeetupsFragment mMeetupsFragment;

    private UpdateBroadcastReceiver updateReceiver;

    private ProgressDialog mUpdateProgressDialog;

    public static int sLastCoursesViewPagerIndex = 0;

    public static ArrayList<Course> sAllCourses;
    public static ArrayList<Course> sViewPagerCourses;
    public static ArrayList<Course> sMeetups;
    public static ArrayList<MaterialItem> sMaterialItems;
    public static ArrayList<MalshabItem> sMalshabItems;

    public static String sUserEmailAddress;
    public static String sUserName;
    public static Uri sUserProfileImage;

    public static LinearLayout sNewUpdatesButton;
    public static LinearLayout sErrorScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("LIFE_CYCLE","onCreate");
        // Change theme based on preference choice
        ThemeUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        // Change language based on preference choice
        LocaleUtils.updateLocale(this);

//        checkPlayServicesAvailability();
        checkGcmRegistrationState();

        // enable on final release to disable screenshots and more
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        // translucent navigation bar
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_main);

        // Initialize shared preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        // Set course fragment as main fragment
        onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_courses));
        mNavigationView.setCheckedItem(R.id.nav_courses);

        initializeUpdateProgressBar();

        MainActivity.sNewUpdatesButton = null;
        initializeNewUpdatesButton();

        // Initialize navigation view header items
        mNameTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_name_text);
        mEmailTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_email_text);
        mCoverImageView = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_cover_image);
        mProfileImageView = (CircleImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_image);
        mNavHeaderFrame = (FrameLayout) mNavigationView.getHeaderView(0).findViewById(R.id.navview_main_header_view);
        mNavHeaderFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Login to Google account on navigation view header press
                navHeaderClicked();
            }
        });

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        initializeGoogleSignIn();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (mSearchItem != null)
                    mSearchItem.collapseActionView();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        if(mSharedPreferences != null) {
            if (mSharedPreferences.getBoolean(Constants.PREF_IS_FIRST_RUN, true)) {
                // Show update progress bar on first app startup
                mUpdateProgressDialog.show();
            }
        }

        // Initialize error screen re-try button
        mButtonRetry = (Button) findViewById(R.id.retry_button);
        mButtonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update data if there is internet connection, else throw error toast
                if (isConnected()) {
                    updateData();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.offline_snackbar_network_unavailable),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // Broadcast receiver to update app data from server
        updateReceiver = new UpdateBroadcastReceiver(MainActivity.this, new UpdateServiceListener() {
            @Override
            public void onFinish(boolean result) {

                Log.i("LIFE_CYCLE", "updateReceiver -- onFinish: " + String.valueOf(result));
                if (mUpdateProgressDialog != null && mUpdateProgressDialog.isShowing()) {
                    mUpdateProgressDialog.dismiss();
                }

                if (mSharedPreferences.getBoolean(Constants.PREF_IS_FIRST_RUN, true)) {
                    if (result) {
                        // First app startup and update data succeed, restart app fragments
                        showFirstRun();
                    } else {
                        // Update data from server failed, show error screen
                        setErrorScreenVisibility(View.VISIBLE);
                    }
                } else {
                    if (result) {
                        // Update data succeed, but it's not first app startup, show new updates popup
                        showNewUpdatesButton();
                    }
                }
            }
        });
    }

    private void setErrorScreenVisibility(int visibility) {
        // Initialize error screen and set viability based on integer
        if (sErrorScreen == null) sErrorScreen = (LinearLayout) findViewById(R.id.placeholder_error);
        if (visibility != sErrorScreen.getVisibility()) sErrorScreen.setVisibility(visibility);
    }

    private void showNewUpdatesButton() {
        if (MainActivity.sNewUpdatesButton != null) {
            animateNewUpdatesButton(View.VISIBLE);
        } else {
            initializeNewUpdatesButton();
            animateNewUpdatesButton(View.VISIBLE);
        }
    }

    private void initializeNewUpdatesButton() {
        MainActivity.sNewUpdatesButton = (LinearLayout) findViewById(R.id.new_updates_button);
        MainActivity.sNewUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                releaseAllDataLists();

                // Show out animation and dismiss button
                animateNewUpdatesButton(View.INVISIBLE);
                // Restart app fragments to show new data
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (currentFragment instanceof CoursesFragment) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new CoursesFragment()).commit();
                }

                mCourseFragment = new CoursesFragment();
                mMaterialsFragment = new MaterialsFragment();
                mMalshabFragment = new MalshabFragment();
            }
        });
    }

    private void animateNewUpdatesButton (final int visibility) {
        Animation animation;
        if (visibility == View.VISIBLE) {
            // show in animation on visible
            animation = AnimationUtils.loadAnimation(this, R.anim.new_updates_button_in);
        } else {
            // show out animation on invisible
            animation = AnimationUtils.loadAnimation(this, R.anim.new_updates_button_out);
        }
        MainActivity.sNewUpdatesButton.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                MainActivity.sNewUpdatesButton.setVisibility(visibility);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void checkGcmRegistrationState(){
        if(!GcmRegistrationService.isDeviceRegistered(this)) {
            Intent intent = new Intent(this, GcmRegistrationService.class);
            intent.setAction(GcmRegistrationService.ACTION_REGISTER_NEW);
            startService(intent);
        }
    }

    private void checkPlayServicesAvailability() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                //Play Services is not installed/enabled
                googleApiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //This device does not support Play Services
            }
        }
    }

    private void initializeUpdateProgressBar() {
        mUpdateProgressDialog = new ProgressDialog(MainActivity.this);
        mUpdateProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mUpdateProgressDialog.setCanceledOnTouchOutside(false);
        mUpdateProgressDialog.setIndeterminate(true);
        mUpdateProgressDialog.setMessage(getString(R.string.refresh_checking_for_updates));
        mUpdateProgressDialog.setProgress(0);
    }

//    // TODO: 11/04/2016 replace search fragment with search activity and handle it there, right now MainActivity set to singleTop
//    @Override
//    protected void onNewIntent(Intent intent) {
//        // Get voice search query and pass it to CoursesSearchableFragment
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            if(!isFinishing()) {
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                fragmentManager.beginTransaction().replace(R.id.content_frame,
//                        CoursesSearchableFragment.newInstance(query, CoursesFragment.mCoursesList))
//                        .commitAllowingStateLoss();
//            }
//        }
//    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Change location again when configuration changed (screen rotation)
        LocaleUtils.updateLocale(this);
    }

    private void showFirstRun() {
        // Change shared preference value to false so next startup will not be called as first
        mSharedPreferences.edit().putBoolean(Constants.PREF_IS_FIRST_RUN, false).apply();
        // Restart app fragments, set course fragment as default and dismiss error screen
        releaseAllDataLists();
        mCourseFragment = null;
        mMaterialsFragment = null;
        mMalshabFragment = null;

        setErrorScreenVisibility(View.GONE);
        onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_courses));
        mNavigationView.setCheckedItem(R.id.nav_courses);
    }

    private void releaseAllDataLists() {
        sAllCourses = null;
        sViewPagerCourses = null;
        sMeetups = null;
        sMaterialItems = null;
        sMalshabItems = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("LIFE_CYCLE","onResume");
        // Register update data from server broadcast and check internet connection broadcast
        registerInternetCheckReceiver();
        registerUpdateReceiver();

        // Initialize shared preference if it's null
        if (mSharedPreferences == null)
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isFirstRun = mSharedPreferences.getBoolean(Constants.PREF_IS_FIRST_RUN, true);
        boolean isUpdateServiceSuccessOnce = mSharedPreferences.getBoolean(Constants.PREF_IS_UPDATE_SERVICE_SUCCESS_ONCE, false);

        if (isFirstRun) {
            if (isUpdateIntentServiceRunning() && isConnected()) {
                // If there is internet connection and update service is running show progress bar and dismiss error screen
                if (mUpdateProgressDialog == null)
                    initializeUpdateProgressBar();
                if (!mUpdateProgressDialog.isShowing())
                    mUpdateProgressDialog.show();
                setErrorScreenVisibility(View.GONE);
            } else {
                // There is no internet connection or update service is not running
                if (!isUpdateServiceSuccessOnce) {
                    // Update service failed, dismiss update progress dialog if it's visible and show error screen
                    if (mUpdateProgressDialog != null && mUpdateProgressDialog.isShowing())
                        mUpdateProgressDialog.dismiss();
                    setErrorScreenVisibility(View.VISIBLE);
                } else {
                    // Update service succeed, dismiss update progress dialog if it's visible and restart fragments
                    if (mUpdateProgressDialog != null && mUpdateProgressDialog.isShowing())
                        mUpdateProgressDialog.dismiss();
                    setErrorScreenVisibility(View.GONE);
                    showFirstRun();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("LIFE_CYCLE","onPause");
        // Unregister update data from server broadcast and check internet connection broadcast
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(updateReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Set viewpager page to 0 when app is closed
        sLastCoursesViewPagerIndex = 0;
        // Close db if exist when app is closed
        LocalDBHelper.closeIfExist();
    }

    private boolean isUpdateIntentServiceRunning() {
//        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if ("com.basmach.marshal.UpdateIntentService".equals(service.service.getClassName())) {
//                Log.i("IS_SERVICE_RUNNING", " --- true");
//                return true;
//            }
//        }
//        Log.i("IS_SERVICE_RUNNING", " --- false");
//        return false;

        return UpdateIntentService.isRunning;
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
        // Check if there is internet connection and save the result as boolean
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (!isConnected()) {
                // There is no internet connection, show error snackbar
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

                // As there is no internet connection, dismiss update dialog if showed and show error screen
                if (mUpdateProgressDialog != null && mUpdateProgressDialog.isShowing())
                    mUpdateProgressDialog.dismiss();

                if (mSharedPreferences == null) mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                if(mSharedPreferences.getBoolean(Constants.PREF_IS_FIRST_RUN, true)) {
                    if (!mSharedPreferences.getBoolean(Constants.PREF_IS_UPDATE_SERVICE_SUCCESS_ONCE, false)) {
                        setErrorScreenVisibility(View.VISIBLE);
                    } else {
                        showFirstRun();
                    }
                }

            }
        }
    };

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
        } else if (resultCode == RESULT_SHOW_COURSE_MATERIALS) {
            // Result returned from course activity to show course materials

            // Get course code from course activity
            String courseCode = data.getStringExtra(EXTRA_COURSE_CODE);

            if (courseCode != null && !(courseCode.equals(""))) {
                // If course code is not empty, pass data to materials fragment and set it as query
                // Materials should have course code as HashTag in order to show course relevant materials

                mMaterialsFragment = MaterialsFragment.newInstanceWithQuery(courseCode, true);

                onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_materials));
                mNavigationView.setCheckedItem(R.id.nav_materials);
                mMaterialsFragment = new MaterialsFragment();
            }
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            signedIn = true;

            // If Google SignIn account doesn't return null, get account data
            if (acct != null) {
                // Set account name and email address
                mNameTextView.setText(acct.getDisplayName());
                mEmailTextView.setText(acct.getEmail());
                // Save account data in static strings and uri to make account data accessible from other activities
                MainActivity.sUserEmailAddress = acct.getEmail();
                MainActivity.sUserName = acct.getDisplayName();
                MainActivity.sUserProfileImage = acct.getPhotoUrl();
                // Set account profile picture if exists
                Uri uri = acct.getPhotoUrl();
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_profile_none)
                        // CircleImageView known to have issues with TransitionDrawable, use dontAnimate() as workaround
                        // .circleCrop() will be available in v4 of Glide, when it's available
                        // CircleImageView can be removed and replaced with this
                        .dontAnimate()
                        .into(mProfileImageView);

                // Use Google plus API to get user cover photo if exists
                Plus.PeopleApi.load(mGoogleApiClient, acct.getId()).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(@NonNull People.LoadPeopleResult peopleData) {
                        if (peopleData.getStatus().isSuccess()) {
                            PersonBuffer personBuffer = peopleData.getPersonBuffer();
                            if (personBuffer != null && personBuffer.getCount() > 0) {
                                Person person = personBuffer.get(0);
                                personBuffer.release();
                                if (person.getCover() != null) {
                                    Glide.with(MainActivity.this)
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
//        // SignOut from Google account *without* revoking app permission to SignIn
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
        // SignOut from Google account and revoke app permission to SignIn
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

    long lastPress;

    @Override
    public void onBackPressed() {
        // On back key press
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            // If navigation view is opened, close it
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Navigation view is closed, check if current fragment is course fragment, and change to it if it's not
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (currentFragment instanceof CoursesFragment) {
                // Current fragment is courses fragment, safe check before exiting the app,
                // back key should be pressed twice in a range of 3 seconds
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastPress > 3000) {
                    Toast.makeText(this, R.string.confirm_exit, Toast.LENGTH_SHORT).show();
                    lastPress = currentTime;
                } else {
                    super.onBackPressed();
                }
            } else {
                // Current fragment is not courses fragment, change back to it before exit
                mNavigationView.setNavigationItemSelectedListener(this);
                onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_courses));
                mNavigationView.setCheckedItem(R.id.nav_courses);
            }
        }
    }

    public void navHeaderClicked() {
        if (signedIn) {
            // Google account is connected, show logout alert dialog
            mDrawerLayout.closeDrawer(GravityCompat.START);
            new AlertDialog.Builder(this)
                    .setMessage(R.string.sign_out_confirm)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            revokeAccess();
                            MainActivity.sUserEmailAddress = null;
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
            // Google account is disconnected, initialize Google SignIn
            mDrawerLayout.closeDrawer(GravityCompat.START);
            signIn();
        }
    }

    public String debugInfo() {
        // Get debug info from the device for error report email and save it as string
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
        mSearchItem = menu.findItem(R.id.menu_main_searchView);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);

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

        MenuItem filterItem = menu.findItem(R.id.menu_main_filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
//                updateData();
                return true;
            }
        });

        return true;
    }

    public void updateData() {
        if (isConnected()) {
            // Update data if device is connected to network
            mUpdateProgressDialog.show();
            Intent updateServiceIntent = new Intent(MainActivity.this, UpdateIntentService.class);
            updateServiceIntent.setAction(UpdateIntentService.ACTION_CHECK_FOR_UPDATE);
            startService(updateServiceIntent);
        }
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
            if(mCourseFragment == null) {
                mCourseFragment = new CoursesFragment();
            }
            fragmentManager.beginTransaction().replace(R.id.content_frame, mCourseFragment).commit();
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
            if (mMeetupsFragment == null)
                mMeetupsFragment = new MeetupsFragment();

            fragmentManager.beginTransaction().replace(R.id.content_frame, mMeetupsFragment).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_discussions) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new DiscussionsFragment()).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_malshab) {
            if (mMalshabFragment == null)
                mMalshabFragment = new MalshabFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, mMalshabFragment).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_contact_us) {
            // Open mail intent, set email address, title and add attachment
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"marshaldevs@gmail.com" });
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_subject));
            Calendar now = Calendar.getInstance();
            // Create debug info text file and set file name to current date and time
            String filename = String.format(Locale.getDefault(),
                    "marshal_%02d%02d%04d_%02d%02d%02d.log",
                    now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH) + 1,
                    now.get(Calendar.YEAR), now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
            File tempFile = new File(getBaseContext().getExternalCacheDir() + File.separator + filename + ".txt") ;
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
            // Check if chrome custom tab preference is enabled
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

}