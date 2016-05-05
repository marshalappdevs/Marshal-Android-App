package com.basmach.marshal.ui;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.entities.Rating;
import com.basmach.marshal.ui.fragments.CyclesBottomSheetDialogFragment;
import com.basmach.marshal.ui.utils.ColorUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class CourseActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE = "course_extra";
    public static final String EXTRA_RATINGS = "ratings_extra";

    private Toolbar mToolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private SharedPreferences mSharedPreferences;

    private Course mCourse;
    private ArrayList<Rating> mRatings;

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

    private int contentColor = -1;
    private int scrimColor = -1;
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    private FloatingActionButton mFabCycles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateTheme();
        super.onCreate(savedInstanceState);
        updateLocale();

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

        mRatings = getIntent().getParcelableArrayListExtra(EXTRA_RATINGS);

        mRatingBarAvergae = (RatingBar) findViewById(R.id.summary_rating_bar);
        mRatingBarUser = (RatingBar) findViewById(R.id.course_content_ratingBar_user);
        mTextViewReviewHint = (TextView) findViewById(R.id.review_hint);
        mTextViewReviewDate = (TextView) findViewById(R.id.review_date);
        mTextViewReviewText = (TextView) findViewById(R.id.review_text);
        mTextViewYourReview = (TextView) findViewById(R.id.your_review_label);
        mTextViewRatingsAmount = (TextView) findViewById(R.id.course_content_textView_ratingsAmount);
        mTextViewRatingAverage = (TextView) findViewById(R.id.course_content_textView_average_value);

        if (mCourse != null) {
            mTextViewRatingAverage.setText(String.valueOf(mCourse.getRatingAverage()).substring(0,3));
            mTextViewRatingsAmount.setText(String.valueOf(mCourse.getRatingsAmount()));
            mRatingBarAvergae.setRating((float) mCourse.getRatingAverage());

            if (mCourse.getUserRating() != null) {
                if (mCourse.getUserRating().getComment() != null) {
                    mTextViewYourReview.setText(mCourse.getUserRating().getComment());
                }

                mRatingBarUser.setRating((float) mCourse.getUserRating().getRating());
            }
        }

//        if (mRatings != null && mRatings.size() > 0) {
//            mTextViewRatingsAmount.setText(String.valueOf(mRatings.size()));
//
//            float ratingsSum = 0;
//
//            for (Rating rating : mRatings) {
//                if (MainActivity.userEmailAddress != null &&
//                        rating.getUserMailAddress().equals(MainActivity.userEmailAddress)) {
//
//                    if (rating.getComment() != null) {
//                        mTextViewReviewHint.setVisibility(View.GONE);
//                        mTextViewReviewDate.setVisibility(View.VISIBLE);
//                        mTextViewReviewText.setVisibility(View.VISIBLE);
//                        mTextViewYourReview.setVisibility(View.VISIBLE);
//                        mTextViewReviewText.setText(rating.getComment());
//                    }
//                    // TODO set mTextViewReviewDate
//                    mRatingBarUser.setRating((float) rating.getRating());
//                    mRatingBarUser.setIsIndicator(true);
//                } else {
//                    mTextViewReviewHint.setVisibility(View.VISIBLE);
//                    mTextViewReviewDate.setVisibility(View.GONE);
//                    mTextViewReviewText.setVisibility(View.GONE);
//                    mTextViewYourReview.setVisibility(View.GONE);
//                    mRatingBarUser.setRating(0);
//                    mRatingBarUser.setIsIndicator(false);
//                }
//
//                ratingsSum += rating.getRating();
//            }
//
//            float average = (ratingsSum / mRatings.size());
//
//            mTextViewRatingAverage.setText(String.valueOf(average).substring(0,3));
//            mRatingBarAvergae.setRating(average);
//        }

        mRatingBarUser.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (MainActivity.userEmailAddress != null && ratingBar.getRating() != 0) {
//                    Toast.makeText(CourseActivity.this, String.valueOf(ratingBar.getRating()), Toast.LENGTH_SHORT).show();
                    showReviewCommentDialog();
                } else {
                    if (ratingBar.getRating() != 0) {
                        Toast.makeText(CourseActivity.this, R.string.please_log_in, Toast.LENGTH_SHORT).show();
                    }
                    ratingBar.setRating(0);
                }
            }
        });
    }

    private void showReviewCommentDialog() {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View dialogView = layoutInflater.inflate(R.layout.rate_review_editor, null);
        alertDialog.setView(dialogView);

        final EditText input = (EditText) dialogView.findViewById(R.id.review_comment);

        TextInputLayout inputLayout = (TextInputLayout) dialogView.findViewById(R.id.inputLayout);
        inputLayout.setError(getString(R.string.review_dialog_error)); // show error

        TextView textView = (TextView) dialogView.findViewById(R.id.item_title);

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

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mRatingBarUser.setRating(0);
            }
        });

        alertDialog.setPositiveButton(getString(R.string.structured_review_question_submit), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String ratingComment = input.getText().toString();
                // Do something with value!
                Toast.makeText(CourseActivity.this, ratingComment, Toast.LENGTH_SHORT).show();
            }

        });

        alertDialog.create();
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
        updateLocale();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_course_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.course_menu_item_related_materials) {
            Toast.makeText(CourseActivity.this, "Related Materials", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.course_menu_item_share) {
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
        return super.onOptionsItemSelected(item);
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
            File tempFile = new File(getBaseContext().getExternalCacheDir() + "/" + "share_header.png") ;
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            fileOutputStream.close();
            bmpUri = Uri.fromFile(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

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
}
