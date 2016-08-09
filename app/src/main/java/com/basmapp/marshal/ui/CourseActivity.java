package com.basmapp.marshal.ui;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.entities.MaterialItem;
import com.basmapp.marshal.entities.Rating;
import com.basmapp.marshal.localdb.DBConstants;
import com.basmapp.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmapp.marshal.services.UpdateIntentService;
import com.basmapp.marshal.ui.adapters.CoursesRecyclerAdapter;
import com.basmapp.marshal.ui.fragments.CyclesBottomSheetDialogFragment;
import com.basmapp.marshal.ui.utils.ColorUtils;
import com.basmapp.marshal.ui.utils.LocaleUtils;
import com.basmapp.marshal.ui.utils.ThemeUtils;
import com.basmapp.marshal.utils.DateHelper;
import com.basmapp.marshal.utils.HashUtil;
import com.basmapp.marshal.utils.MarshalServiceProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Response;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class CourseActivity extends AppCompatActivity {

    private static final String FAB_SHOWCASE_ID = "cycle_fab_tutorial";

    private Toolbar mToolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private Course mCourse;
    private Rating mUserRating;

    private TextView mTextViewCourseName;
    private TextView mTextViewCourseCategory;
    private TextView mTextViewCourseCode;
    private TextView mTextViewGeneralDescription;
    private TextView mTextViewSyllabus;
    private TextView mTextViewPrerequisites;
    private TextView mTextViewTargetPopulation;
    private TextView mTextViewDayTime;
    private TextView mTextViewDaysDuration;
    private TextView mTextViewHoursDuration;
    private TextView mTextViewComments;
    private ImageView mHeader;
    private TextView mTextViewRatingAverage;
    private TextView mTextViewRatingsAmount;
    private TextView mTextViewReviewHint;
    private TextView mTextViewReviewDate;
    private TextView mTextViewReviewText;
    private TextView mTextViewReviewEdited;
    private TextView mReviewAuthor;
    private RatingBar mReviewRating;
    private RatingBar mRatingBarAverage;
    private RatingBar mRatingBarUser;
    private RelativeLayout mReviewItemContainer;
    private LinearLayout mActionContainer;
    private LinearLayout mMaterialsButton;
    private LinearLayout mShareButton;
    private Button mBtnReadAllReviews;
    private CircleImageView mProfileImageView;
    private CircleImageView mReviewProfileImageView;

    private int contentColor = -1;
    private int scrimColor = -1;
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    private FloatingActionButton mFabCycles;
    private RatingBar.OnRatingBarChangeListener mRatingBarUserOnChangeListener;
    private LinearLayout mRatingsFrame;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        LocaleUtils.updateLocale(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.course_transition));
            getWindow().setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.course_transition));
        }

        setContentView(R.layout.activity_course);

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

        mCourse = getIntent().getParcelableExtra(Constants.EXTRA_COURSE);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean courseShared = mSharedPreferences.getBoolean("courseShared", false);
                if (courseShared) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mFabCycles.animate().cancel();
                        mFabCycles.animate()
                                .scaleX(0f)
                                .scaleY(0f)
                                .alpha(0f)
                                .setDuration(200)
                                .setInterpolator(new FastOutLinearInInterpolator())
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mToolbar.setVisibility(View.GONE);
                                        supportFinishAfterTransition();
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {
                                    }
                                });
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            }
        });

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        // hide toolbar expanded title
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));

        //Initialize Cycles FAB
        mFabCycles = (FloatingActionButton) findViewById(R.id.course_activity_fab_cycles);

        //Initialize Shared Preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (mCourse != null) {

            if (mCourse.getCycles() == null || mCourse.getCycles().size() == 0) {
                mFabCycles.setVisibility(View.GONE);
            } else {
                // Initialize Cycles FAB onClick event
                mFabCycles.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<Cycle> cycles = new ArrayList<>(mCourse.getCycles());

                        for (int index = 0; index < cycles.size(); index++) {
                            if (cycles.get(index).getStartDate() == null || cycles.get(index).getEndDate() == null) {
                                cycles.remove(cycles.get(index));
                            }
                        }

                        if (cycles.size() > 0) {
                            CyclesBottomSheetDialogFragment bottomSheet =
                                    CyclesBottomSheetDialogFragment.newInstance(mCourse);
                            bottomSheet.show(getSupportFragmentManager(), "CyclesBottomSheet");
                        } else {
                            Toast.makeText(CourseActivity.this,
                                    getResources().getString(R.string.course_no_cycles_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Transition sharedElementEnterTransition = getWindow().getSharedElementEnterTransition();
                    sharedElementEnterTransition.addListener(new Transition.TransitionListener() {
                        @Override
                        public void onTransitionStart(Transition transition) {
                        }

                        @Override
                        public void onTransitionEnd(Transition transition) {
                            new MaterialShowcaseView.Builder(CourseActivity.this)
                                    .setTarget(mFabCycles)
                                    .setShapePadding(48)
                                    .setDismissText(R.string.got_it)
                                    .setDismissOnTouch(false)
                                    .setDismissOnTargetTouch(true)
                                    .setTargetTouchable(true)
                                    .setTitleText(R.string.cycle_fab_tutorial_description)
//                            .setMaskColour(Color.argb(210, 0, 0, 0))
                                    .singleUse(FAB_SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                                    .show();
                        }

                        @Override
                        public void onTransitionCancel(Transition transition) {
                        }

                        @Override
                        public void onTransitionPause(Transition transition) {
                        }

                        @Override
                        public void onTransitionResume(Transition transition) {
                        }
                    });
                } else {
                    new MaterialShowcaseView.Builder(CourseActivity.this)
                            .setTarget(mFabCycles)
                            .setShapePadding(48)
                            .setDismissText(R.string.got_it)
                            .setDismissOnTouch(false)
                            .setDismissOnTargetTouch(true)
                            .setTargetTouchable(true)
                            .setTitleText(R.string.cycle_fab_tutorial_description)
//                            .setMaskColour(Color.argb(210, 0, 0, 0))
                            .singleUse(FAB_SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                            .show();
                }
            }

            onSecondaryActionsClick();

            // Set the course title
            collapsingToolbarLayout.setTitle(mCourse.getName());

            mTextViewCourseName = (TextView) findViewById(R.id.course_content_textView_courseName);
            mTextViewCourseCategory = (TextView) findViewById(R.id.course_content_textView_courseCategory);
            mTextViewCourseCode = (TextView) findViewById(R.id.course_content_textView_courseCode);
            mTextViewGeneralDescription = (TextView) findViewById(R.id.course_content_textView_description);
            mTextViewSyllabus = (TextView) findViewById(R.id.course_content_textView_syllabus);
            mTextViewPrerequisites = (TextView) findViewById(R.id.course_content_textView_prerequisites);
            mTextViewTargetPopulation = (TextView) findViewById(R.id.course_content_textView_targetPopulation);
            mTextViewDayTime = (TextView) findViewById(R.id.course_content_textView_dayTime);
            mTextViewDaysDuration = (TextView) findViewById(R.id.course_content_textView_daysDuration);
            mTextViewHoursDuration = (TextView) findViewById(R.id.course_content_textView_hoursDuration);
            mTextViewComments = (TextView) findViewById(R.id.course_content_textView_comments);
            mTextViewReviewHint = (TextView) findViewById(R.id.review_hint);
            mReviewProfileImageView = (CircleImageView) findViewById(R.id.user_profile_image);

            // Set the course photo
            mHeader = (ImageView) findViewById(R.id.header);

            Glide.with(this)
                    .load(mCourse.getImageUrl())
                    .asBitmap()
                    .into(new BitmapImageViewTarget(mHeader) {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                            super.onResourceReady(bitmap, glideAnimation);
                            final Bitmap bitmapDrawable = ((BitmapDrawable) mHeader.getDrawable()).getBitmap();
                            Palette.from(bitmapDrawable).generate(new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette palette) {
                                    contentColor = palette.getMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                                    scrimColor = ContextCompat.getColor(getApplicationContext(), R.color.black_trans80);
                                    collapsingToolbarLayout.setStatusBarScrimColor(scrimColor);
                                    collapsingToolbarLayout.setContentScrimColor(contentColor);
                                }
                            });

                            final int twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                    24, CourseActivity.this.getResources().getDisplayMetrics());
                            Palette.from(bitmap)
                                    .maximumColorCount(3)
                                    .clearFilters() /* by default palette ignore certain hues
                                    (e.g. pure black/white) but we don't want this. */
                                    .setRegion(0, 0, bitmap.getWidth() - 1, twentyFourDip) /* - 1 to work around
                                    https://code.google.com/p/android/issues/detail?id=191013 */
                                    .generate(new Palette.PaletteAsyncListener() {
                                        @Override
                                        public void onGenerated(Palette palette) {
                                            boolean isDark;
                                            @ColorUtils.Lightness int lightness = ColorUtils.isDark(palette);
                                            if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
                                                isDark = ColorUtils.isDark(bitmapDrawable, bitmapDrawable.getWidth() / 2, 0);
                                            } else {
                                                isDark = lightness == ColorUtils.IS_DARK;
                                            }

                                            if (!isDark) { // make toolbar icons and title dark on light images
                                                final PorterDuffColorFilter colorFilter
                                                        = new PorterDuffColorFilter(ContextCompat.getColor(
                                                        CourseActivity.this, R.color.dark_icon), PorterDuff.Mode.MULTIPLY);

                                                // Change the color of the navigation icon
                                                Drawable navigationIcon = mToolbar.getNavigationIcon();
                                                if (navigationIcon != null) {
                                                    navigationIcon.setColorFilter(colorFilter);
                                                    mToolbar.setNavigationIcon(navigationIcon);
                                                }

                                                // Change the color of the overflow icon
                                                Drawable overflowIcon = mToolbar.getOverflowIcon();
                                                if (overflowIcon != null) {
                                                    overflowIcon.setColorFilter(colorFilter);
                                                    mToolbar.setOverflowIcon(overflowIcon);
                                                }

                                                // Change the color of the title
                                                collapsingToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(
                                                        getApplicationContext(), android.R.color.primary_text_light));
                                            }

                                            // color the status bar. Set a semi transparent dark color on L,
                                            // light or dark color on M (with matching status bar icons)
                                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP && !isDark) {
                                                getWindow().setStatusBarColor(ContextCompat.getColor(
                                                        getApplicationContext(), R.color.black_trans80));
                                            }
                                            final Palette.Swatch topColor =
                                                    ColorUtils.getMostPopulousSwatch(palette);
                                            if (topColor != null &&
                                                    (isDark || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                                                ColorUtils.scrimify(topColor.getRgb(),
                                                        isDark, SCRIM_ADJUSTMENT);
                                                // set a light status bar on M+
                                                if (!isDark && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    setLightStatusBar(mHeader);
                                                }
                                            }
                                        }
                                    });
                        }
                    });
            initializeTextViews();
        }

        mReviewItemContainer = (RelativeLayout) findViewById(R.id.review_item_container);
        mActionContainer = (LinearLayout) findViewById(R.id.action_container);
        mReviewRating = (RatingBar) findViewById(R.id.review_rating);
        mRatingBarAverage = (RatingBar) findViewById(R.id.summary_rating_bar);
        mRatingBarUser = (RatingBar) findViewById(R.id.course_content_ratingBar_user);
        mTextViewReviewHint = (TextView) findViewById(R.id.review_hint);
        mTextViewReviewDate = (TextView) findViewById(R.id.review_date);
        mTextViewReviewText = (TextView) findViewById(R.id.review_text);
        mTextViewReviewEdited = (TextView) findViewById(R.id.review_edited);
        mTextViewRatingsAmount = (TextView) findViewById(R.id.course_content_textView_ratingsAmount);
        mTextViewRatingAverage = (TextView) findViewById(R.id.course_content_textView_average_value);
        mReviewAuthor = (TextView) findViewById(R.id.review_author);
        mBtnReadAllReviews = (Button) findViewById(R.id.course_content_button_readAllReviews);
        mRatingsFrame = (LinearLayout) findViewById(R.id.course_content_ratingsFrame);

        if (mCourse != null) {
            setRatingViewsVisibility(View.VISIBLE);
            showRatingAverage();
            showRatingsCount();
            showUserRating();

            mBtnReadAllReviews.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mRatingBarAverage.getRating() == 0) {
                        Toast.makeText(CourseActivity.this, R.string.no_reviews_error, Toast.LENGTH_LONG).show();
                    } else {
                        Intent i = new Intent(CourseActivity.this, RatingsActivity.class);
                        i.putExtra(Constants.EXTRA_COURSE, mCourse);
                        i.putExtra(Constants.EXTRA_RATING_AMOUNT, mTextViewRatingsAmount.getText().toString());
                        i.putExtra(Constants.EXTRA_RATING_BAR_STARS, mRatingBarAverage.getRating());
                        i.putExtra(Constants.EXTRA_RATING_AVERAGE, mTextViewRatingAverage.getText().toString());
                        startActivity(i);
                    }
                }
            });

            mRatingBarUserOnChangeListener = new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (MainActivity.sUserEmailAddress != null && ratingBar.getRating() != 0) {
                        showReviewCommentDialog(false);
                    } else {
                        if (ratingBar.getRating() != 0) {
                            Toast.makeText(CourseActivity.this, R.string.please_log_in, Toast.LENGTH_SHORT).show();
                        }
                        ratingBar.setRating(0);
                    }
                }
            };

            mRatingBarUser.setOnRatingBarChangeListener(mRatingBarUserOnChangeListener);
            mActionContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showReviewCommentDialog(true);
                }
            });
        }
    }

    private void setRatingViewsVisibility(int visibility) {
        if (mRatingsFrame != null) {
            mRatingsFrame.setVisibility(visibility);
        }
    }

    private void showUserRating() {
        if (MainActivity.sUserEmailAddress != null) {
            Rating.queryInBackground(Rating.class, CourseActivity.this, false,
                    new String[]{DBConstants.COL_COURSE_CODE, DBConstants.COL_USER_MAIL_ADDRESS},
                    new String[]{mCourse.getCourseCode(), HashUtil.SHA(MainActivity.sUserEmailAddress)},
                    new BackgroundTaskCallBack() {
                        @Override
                        public void onSuccess(String result, List<Object> data) {

                            if (data != null && data.size() > 0) {

                                mRatingBarUser.setVisibility(View.GONE);
                                mReviewItemContainer.setVisibility(View.VISIBLE);
                                mReviewAuthor.setText(MainActivity.sUserName);
                                Uri uri = MainActivity.sUserProfileImage;
                                Glide.with(CourseActivity.this)
                                        .load(uri)
                                        .placeholder(R.drawable.ic_profile_none)
                                        .dontAnimate()
                                        .into(mReviewProfileImageView);
                                mTextViewReviewHint.setVisibility(View.GONE);
                                mTextViewReviewText.setText(((Rating)(data.get(0))).getComment());
                                try {
                                    mTextViewReviewDate.setVisibility(View.VISIBLE);
                                    mTextViewReviewDate.setText(DateHelper.dateToString(((Rating)(data.get(0))).getLastModified()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                mReviewRating.setRating((float) ((Rating)(data.get(0))).getRating());
                                mUserRating = (Rating)data.get(0);
                                if (mUserRating.getCreatedAt().before(mUserRating.getLastModified())) {
                                    mTextViewReviewEdited.setVisibility(View.VISIBLE);
                                }
                            } else {
                                initializeRatingViews();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            initializeRatingViews();
                        }
                    });
        } else {
            initializeRatingViews();
        }
    }

    private void showRatingsCount() {
        Rating.countByColumnInBackground(Rating.class, CourseActivity.this,
                false, DBConstants.COL_COURSE_CODE, mCourse.getCourseCode(), new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        if (data != null && data.size() > 0) {
                            try {
                                mTextViewRatingsAmount.setText(String.valueOf(data.get(0)));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {

                    }
                });
    }

    private void showRatingAverage() {
        Rating.getAverageByColumnInBackground(Rating.class, CourseActivity.this, false,
                DBConstants.COL_RATING, DBConstants.COL_COURSE_CODE, mCourse.getCourseCode(),
                new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        if (data != null && data.size() > 0) {
                            try {
                                mTextViewRatingAverage.setText(String.valueOf(data.get(0)).substring(0,3));
                                mRatingBarAverage.setRating((Float) data.get(0));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {

                    }
                });
    }

    private void showReviewCommentDialog(Boolean isEditMode) {

        final AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.Rating_DialogAlert).create();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View dialogView = layoutInflater.inflate(R.layout.rate_review_dialog, null);
        alertDialog.setView(dialogView);

        mProfileImageView = (CircleImageView) dialogView.findViewById(R.id.user_profile_image);
        TextView reviewBy = (TextView) dialogView.findViewById(R.id.review_by);
        String userName = String.format(getString(R.string.review_by), MainActivity.sUserName);
        reviewBy.setText(userName);

        Uri uri = MainActivity.sUserProfileImage;
        Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_profile_none)
                .into(mProfileImageView);

        final RatingBar ratingBar = (RatingBar) dialogView.findViewById(R.id.course_content_ratingBar_user);

        final EditText input = (EditText) dialogView.findViewById(R.id.review_comment);

        final Button negativeButton = (Button) dialogView.findViewById(R.id.negative_button);
        final Button positiveButton = (Button) dialogView.findViewById(R.id.positive_button);

        TextInputLayout inputLayout = (TextInputLayout) dialogView.findViewById(R.id.inputLayout);

        final TextView textView = (TextView) dialogView.findViewById(R.id.item_title);
        textView.setTextColor(contentColor);

        if (mRatingBarUser.getRating() == 1) {
            textView.setText(getString(R.string.review_dialog_poor));
        }
        if (mRatingBarUser.getRating() == 2) {
            textView.setText(getString(R.string.review_dialog_below_average));
        }
        if (mRatingBarUser.getRating() == 3) {
            textView.setText(getString(R.string.review_dialog_average));
        }
        if (mRatingBarUser.getRating() == 4) {
            textView.setText(getString(R.string.review_dialog_above_average));
        }
        if (mRatingBarUser.getRating() == 5) {
            textView.setText(getString(R.string.review_dialog_excellent));
        }

        if (!isEditMode) {
            inputLayout.setError(getString(R.string.public_reviews_message)); // show error
            negativeButton.setVisibility(View.GONE);
            positiveButton.setText(getString(R.string.structured_review_question_submit));

            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mRatingBarUser.setRating(0);
                }
            });

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String emailHash;
                    Rating tempRating;

                    try {
                        emailHash = HashUtil.SHA(MainActivity.sUserEmailAddress);
                        tempRating = new Rating(CourseActivity.this);
                        tempRating.setComment(input.getText().toString());
                        tempRating.setRating(mRatingBarUser.getRating());
                        tempRating.setUserMailAddress(emailHash);
                        tempRating.setPlainMailAddress(MainActivity.sUserEmailAddress);
                        tempRating.setCourseCode(mCourse.getCourseCode());
                        tempRating.setCreatedAt(new Date());
                        tempRating.setLastModified(new Date());
                        new SendRatingRequest(SendRatingRequest.REQUEST_TYPE_POST, tempRating, new BackgroundTaskCallBack() {
                            @Override
                            public void onSuccess(String result, List<Object> data) {
                                // Simulate showing user review
                                mTextViewReviewHint.setVisibility(View.GONE);
                                mRatingBarUser.setVisibility(View.GONE);
                                mReviewItemContainer.setVisibility(View.VISIBLE);
                                mReviewAuthor.setText(MainActivity.sUserName);
                                Uri uri = MainActivity.sUserProfileImage;
                                Glide.with(CourseActivity.this)
                                        .load(uri)
                                        .placeholder(R.drawable.ic_profile_none)
                                        .into(mReviewProfileImageView);
                                mTextViewReviewEdited.setVisibility(View.GONE);

                                mReviewRating.setRating(mRatingBarUser.getRating());
                                try {
                                    mTextViewReviewDate.setVisibility(View.VISIBLE);
                                    mTextViewReviewDate.setText(DateHelper.dateToString(mUserRating.getLastModified()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                mTextViewReviewText.setText(input.getText().toString());

                                showRatingAverage();
                                showRatingsCount();
                                showUserRating();

                                // Send broadcast for update the rating on the CardView
                                Intent intent = new Intent(CoursesRecyclerAdapter.ACTION_ITEM_DATA_CHANGED);
                                sendBroadcast(intent);

                                Toast.makeText(CourseActivity.this, R.string.review_feedback_posted, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(String error) {

                            }
                        }).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    alertDialog.dismiss();
                }
            });
        } else {
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText(getString(R.string.delete_review));
            positiveButton.setVisibility(View.VISIBLE);
            positiveButton.setText(getString(R.string.save_review));

            ratingBar.setVisibility(View.VISIBLE);
            ratingBar.setRating(mReviewRating.getRating());
            input.setText(mTextViewReviewText.getText().toString());

            if (mReviewRating.getRating() == 1) {
                textView.setText(getString(R.string.review_dialog_poor));
            }
            if (mReviewRating.getRating() == 2) {
                textView.setText(getString(R.string.review_dialog_below_average));
            }
            if (mReviewRating.getRating() == 3) {
                textView.setText(getString(R.string.review_dialog_average));
            }
            if (mReviewRating.getRating() == 4) {
                textView.setText(getString(R.string.review_dialog_above_average));
            }
            if (mReviewRating.getRating() == 5) {
                textView.setText(getString(R.string.review_dialog_excellent));
            }

            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (ratingBar.getRating() == 0) {
                        ratingBar.setRating(1);
                    }
                    if (ratingBar.getRating() == 1) {
                        textView.setText(getString(R.string.review_dialog_poor));
                    }
                    if (ratingBar.getRating() == 2) {
                        textView.setText(getString(R.string.review_dialog_below_average));
                    }
                    if (ratingBar.getRating() == 3) {
                        textView.setText(getString(R.string.review_dialog_average));
                    }
                    if (ratingBar.getRating() == 4) {
                        textView.setText(getString(R.string.review_dialog_above_average));
                    }
                    if (ratingBar.getRating() == 5) {
                        textView.setText(getString(R.string.review_dialog_excellent));
                    }
                }
            });

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mUserRating != null) {
                        Rating tempRating = mUserRating;
                        tempRating.setRating(ratingBar.getRating());
                        tempRating.setComment(input.getText().toString());
                        tempRating.setLastModified(new Date());
                        new SendRatingRequest(SendRatingRequest.REQUEST_TYPE_PUT, tempRating).execute();
                    } else {
                        Toast.makeText(CourseActivity.this, R.string.review_delete_retry, Toast.LENGTH_LONG).show();
                    }
                    alertDialog.dismiss();
                }
            });

            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mUserRating != null) {
                        new SendRatingRequest(SendRatingRequest.REQUEST_TYPE_DELETE, mUserRating).execute();
                    } else {
                        Toast.makeText(CourseActivity.this, R.string.review_delete_retry, Toast.LENGTH_LONG).show();
                    }
                    alertDialog.dismiss();
                }
            });
        }
        alertDialog.show();
}

    private void setLightStatusBar(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    private void initializeRatingViews() {
        mTextViewReviewHint.setVisibility(View.VISIBLE);
        mReviewItemContainer.setVisibility(View.GONE);
        mRatingBarUser.setVisibility(View.VISIBLE);
        mRatingBarUser.setRating(0);
        mRatingBarUser.setIsIndicator(false);
        mRatingBarUser.setOnRatingBarChangeListener(mRatingBarUserOnChangeListener);
    }

    private void clearLightStatusBar(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    private void initializeTextViews() {

        boolean isAnyDataExist = false;

        if (mCourse.getIsMeetup()) {
            mTextViewReviewHint.setText(getString(R.string.rate_this_meetup));
        } else {
            mTextViewReviewHint.setText(getString(R.string.rate_this_course));
        }

        // Set course's Name
        if ((mCourse.getName() != null) &&
                (!mCourse.getName().equals(""))) {
            mTextViewCourseName.setText(mCourse.getName());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_name).setVisibility(View.GONE);
        }

        // Set course's Category
        if ((mCourse.getCategory() != null)) {
            if (Objects.equals(mCourse.getCategory(), "software"))
                mTextViewCourseCategory.setText(getString(R.string.course_type_software));
            if (Objects.equals(mCourse.getCategory(), "cyber"))
                mTextViewCourseCategory.setText(getString(R.string.course_type_cyber));
            if (Objects.equals(mCourse.getCategory(), "it"))
                mTextViewCourseCategory.setText(getString(R.string.course_type_it));
            if (Objects.equals(mCourse.getCategory(), "tools"))
                mTextViewCourseCategory.setText(getString(R.string.course_type_tools));
            if (Objects.equals(mCourse.getCategory(), "system"))
                mTextViewCourseCategory.setText(getString(R.string.course_type_system));
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_category).setVisibility(View.GONE);
        }

        // Set course's Code
        if ((mCourse.getCourseCode() != null) &&
                (!mCourse.getCourseCode().equals(""))) {
            mTextViewCourseCode.setText(mCourse.getCourseCode());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_code).setVisibility(View.GONE);
        }

        // Set course's Description
        if ((mCourse.getDescription() != null) &&
                (!mCourse.getDescription().equals(""))) {
            mTextViewGeneralDescription.setText(mCourse.getDescription());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_description).setVisibility(View.GONE);
        }

        // Set course's Syllabus
        if ((mCourse.getSyllabus() != null) &&
                (!mCourse.getSyllabus().equals(""))) {
            mTextViewSyllabus.setText(mCourse.getSyllabus());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_syllabus).setVisibility(View.GONE);
        }

        // Set course's prerequisites
        if ((mCourse.getPrerequisites() != null) &&
                (!mCourse.getPrerequisites().equals(""))) {
            mTextViewPrerequisites.setText(mCourse.getPrerequisites());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_prerequisites).setVisibility(View.GONE);
        }

        // Set course's Target population
        if ((mCourse.getTargetPopulation() != null) &&
                (!mCourse.getTargetPopulation().equals(""))) {
            mTextViewTargetPopulation.setText(mCourse.getTargetPopulation());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_targetPopulation).setVisibility(View.GONE);
        }

        // Set course's DayTime
        if ((mCourse.getDayTime() != null) &&
                (!mCourse.getDayTime().equals(""))) {
            mTextViewDayTime.setText(mCourse.getDayTime());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_dayTime).setVisibility(View.GONE);
        }

        // Set course's Days duration
        if (mCourse.getDurationInDays() != 0) {
            mTextViewDaysDuration.setText(String.valueOf(mCourse.getDurationInDays()));
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_dayDuration).setVisibility(View.GONE);
        }

        // Set course's Hours duration
        if (mCourse.getDurationInHours() != 0) {
            mTextViewHoursDuration.setText(String.valueOf(mCourse.getDurationInHours()));
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_hoursDuration).setVisibility(View.GONE);
        }

        // Set course's comments
        if ((mCourse.getComments() != null) &&
                (!mCourse.getComments().equals(""))) {
            mTextViewComments.setText(mCourse.getComments());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_comments).setVisibility(View.GONE);
        }
        if(!isAnyDataExist) {
            findViewById(R.id.course_content_textView_noDetailsMessage).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        Boolean courseShared = mSharedPreferences.getBoolean("courseShared", false);
        if (courseShared) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mFabCycles.animate().cancel();
                mFabCycles.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .alpha(0f)
                        .setDuration(200)
                        .setInterpolator(new FastOutLinearInInterpolator())
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mToolbar.setVisibility(View.GONE);
                                supportFinishAfterTransition();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        });
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleUtils.updateLocale(this);
    }

    public void onSecondaryActionsClick() {
        mMaterialsButton = (LinearLayout) findViewById(R.id.materials_button);
        mMaterialsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialItem.rawQueryInBackground(MaterialItem.getSelectCourseMaterialsQuery(mCourse.getCourseCode()),
                        CourseActivity.this, MaterialItem.class, true, new BackgroundTaskCallBack() {
                            @Override
                            public void onSuccess(String result, List<Object> data) {
                                if (data != null && data.size() > 0) {
                                    try {
                                        ArrayList<MaterialItem> materialItems = (ArrayList)data;
                                        Intent i = new Intent(CourseActivity.this, CourseMaterialsActivity.class);
                                        i.putExtra(Constants.EXTRA_COURSE, mCourse);
                                        i.putParcelableArrayListExtra(Constants.EXTRA_COURSE_MATERIALS_LIST, materialItems);
                                        startActivity(i);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(CourseActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(CourseActivity.this, R.string.no_materials_for_course, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onError(String error) {
                                if (error != null) {
                                    Log.e("GET COURSE MATERIALS "," ERROR:\n" + error);
                                } else {
                                    Log.e("GET COURSE MATERIALS "," ERROR");
                                }
                                Toast.makeText(CourseActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        mShareButton = (LinearLayout) findViewById(R.id.share_button);
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ShareCourseImageTask(CourseActivity.this, mCourse).execute();
            }
        });
    }

    private class SendRatingRequest extends AsyncTask<Void,Void,Boolean> {

        public static final int REQUEST_TYPE_POST = 10;
        public static final int REQUEST_TYPE_PUT = 11;
        public static final int REQUEST_TYPE_DELETE = 12;

        private int requestType = 0;
        private Rating tempRating;
        private BackgroundTaskCallBack callBack;

        public SendRatingRequest (int requestType, Rating rating) {
            this.requestType = requestType;
            this.tempRating = rating;
        }

        public SendRatingRequest (int requestType, Rating rating, BackgroundTaskCallBack callBack) {
            this.requestType = requestType;
            this.tempRating = rating;
            this.callBack = callBack;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (requestType != 0) {
                try {
                    String apiToken = UpdateIntentService.getApiToken();
                    switch (requestType) {
                        case REQUEST_TYPE_POST:
                            if (MarshalServiceProvider.getInstance(apiToken).
                                    postRating(tempRating).execute().isSuccessful()) {
                                tempRating.create();
                                return true;
                            } else {
                                return false;
                            }

                        case REQUEST_TYPE_PUT:
                            if (MarshalServiceProvider.getInstance(apiToken).
                                    updateRating(tempRating).execute().isSuccessful()) {
                                tempRating.save();
                                return true;
                            } else {
                                return false;
                            }

                        case REQUEST_TYPE_DELETE:
                            if (MarshalServiceProvider.getInstance(apiToken).deleteRating(mUserRating.getCourseCode(),
                                    mUserRating.getUserMailAddress()).execute().isSuccessful()) {
                                mUserRating.delete();
                                return true;
                            } else {
                                return false;
                            }

                        default:
                            return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                try {
                    switch (requestType) {
                        case REQUEST_TYPE_POST:
                            mUserRating = tempRating;
                            callBack.onSuccess("", null);
                            break;
                        case REQUEST_TYPE_PUT:
                            mUserRating = tempRating;
                            mTextViewReviewText.setVisibility(View.VISIBLE);
                            showRatingChanges();
                            break;
                        case REQUEST_TYPE_DELETE:
                            // Simulate removing user review
                            initializeRatingViews();
                            showRatingChanges();
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                switch (requestType) {
                    case REQUEST_TYPE_POST:
                        mRatingBarUser.setOnRatingBarChangeListener(null);
                        mRatingBarUser.setRating(0);
                        mRatingBarUser.setOnRatingBarChangeListener(mRatingBarUserOnChangeListener);
                        Toast.makeText(CourseActivity.this, R.string.review_create_error, Toast.LENGTH_LONG).show();
                        break;
                    case REQUEST_TYPE_PUT:
                        Toast.makeText(CourseActivity.this, R.string.review_save_retry, Toast.LENGTH_LONG).show();
                        break;
                    case REQUEST_TYPE_DELETE:
                        Toast.makeText(CourseActivity.this, R.string.review_delete_retry, Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
            }
        }

        public void showRatingChanges() {
            showRatingAverage();
            showRatingsCount();
            showUserRating();

            // Send broadcast for update the rating on the CardView
            Intent intent = new Intent(CoursesRecyclerAdapter.ACTION_ITEM_DATA_CHANGED);
            sendBroadcast(intent);
        }
    }
}
