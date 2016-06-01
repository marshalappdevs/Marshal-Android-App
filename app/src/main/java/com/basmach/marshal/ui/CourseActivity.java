package com.basmach.marshal.ui;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.entities.Rating;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmach.marshal.ui.adapters.CoursesRecyclerAdapter;
import com.basmach.marshal.ui.fragments.CyclesBottomSheetDialogFragment;
import com.basmach.marshal.ui.utils.ColorUtils;
import com.basmach.marshal.ui.utils.LocaleUtils;
import com.basmach.marshal.ui.utils.ThemeUtils;
import com.basmach.marshal.utils.DateHelper;
import com.basmach.marshal.utils.HashUtil;
import com.basmach.marshal.utils.MarshalServiceProvider;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Response;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class CourseActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE = "course_extra";
    public static final String EXTRA_RATINGS = "ratings_extra";

    private Toolbar mToolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private Course mCourse;
    private Rating mUserRating;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        LocaleUtils.updateLocale(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.course_shared_enter));
            getWindow().setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.course_shared_return));
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

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        // hide toolbar expanded title
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));

        //Initialize Cycles FAB
        mFabCycles = (FloatingActionButton) findViewById(R.id.course_activity_fab_cycles);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition sharedElementEnterTransition = getWindow().getSharedElementEnterTransition();
            sharedElementEnterTransition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                }

                @Override
                public void onTransitionEnd(Transition transition) {
//                    mToolbar.setVisibility(View.VISIBLE);
                    new MaterialShowcaseView.Builder(CourseActivity.this)
                            .setTarget(mFabCycles)
                            .setShapePadding(48)
                            .setDismissText(R.string.got_it)
                            .setTitleText(R.string.cycle_explanation)
                            .setDismissOnTouch(true)
                            .singleUse("") // provide a unique ID used to ensure it is only shown once
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
        }

        mCourse = getIntent().getParcelableExtra(EXTRA_COURSE);

        if (mCourse != null) {
            Log.i("Course Activity", "course passed");

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

            onSecondaryActionsClick();

            // Set the course title
            collapsingToolbarLayout.setTitle(mCourse.getName());

            mBtnReadAllReviews = (Button) findViewById(R.id.course_content_button_readAllReviews);
            mBtnReadAllReviews.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mRatingBarAverage.getRating() == 0) {
                        Toast.makeText(CourseActivity.this, R.string.no_reviews_error, Toast.LENGTH_LONG).show();
                    } else {
                        Intent i = new Intent(CourseActivity.this, RatingsActivity.class);
                        i.putExtra(RatingsActivity.EXTRA_COURSE, mCourse);
                        i.putExtra(RatingsActivity.EXTRA_RATING_AMOUNT, mTextViewRatingsAmount.getText().toString());
                        i.putExtra(RatingsActivity.EXTRA_RATING_BAR_STARS, mRatingBarAverage.getRating());
                        i.putExtra(RatingsActivity.EXTRA_RATING_AVERAGE, mTextViewRatingAverage.getText().toString());
                        startActivity(i);
                    }
                }
            });
            mTextViewCourseCode = (TextView) findViewById(R.id.course_content_textView_courseCode);
            mTextViewGeneralDescription = (TextView) findViewById(R.id.course_content_textView_description);
            mTextViewSyllabus = (TextView) findViewById(R.id.course_content_textView_syllabus);
            mTextViewPrerequisites = (TextView) findViewById(R.id.course_content_textView_prerequisites);
            mTextViewTargetPopulation = (TextView) findViewById(R.id.course_content_textView_targetPopulation);
            mTextViewDayTime = (TextView) findViewById(R.id.course_content_textView_dayTime);
            mTextViewDaysDuration = (TextView) findViewById(R.id.course_content_textView_daysDuration);
            mTextViewHoursDuration = (TextView) findViewById(R.id.course_content_textView_hoursDuration);
            mTextViewComments = (TextView) findViewById(R.id.course_content_textView_comments);
            mReviewProfileImageView = (CircleImageView) findViewById(R.id.user_profile_image);

            // Set the course photo
            mHeader = (ImageView) findViewById(R.id.header);

            mHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCourse.getName() != null && !(mCourse.getName().equals(""))) {
                        Toast.makeText(CourseActivity.this, mCourse.getName(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            Picasso.with(this).load(mCourse.getImageUrl()).into(mHeader, new Callback() {
                @Override
                public void onSuccess() {
                    final Bitmap bitmap = ((BitmapDrawable) mHeader.getDrawable()).getBitmap();
                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            contentColor = palette.getMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            scrimColor = ContextCompat.getColor(getApplicationContext(), R.color.black_trans80);
                            collapsingToolbarLayout.setStatusBarScrimColor(scrimColor);
                            collapsingToolbarLayout.setContentScrimColor(contentColor);
//                            paintTitlesTextColor(contentColor);
//                            collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimaryDark)));
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
                                        isDark = ColorUtils.isDark(bitmap, bitmap.getWidth() / 2, 0);
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

                @Override
                public void onError() {

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

        if (mCourse != null) {
            showRatingAverage();
            showRatingsCount();
            showUserRating();
        }

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

    private void showUserRating() {
        if (MainActivity.sUserEmailAddress != null) {
            Rating.queryInBackground(Rating.class, CourseActivity.this, false,
                    new String[]{DBConstants.COL_COURSE_CODE, DBConstants.COL_USER_MAIL_ADDRESS},
                    new String[]{mCourse.getCourseCode(), HashUtil.SHA1(MainActivity.sUserEmailAddress)},
                    new BackgroundTaskCallBack() {
                        @Override
                        public void onSuccess(String result, List<Object> data) {

                            if (data != null && data.size() > 0) {

                                mRatingBarUser.setVisibility(View.GONE);
                                mReviewItemContainer.setVisibility(View.VISIBLE);
                                mReviewAuthor.setText(MainActivity.sUserName);
                                Uri uri = MainActivity.sUserProfileImage;
                                Picasso.with(CourseActivity.this)
                                        .load(uri)
                                        .placeholder(R.drawable.ic_profile_none)
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
        Picasso.with(this)
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

                    String emailHash = null;
                    try {
                        emailHash = HashUtil.SHA1(MainActivity.sUserEmailAddress);
                        mUserRating = new Rating(CourseActivity.this);
                        mUserRating.setComment(input.getText().toString());
                        mUserRating.setRating(mRatingBarUser.getRating());
                        mUserRating.setUserMailAddress(emailHash);
                        mUserRating.setPlainMailAddress(MainActivity.sUserEmailAddress);
                        mUserRating.setCourseCode(mCourse.getCourseCode());
                        mUserRating.setCreatedAt(new Date());
                        mUserRating.setLastModified(new Date());
                        MarshalServiceProvider.getInstance().postRating(mUserRating).enqueue(new retrofit2.Callback<Rating>() {
                            @Override
                            public void onResponse(Call<Rating> call, Response<Rating> response) {
                                if (response.isSuccessful()) {
                                    new AsyncTask<Void, Void, Boolean>() {
                                        @Override
                                        protected Boolean doInBackground(Void... voids) {
                                            try {
                                                mUserRating.create();
                                                return true;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                return false;
                                            }
                                        }

                                        @Override
                                        protected void onPostExecute(Boolean result) {
                                            super.onPostExecute(result);
                                            if (result) {
                                                showRatingAverage();
                                                showRatingsCount();
                                                showUserRating();

                                                // Send broadcast for update the rating on the CardView
                                                Intent intent = new Intent(CoursesRecyclerAdapter.ACTION_ITEM_DATA_CHANGED);
                                                sendBroadcast(intent);
                                            }
                                        }
                                    }.execute();

                                    // Simulate showing user review

                                    mTextViewReviewHint.setVisibility(View.GONE);
                                    mRatingBarUser.setVisibility(View.GONE);
                                    mReviewItemContainer.setVisibility(View.VISIBLE);
                                    mReviewAuthor.setText(MainActivity.sUserName);
                                    Uri uri = MainActivity.sUserProfileImage;
                                    Picasso.with(CourseActivity.this)
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
                                    Toast.makeText(CourseActivity.this, R.string.review_feedback_posted, Toast.LENGTH_LONG).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Rating> call, Throwable t) {
                                mRatingBarUser.setOnRatingBarChangeListener(null);
                                mRatingBarUser.setRating(0);
                                mRatingBarUser.setOnRatingBarChangeListener(mRatingBarUserOnChangeListener);
                                Toast.makeText(CourseActivity.this, R.string.review_feedback_posted_error, Toast.LENGTH_LONG).show();
                            }
                        });
                        alertDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

                    if(mUserRating != null) {
                        mUserRating.setRating(ratingBar.getRating());
                        mUserRating.setComment(input.getText().toString());
                        mUserRating.setLastModified(new Date());
                        MarshalServiceProvider.getInstance().updateRating(mUserRating).enqueue(new retrofit2.Callback<Rating>() {
                            @Override
                            public void onResponse(Call<Rating> call, Response<Rating> response) {
                                if(response.isSuccessful()) {
                                    new AsyncTask<Void, Void, Boolean>() {
                                        @Override
                                        protected Boolean doInBackground(Void... voids) {
                                            try {
                                                mUserRating.save();
                                                return true;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                return false;
                                            }
                                        }

                                        @Override
                                        protected void onPostExecute(Boolean result) {
                                            super.onPostExecute(result);
                                            if (result) {
                                                mTextViewReviewText.setVisibility(View.VISIBLE);
                                                showUserRating();
                                                showRatingsCount();
                                                showRatingAverage();

                                                // Send broadcast for update the rating on the CardView
                                                Intent intent = new Intent(CoursesRecyclerAdapter.ACTION_ITEM_DATA_CHANGED);
                                                sendBroadcast(intent);
                                            }
                                        }
                                    }.execute();
                                }
                            }

                            @Override
                            public void onFailure(Call<Rating> call, Throwable t) {
                                Toast.makeText(CourseActivity.this, R.string.review_save_retry, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    alertDialog.dismiss();
                }
            });

            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mUserRating != null) {
                        MarshalServiceProvider.getInstance().deleteRating(mUserRating.getCourseCode(),
                                mUserRating.getUserMailAddress()).enqueue(new retrofit2.Callback<Rating>() {
                            @Override
                            public void onResponse(Call<Rating> call, Response<Rating> response) {
                                if (response.isSuccessful()) {
                                    new AsyncTask<Void, Void, Boolean>() {
                                        @Override
                                        protected Boolean doInBackground(Void... voids) {
                                            try {
                                                mUserRating.delete();
                                                return true;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                return false;
                                            }
                                        }

                                        @Override
                                        protected void onPostExecute(Boolean result) {
                                            super.onPostExecute(result);
                                            if (result) {
                                                // Simulate removing user review
                                                initializeRatingViews();
                                                showUserRating();
                                                showRatingsCount();
                                                showRatingAverage();

                                                // Send broadcast for update the rating on the CardView
                                                Intent intent = new Intent(CoursesRecyclerAdapter.ACTION_ITEM_DATA_CHANGED);
                                                sendBroadcast(intent);
                                            }
                                        }
                                    }.execute();
                                }
                            }

                            @Override
                            public void onFailure(Call<Rating> call, Throwable t) {
                                Toast.makeText(CourseActivity.this, R.string.review_delete_retry, Toast.LENGTH_LONG).show();
                            }
                        });
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mFabCycles.animate().cancel();
            mFabCycles.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(200)
                    .setInterpolator(new FastOutLinearInInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override public void onAnimationStart(Animator animation) {}

                        @Override public void onAnimationEnd(Animator animation) {
                            mToolbar.setVisibility(View.GONE);
                            supportFinishAfterTransition();
                        }

                        @Override public void onAnimationCancel(Animator animation) {}

                        @Override public void onAnimationRepeat(Animator animation) {}
                    });
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
                Toast.makeText(CourseActivity.this, R.string.course_related_content, Toast.LENGTH_LONG).show();
            }
        });
        mShareButton = (LinearLayout) findViewById(R.id.share_button);
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://play.google.com/store/apps/details?id=com.basmach.marshal";
                String courseName = String.format(getString(R.string.share_course_text), mCourse.getName(), url);
                Uri courseImage = getLocalBitmapUri(mHeader);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, courseName);
                if (courseImage != null) {
                    shareIntent.setType("image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, courseImage);
                }
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_with)));
            }
        });
    }

    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to cache directory
        Uri bmpUri = null;
        try {
            File tempFile = new File(getBaseContext().getExternalCacheDir() + File.separator + mCourse.getCourseCode() + ".jpg") ;
            // check if image already exists, if it does, don't create it again.
            // to save space it's possible to remove that check and give the image a static name
            // then every image will overwrite last one.
            if (!tempFile.exists()) {
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            bmpUri = Uri.fromFile(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
