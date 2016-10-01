package com.basmapp.marshal.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.MalshabItem;
import com.basmapp.marshal.entities.MaterialItem;
import com.basmapp.marshal.interfaces.UpdateServiceListener;
import com.basmapp.marshal.localdb.LocalDBHelper;
import com.basmapp.marshal.receivers.UpdateBroadcastReceiver;
import com.basmapp.marshal.services.FcmRegistrationService;
import com.basmapp.marshal.services.UpdateIntentService;
import com.basmapp.marshal.ui.fragments.CoursesFragment;
import com.basmapp.marshal.ui.fragments.MalshabFragment;
import com.basmapp.marshal.ui.fragments.MaterialsFragment;
import com.basmapp.marshal.ui.fragments.MeetupsFragment;
import com.basmapp.marshal.ui.fragments.SubscriptionsFragment;
import com.basmapp.marshal.util.LocaleUtils;
import com.basmapp.marshal.util.glide.CircleTransform;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_IN = 9001;
    public static final int RC_COURSE_ACTIVITY = 8000;

    private GoogleApiClient mGoogleApiClient;
    //    private ProgressDialog mProgressDialog;
    private Toolbar mToolbar;
    public DrawerLayout mDrawerLayout;
    public static ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;
    private SearchView mSearchView;
    private SharedPreferences mSharedPreferences;
    private TextView mNameTextView, mEmailTextView;
    private ImageView mProfileImageView;
    private FrameLayout mNavHeaderFrame;
    private ImageView mCoverImageView;
    private boolean signedIn = false;
    private MenuItem mSearchItem;
    private Snackbar mNetworkSnackbar;

    // Fragments
    private CoursesFragment mCourseFragment;
    private MaterialsFragment mMaterialsFragment;
    private MalshabFragment mMalshabFragment;
    private MeetupsFragment mMeetupsFragment;
    private SubscriptionsFragment mSubscriptionsFragment;

    private UpdateBroadcastReceiver updateReceiver;

    private ProgressDialog mUpdateProgressDialog;

    public static int sLastCoursesViewPagerIndex = 4;

    public static ArrayList<Course> sAllCourses;
    public static ArrayList<Course> sViewPagerCourses;
    public static ArrayList<Course> sMeetups;
    public static ArrayList<Course> sMyCourses;
    public static ArrayList<MaterialItem> sMaterialItems;
    public static ArrayList<MalshabItem> sMalshabItems;

    public static String sUserEmailAddress;
    public static String sUserName;
    public static Uri sUserProfileImage;

    private FrameLayout sNewUpdatesButton;
    private LinearLayout sErrorScreen;

    public static boolean needRecreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("LIFE_CYCLE", "onCreate");
        super.onCreate(savedInstanceState);

//        checkPlayServicesAvailability();
        checkFcmRegistrationState();

        // enable on final release to disable screenshots and more
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        // translucent navigation bar
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_main);

        // Initialize shared preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (mSharedPreferences != null) {
            if (mSharedPreferences.getBoolean(Constants.PREF_SHOW_WARM_WELCOME, true)) {
                startActivity(new Intent(this, WelcomeActivity.class));
            }
        }

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        // Set course fragment as main fragment
        onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_courses));
        mNavigationView.setCheckedItem(R.id.nav_courses);

        initializeUpdateProgressBar();

        initializeNewUpdatesButton();

        // Initialize navigation view header items
        mNameTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.display_name);
        mEmailTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.account_name);
        mCoverImageView = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.cover_photo);
        mProfileImageView = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.avatar);
        mNavHeaderFrame = (FrameLayout) mNavigationView.getHeaderView(0).findViewById(R.id.account_info_container);
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

        if (mSharedPreferences != null) {
            if (mSharedPreferences.getBoolean(Constants.PREF_IS_FIRST_RUN, true)) {
                // Show update progress bar on first app startup
                mUpdateProgressDialog.show();
            }
        }

        // Initialize error screen re-try button
        Button retryButton = (Button) findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
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

        if (mSharedPreferences.getBoolean(Constants.PREF_MUST_UPDATE, false)) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setCancelable(false);
            dialogBuilder.setIcon(R.drawable.ic_drawer_info);
            dialogBuilder.setMessage(getString(R.string.app_upgrade_available));
            dialogBuilder.setPositiveButton(R.string.btn_play_store, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mSharedPreferences.edit().putBoolean(Constants.PREF_MUST_UPDATE, false).apply();
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
            });
            dialogBuilder.show();
        }
    }

    private void setErrorScreenVisibility(int visibility) {
        // Initialize error screen and set viability based on integer
        if (sErrorScreen == null)
            sErrorScreen = (LinearLayout) findViewById(R.id.placeholder_error);
        if (visibility != sErrorScreen.getVisibility()) sErrorScreen.setVisibility(visibility);
    }

    private void showNewUpdatesButton() {
        if (sNewUpdatesButton != null) {
            animateNewUpdatesButton(View.VISIBLE);
        } else {
            initializeNewUpdatesButton();
            animateNewUpdatesButton(View.VISIBLE);
        }
    }

    private void initializeNewUpdatesButton() {
        sNewUpdatesButton = (FrameLayout) findViewById(R.id.new_updates_button);
        sNewUpdatesButton.setOnClickListener(new View.OnClickListener() {
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
                mMeetupsFragment = new MeetupsFragment();
                mSubscriptionsFragment = new SubscriptionsFragment();
            }
        });
    }

    private void animateNewUpdatesButton(final int visibility) {
        Animation animation;
        if (visibility == View.VISIBLE) {
            // show in animation on visible
            animation = AnimationUtils.loadAnimation(this, R.anim.new_updates_banner_in);
        } else {
            // show out animation on invisible
            animation = AnimationUtils.loadAnimation(this, R.anim.new_updates_banner_out);
        }
        sNewUpdatesButton.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                sNewUpdatesButton.setVisibility(visibility);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void checkFcmRegistrationState() {
        if (!FcmRegistrationService.isDeviceRegistered(this)) {
            Intent intent = new Intent(this, FcmRegistrationService.class);
            intent.setAction(FcmRegistrationService.ACTION_REGISTER_NEW);
            startService(intent);
        }
    }

    private void initializeUpdateProgressBar() {
        mUpdateProgressDialog = new ProgressDialog(MainActivity.this);
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

    private void showFirstRun() {
        // Change shared preference value to false so next startup will not be called as first
        mSharedPreferences.edit().putBoolean(Constants.PREF_IS_FIRST_RUN, false).apply();
        // Restart app fragments, set course fragment as default and dismiss error screen
        releaseAllDataLists();
        mCourseFragment = null;
        mMaterialsFragment = null;
        mMalshabFragment = null;
        mMeetupsFragment = null;
        mSubscriptionsFragment = null;

        setErrorScreenVisibility(View.GONE);
        onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_courses));
        mNavigationView.setCheckedItem(R.id.nav_courses);
    }

    private void releaseAllDataLists() {
        sAllCourses = null;
        sViewPagerCourses = null;
        sMeetups = null;
        sMyCourses = null;
        sMaterialItems = null;
        sMalshabItems = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("LIFE_CYCLE", "onStart");

        if (needRecreate) {
            needRecreate = false;
            getSupportFragmentManager().beginTransaction().remove(mCourseFragment).commit();
            recreate();
        }

        // Register update data from server broadcast and check internet connection broadcast
        registerInternetCheckReceiver();
        registerUpdateReceiver();

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
    protected void onResume() {
        super.onResume();
        Log.i("LIFE_CYCLE", "onResume");

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
        Log.i("LIFE_CYCLE", "onPause");
        // Cancel no network Snackbar
        if (mNetworkSnackbar != null)
            mNetworkSnackbar.dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("LIFE_CYCLE", "onStop");
        // Unregister update data from server broadcast and check internet connection broadcast
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(updateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("LIFE_CYCLE", "onDestroy");
        // Set viewpager page to start when app is closed
        sLastCoursesViewPagerIndex = 4;
        // Close db if exist when app is closed
        LocalDBHelper.closeIfExist();
    }


    private boolean isUpdateIntentServiceRunning() {
//        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if ("com.basmapp.marshal.UpdateIntentService".equals(service.service.getClassName())) {
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
                // There is no internet connection, show error Snackbar
                mNetworkSnackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.offline_snackbar_network_unavailable, Snackbar.LENGTH_LONG);
                mNetworkSnackbar.setAction(R.string.offline_snackbar_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isConnected()) mNetworkSnackbar.dismiss();
                        else onReceive(context, intent);
                    }
                });
//                mNetworkSnackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_light));
                mNetworkSnackbar.setDuration(10000);
                mNetworkSnackbar.show();

                // As there is no internet connection, dismiss update dialog if showed and show error screen
                if (mUpdateProgressDialog != null && mUpdateProgressDialog.isShowing())
                    mUpdateProgressDialog.dismiss();

                if (mSharedPreferences == null)
                    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                if (mSharedPreferences.getBoolean(Constants.PREF_IS_FIRST_RUN, true)) {
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
//                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .addApi(Plus.API)
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
                        .placeholder(R.drawable.ic_default_avatar)
                        .transform(new CircleTransform(this))
                        .into(mProfileImageView);

                // Use Google plus API to get user cover photo if exists
//                Plus.PeopleApi.load(mGoogleApiClient, acct.getId()).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
//                    @Override
//                    public void onResult(@NonNull People.LoadPeopleResult peopleData) {
//                        if (peopleData.getStatus().isSuccess()) {
//                            PersonBuffer personBuffer = peopleData.getPersonBuffer();
//                            if (personBuffer != null && personBuffer.getCount() > 0) {
//                                Person person = personBuffer.get(0);
//                                personBuffer.release();
//                                if (person.getCover() != null) {
//                                    Glide.with(MainActivity.this)
//                                            .load(person.getCover().getCoverPhoto().getUrl())
//                                            .placeholder(R.drawable.bg_default_profile_art)
//                                            .into(mCoverImageView);
//                                }
//                            } // else Log.e(TAG, "Plus response was empty! Failed to load profile.");
//                        } // else Log.e(TAG, "Failed to load plus proflie, error " + loadPeopleResult.getStatus().getStatusCode());
//                    }
//                });
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

    private void signOut() {
        // SignOut from Google account *without* revoking app permission to SignIn
//        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // [START_EXCLUDE]
                        signedIn = false;
                        // [END_EXCLUDE]
                    }
                });
    }

    private void revokeAccess() {
        // SignOut from Google account and revoke app permission to SignIn
//        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
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
        if (signedIn && mGoogleApiClient.isConnected()) {
            // Google account is connected, show logout alert dialog
            mDrawerLayout.closeDrawer(GravityCompat.START);
            new AlertDialog.Builder(this)
                    .setMessage(R.string.sign_out_confirm)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            signOut();
                            sUserEmailAddress = null;
                            sUserName = null;
                            sUserProfileImage = null;
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

    public static int getPrimaryColorCode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() + "_preferences", MODE_PRIVATE);
        int defaultColor = ContextCompat.getColor(context.getApplicationContext(), R.color.blue_primary_color);
        return prefs.getInt(Constants.PREF_PRIMARY_COLOR_CODE, defaultColor);
    }

    public static int getAccentColorCode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() + "_preferences", MODE_PRIVATE);
        int defaultColor = ContextCompat.getColor(context.getApplicationContext(), R.color.red_accent_color);
        return prefs.getInt(Constants.PREF_ACCENT_COLOR_CODE, defaultColor);
    }

    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    public static float px2dp(float px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, Resources.getSystem().getDisplayMetrics());
    }

    private TextView setBadge(int menuItemId) {
        TextView badge = (TextView)
                MenuItemCompat.getActionView(mNavigationView.getMenu().findItem(menuItemId));
        badge.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        badge.setBackgroundResource(R.drawable.new_promo_background);
        badge.setEllipsize(TextUtils.TruncateAt.END);
        badge.setGravity(Gravity.CENTER_VERTICAL);
        badge.setMaxLines(1);
        badge.setPaddingRelative(dp2px(4), dp2px(2), dp2px(4), dp2px(2));
        badge.setText("99+");
        badge.setAllCaps(true);
        badge.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        return badge;
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
            if (mCourseFragment == null) {
                mCourseFragment = new CoursesFragment();
            }
            fragmentManager.beginTransaction().replace(R.id.content_frame, mCourseFragment).commit();
            setTitle(item.getTitle());
//            setBadge(item.getItemId());
        } else if (id == R.id.nav_subscriptions) {
            if (mSubscriptionsFragment == null) {
                mSubscriptionsFragment = new SubscriptionsFragment();
            }
            fragmentManager.beginTransaction().replace(R.id.content_frame, mSubscriptionsFragment).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_materials) {
            if (mMaterialsFragment == null) {
                mMaterialsFragment = new MaterialsFragment();
            }
            fragmentManager.beginTransaction().replace(R.id.content_frame, mMaterialsFragment).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_meetups) {
            if (mMeetupsFragment == null)
                mMeetupsFragment = new MeetupsFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, mMeetupsFragment).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_malshab) {
            if (mMalshabFragment == null)
                mMalshabFragment = new MalshabFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, mMalshabFragment).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            if (LocaleUtils.isRtl()) {
                overridePendingTransition(R.anim.activity_open_enter_rtl,
                        R.anim.activity_open_exit);
            } else {
                overridePendingTransition(R.anim.activity_open_enter,
                        R.anim.activity_open_exit);
            }
        } else if (id == R.id.nav_contact_us) {
            startActivity(new Intent(this, DescribeProblemActivity.class));
            if (LocaleUtils.isRtl()) {
                overridePendingTransition(R.anim.activity_open_enter_rtl,
                        R.anim.activity_open_exit);
            } else {
                overridePendingTransition(R.anim.activity_open_enter,
                        R.anim.activity_open_exit);
            }
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
            if (LocaleUtils.isRtl()) {
                overridePendingTransition(R.anim.activity_open_enter_rtl,
                        R.anim.activity_open_exit);
            } else {
                overridePendingTransition(R.anim.activity_open_enter,
                        R.anim.activity_open_exit);
            }
        }
        if (mDrawerLayout != null) mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}