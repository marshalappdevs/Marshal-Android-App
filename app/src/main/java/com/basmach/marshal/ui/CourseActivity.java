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
import android.support.design.widget.Snackbar;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
import com.basmach.marshal.utils.MarshalServiceProvider;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class CourseActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE = "course_extra";
    public static final String EXTRA_RATINGS = "ratings_extra";

    private Toolbar mToolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private Course mCourse;

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
    private TextView mTextViewYourReview;
    private RatingBar mRatingBarAvergae;
    private RatingBar mRatingBarUser;
    private LinearLayout mMaterialsButton;
    private LinearLayout mShareButton;

    private int contentColor = -1;
    private int scrimColor = -1;
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    private FloatingActionButton mFabCycles;

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
                                @Override public void onAnimationStart(Animator animation) {}

                                @Override public void onAnimationEnd(Animator animation) {
                                    mToolbar.setVisibility(View.GONE);
                                    supportFinishAfterTransition();
                                }

                                @Override public void onAnimationCancel(Animator animation) {}

                                @Override public void onAnimationRepeat(Animator animation) {}
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
            if (mFabCycles != null) mFabCycles.hide();
            Transition sharedElementEnterTransition = getWindow().getSharedElementEnterTransition();
            sharedElementEnterTransition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {}

                @Override
                public void onTransitionEnd(Transition transition) {
//                    mToolbar.setVisibility(View.VISIBLE);
                    mFabCycles.show();
                }

                @Override
                public void onTransitionCancel(Transition transition) {}

                @Override
                public void onTransitionPause(Transition transition) {}

                @Override
                public void onTransitionResume(Transition transition) {}
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

                    for(int index = 0; index < cycles.size(); index++) {
                        if(cycles.get(index).getStartDate() == null || cycles.get(index).getEndDate() == null) {
                            cycles.remove(cycles.get(index));
                        }
                    }

                    if (cycles.size() > 0) {
                        CyclesBottomSheetDialogFragment bottomSheet =
                                CyclesBottomSheetDialogFragment.newInstance(mCourse);
                        bottomSheet.show(getSupportFragmentManager(),"CyclesBottomSheet");
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

            mTextViewCourseCode = (TextView) findViewById(R.id.course_content_textView_courseCode);
            mTextViewGeneralDescription = (TextView) findViewById(R.id.course_content_textView_description);
            mTextViewSyllabus = (TextView) findViewById(R.id.course_content_textView_syllabus);
            mTextViewPrerequisites = (TextView) findViewById(R.id.course_content_textView_prerequisites);
            mTextViewTargetPopulation = (TextView) findViewById(R.id.course_content_textView_targetPopulation);
            mTextViewDayTime = (TextView) findViewById(R.id.course_content_textView_dayTime);
            mTextViewDaysDuration = (TextView) findViewById(R.id.course_content_textView_daysDuration);
            mTextViewHoursDuration = (TextView) findViewById(R.id.course_content_textView_hoursDuration);
            mTextViewComments = (TextView) findViewById(R.id.course_content_textView_comments);

            // Set the course photo
            mHeader = (ImageView) findViewById(R.id.header);

            mHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mCourse.getName() != null && !(mCourse.getName().equals(""))) {
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

        mRatingBarAvergae = (RatingBar) findViewById(R.id.summary_rating_bar);
        mRatingBarUser = (RatingBar) findViewById(R.id.course_content_ratingBar_user);
        mTextViewReviewHint = (TextView) findViewById(R.id.review_hint);
        mTextViewReviewDate = (TextView) findViewById(R.id.review_date);
        mTextViewReviewText = (TextView) findViewById(R.id.review_text);
        mTextViewYourReview = (TextView) findViewById(R.id.your_review_label);
        mTextViewRatingsAmount = (TextView) findViewById(R.id.course_content_textView_ratingsAmount);
        mTextViewRatingAverage = (TextView) findViewById(R.id.course_content_textView_average_value);

        if (mCourse != null) {

            showRatingAverage();
            showRatingsCount();
            showUserRating();
        }

        mRatingBarUser.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (MainActivity.userEmailAddress != null && ratingBar.getRating() != 0) {
                    showReviewCommentDialog(false);
                } else {
                    if (ratingBar.getRating() != 0) {
                        Toast.makeText(CourseActivity.this, R.string.please_log_in, Toast.LENGTH_SHORT).show();
                    }
                    ratingBar.setRating(0);
                }
            }
        });
        mTextViewReviewText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReviewCommentDialog(true);
            }
        });
    }

    private void showUserRating() {
        Rating.queryInBackground(Rating.class, CourseActivity.this, false,
                new String[]{DBConstants.COL_COURSE_CODE, DBConstants.COL_USER_MAIL_ADDRESS},
                new String[]{mCourse.getCourseCode(), MainActivity.userEmailAddress},
                new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {

                        if (data != null && data.size() > 0) {

                            mTextViewReviewHint.setVisibility(View.GONE);
                            mTextViewReviewDate.setVisibility(View.VISIBLE);
                            mTextViewReviewText.setVisibility(View.VISIBLE);
                            mTextViewYourReview.setVisibility(View.VISIBLE);
                            mTextViewReviewText.setText(((Rating)(data.get(0))).getComment());

                            // TODO set mTextViewReviewDate
                            try {
                                mTextViewReviewDate.setText(DateHelper.dateToString(((Rating)(data.get(0))).getLastModified()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            mRatingBarUser.setOnRatingBarChangeListener(null);
                            mRatingBarUser.setRating((float) ((Rating)(data.get(0))).getRating());
                            mRatingBarUser.setIsIndicator(true);
                        } else {
                            mTextViewReviewHint.setVisibility(View.VISIBLE);
                            mTextViewReviewDate.setVisibility(View.GONE);
                            mTextViewReviewText.setVisibility(View.GONE);
                            mTextViewYourReview.setVisibility(View.GONE);
                            mRatingBarUser.setRating(0);
                            mRatingBarUser.setIsIndicator(false);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        mTextViewReviewHint.setVisibility(View.VISIBLE);
                        mTextViewReviewDate.setVisibility(View.GONE);
                        mTextViewReviewText.setVisibility(View.GONE);
                        mTextViewYourReview.setVisibility(View.GONE);
                        mRatingBarUser.setRating(0);
                        mRatingBarUser.setIsIndicator(false);
                    }
                });
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
                                mRatingBarAvergae.setRating((Float) data.get(0));
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

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.Cycle_DialogAlert);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View dialogView = layoutInflater.inflate(R.layout.rate_review_editor, null);
        alertDialog.setView(dialogView);

        final RatingBar ratingBar = (RatingBar) dialogView.findViewById(R.id.course_content_ratingBar_user);

        final EditText input = (EditText) dialogView.findViewById(R.id.review_comment);

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
            inputLayout.setError(getString(R.string.review_dialog_error)); // show error

            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mRatingBarUser.setRating(0);
                }
            });

            alertDialog.setPositiveButton(getString(R.string.structured_review_question_submit), new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, int whichButton) {
                    if (mRatingBarUser.getRating() > 0) {
                        final Rating newRating = new Rating(CourseActivity.this);
                        newRating.setComment(input.getText().toString());
                        newRating.setRating(mRatingBarUser.getRating());
                        newRating.setUserMailAddress(MainActivity.userEmailAddress);
                        newRating.setCourseCode(mCourse.getCourseCode());
                        newRating.setLastModified(new Date());
                        MarshalServiceProvider.getInstance().postRating(newRating).enqueue(new retrofit2.Callback<Rating>() {
                            @Override
                            public void onResponse(Call<Rating> call, Response<Rating> response) {
                                if (response.isSuccessful()) {
                                    new AsyncTask<Void, Void, Boolean>() {
                                        @Override
                                        protected Boolean doInBackground(Void... voids) {
                                            try {
                                                newRating.create();
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
                                    mTextViewReviewDate.setVisibility(View.VISIBLE);
                                    mTextViewReviewText.setVisibility(View.VISIBLE);
                                    mTextViewYourReview.setVisibility(View.VISIBLE);

                                    mRatingBarUser.setRating(mRatingBarUser.getRating());
                                    try {
                                        mTextViewReviewDate.setText(DateHelper.dateToString(newRating.getLastModified()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mTextViewReviewText.setText(input.getText().toString());
                                    mRatingBarUser.setIsIndicator(true);
                                    dialog.dismiss();
                                    Toast.makeText(CourseActivity.this, "Thanks for your rating", Toast.LENGTH_LONG).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Rating> call, Throwable t) {

                            }
                        });
                    } else {
                        Snackbar.make(dialogView, "Please rate before submit", Snackbar.LENGTH_LONG).show();
                    }
                }

            });
        } else {
            ratingBar.setVisibility(View.VISIBLE);
            ratingBar.setRating(mRatingBarUser.getRating());
            input.setText(mTextViewReviewText.getText().toString());

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

            alertDialog.setPositiveButton(getString(R.string.save_review), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    // Simulate showing user edited review
                    mTextViewReviewText.setText(input.getText().toString());
                    mRatingBarUser.setOnRatingBarChangeListener(null);
                    mRatingBarUser.setRating(ratingBar.getRating());
                }
            });

            alertDialog.setNegativeButton(getString(R.string.delete_review), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(CourseActivity.this, "comment deleted", Toast.LENGTH_LONG).show();
                }
            });
        }

        AlertDialog dialog = alertDialog.create();
        alertDialog.show();
}

    private void setLightStatusBar(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
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
