package com.basmapp.marshal.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Rating;
import com.basmapp.marshal.localdb.interfaces.BackgroundTaskCallBack;
import com.basmapp.marshal.util.AuthUtil;
import com.basmapp.marshal.util.HashUtil;
import com.basmapp.marshal.util.LocaleUtils;
import com.basmapp.marshal.util.MarshalServiceProvider;
import com.basmapp.marshal.util.ThemeUtils;
import com.basmapp.marshal.util.glide.CircleTransform;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RateReviewActivity extends AppCompatActivity {

    public static final int RESULT_POST_SUCCESS = 111;
    public static final int RESULT_PUT_SUCCESS = 121;
    public static final int RESULT_DELETE_SUCCESS = 131;

    public static final int RESULT_POST_FAILED = 110;
    public static final int RESULT_PUT_FAILED = 120;
    public static final int RESULT_DELETE_FAILED = 130;

    // Data Members
    private Course mCourse;
    private Rating mUserRating;
    private int mContentColor = -1;
    private boolean mIsEditMode;

    private RatingBar mRatingBar;
    private EditText mInputEditText;
    private TextView mItemTitleTextView;

    boolean blockComments = true;

    static boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        ThemeUtils.updateAccent(this);
        LocaleUtils.updateLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_review_dialog);

        // Getting data from the Intent
        mIsEditMode = getIntent().getBooleanExtra(Constants.EXTRA_IS_EDIT_MODE, false);
        mCourse = getIntent().getParcelableExtra(Constants.EXTRA_COURSE);
        mUserRating = getIntent().getParcelableExtra(Constants.EXTRA_USER_RATING);
        mContentColor = getIntent().getIntExtra(Constants.EXTRA_CONTENT_COLOR, -1);

        showReviewCommentDialog();
    }

    @Override
    public void onStart() {
        super.onStart();
        isRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isRunning = false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleUtils.updateConfig(this, newConfig);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void showReviewCommentDialog() {
        // Components Initialization
        View userHeaderBackground = (findViewById(R.id.review_user_header_background));
        userHeaderBackground.setBackgroundColor(mContentColor);
        ImageView profileImageView = (ImageView) findViewById(R.id.review_header_user_profile_image);
        TextView reviewBy = (TextView) findViewById(R.id.review_header_review_by);
        mInputEditText = (EditText) findViewById(R.id.review_review_comment);
        mRatingBar = (RatingBar) findViewById(R.id.review_ratingBar_user);
        Button negativeButton = (Button) findViewById(R.id.review_negative_button);
        Button positiveButton = (Button) findViewById(R.id.review_positive_button);
        TextInputLayout inputLayout = (TextInputLayout) findViewById(R.id.review_inputLayout);
        mItemTitleTextView = (TextView) findViewById(R.id.review_item_title);
        mItemTitleTextView.setTextColor(ThemeUtils.getThemeColor(this, R.attr.colorAccent));

        mRatingBar.setRating((float) mUserRating.getRating());

        // Variables Initialization
        String userName = String.format(getString(R.string.review_by), MainActivity.sUserName);
        reviewBy.setText(userName);
        Uri uri = MainActivity.sUserProfileImage;

        // Show the user profile picture
        if (uri != null) {
            Glide.with(this)
                    .load(uri)
                    .transform(new CircleTransform(this))
                    .placeholder(R.drawable.ic_profile_none)
                    .into(profileImageView);
        }

        // Show Comments Dialog on editText's click
        if (blockComments) {
            final List<String> mComposedComment = new ArrayList<>();
            mInputEditText.setFocusable(false);
            mInputEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Delete previous composed comment
                    mComposedComment.clear();
                    // Comment Dialog initialization
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(RateReviewActivity.this);
                    alertDialog.setTitle(getString(R.string.compose_new_comment));
                    alertDialog.setMultiChoiceItems(R.array.review_comments, null, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            String selectedComments = getResources().getStringArray(R.array.review_comments)[which];
                            if (isChecked) {
                                mComposedComment.add(selectedComments);
                            } else if (mComposedComment.contains(selectedComments)) {
                                // Remove comment when unchecked
                                mComposedComment.remove(selectedComments);
                            }
                        }
                    });
                    alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mInputEditText.setText(String.format(getString(R.string.composed_comment),
                                    android.text.TextUtils.join(", ", mComposedComment)));
                        }
                    });
                    // Show the comments dialog
                    alertDialog.show();
                }
            });
        }

        showRatingText((int) mRatingBar.getRating());

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (rating < 1) {
                    ratingBar.setRating(1);
                }
                showRatingText((int) rating);
            }
        });

        if (!mIsEditMode) {
            inputLayout.setError(getString(R.string.public_reviews_message)); // show error
            negativeButton.setVisibility(View.GONE);
            positiveButton.setText(getString(R.string.structured_review_question_submit));

            // Submit Review
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String emailHash;
                    Rating tempRating;

                    try {
                        emailHash = HashUtil.SHA(MainActivity.sUserEmailAddress);

                        tempRating = new Rating(RateReviewActivity.this);
                        tempRating.setComment(mInputEditText.getText().toString());
                        tempRating.setRating(mRatingBar.getRating());
                        tempRating.setUserMailAddress(emailHash);
                        tempRating.setCourseID(mCourse.getCourseID());
                        tempRating.setCreatedAt(new Date());
                        tempRating.setLastModified(new Date());

                        new SendRatingRequest(SendRatingRequest.REQUEST_TYPE_POST, tempRating).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                        setResult(RESULT_POST_FAILED);
                        finish();
                    }
                }
            });
        } else {
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText(getString(R.string.delete_review));
            positiveButton.setVisibility(View.VISIBLE);
            positiveButton.setText(getString(R.string.save_review));
            mInputEditText.setText(mUserRating.getComment());

            // Save Review
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mUserRating != null) {
                        Rating tempRating = new Rating(RateReviewActivity.this);
                        tempRating.setId(mUserRating.getId());
                        tempRating.setCourseID(mUserRating.getCourseID());
                        tempRating.setCreatedAt(mUserRating.getCreatedAt());
                        tempRating.setRating(mRatingBar.getRating());
                        tempRating.setComment(mInputEditText.getText().toString());
                        tempRating.setLastModified(new Date());
                        tempRating.setUserMailAddress(mUserRating.getUserMailAddress());

                        new SendRatingRequest(SendRatingRequest.REQUEST_TYPE_PUT, tempRating).execute();
                    } else {
                        setResult(RESULT_PUT_FAILED);
                        finish();
                    }
                }
            });

            // Delete Review
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mUserRating != null) {
                        Rating tempRating = new Rating(RateReviewActivity.this);
                        tempRating.setId(mUserRating.getId());
                        tempRating.setCourseID(mUserRating.getCourseID());
                        tempRating.setCreatedAt(mUserRating.getCreatedAt());
                        tempRating.setRating(mUserRating.getRating());
                        tempRating.setComment(mInputEditText.getText().toString());
                        tempRating.setLastModified(new Date());
                        tempRating.setUserMailAddress(mUserRating.getUserMailAddress());

                        new SendRatingRequest(SendRatingRequest.REQUEST_TYPE_DELETE, tempRating).execute();
                    } else {
                        setResult(RESULT_DELETE_FAILED);
                        finish();
                    }
                }
            });
        }
    }

    private void showRatingText(int rating) {
        switch (rating) {
            case 1:
                mItemTitleTextView.setText(getString(R.string.review_dialog_poor));
                break;
            case 2:
                mItemTitleTextView.setText(getString(R.string.review_dialog_below_average));
                break;
            case 3:
                mItemTitleTextView.setText(getString(R.string.review_dialog_average));
                break;
            case 4:
                mItemTitleTextView.setText(getString(R.string.review_dialog_above_average));
                break;
            case 5:
                mItemTitleTextView.setText(getString(R.string.review_dialog_excellent));
                break;
            default:
                break;
        }
    }

    private class SendRatingRequest extends AsyncTask<Void, Void, Boolean> {

        static final int REQUEST_TYPE_POST = 10;
        static final int REQUEST_TYPE_PUT = 11;
        static final int REQUEST_TYPE_DELETE = 12;

        private int requestType = 0;
        private Rating tempRating;
        private BackgroundTaskCallBack callBack;

        SendRatingRequest(int requestType, Rating rating) {
            this.requestType = requestType;
            this.tempRating = rating;
        }

        SendRatingRequest(int requestType, Rating rating, BackgroundTaskCallBack callBack) {
            this.requestType = requestType;
            this.tempRating = rating;
            this.callBack = callBack;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (requestType != 0) {
                try {
                    String apiToken = AuthUtil.getApiToken();
                    switch (requestType) {
                        case REQUEST_TYPE_POST:
                            if (MarshalServiceProvider.getInstance(apiToken).
                                    postRating(mCourse.getId(), tempRating).execute().isSuccessful()) {
                                tempRating.create();
                                return true;
                            } else {
                                return false;
                            }

                        case REQUEST_TYPE_PUT:
                            if (MarshalServiceProvider.getInstance(apiToken).
                                    updateRating(mCourse.getId(), tempRating).execute().isSuccessful()) {
                                tempRating.save();
                                return true;
                            } else {
                                return false;
                            }

                        case REQUEST_TYPE_DELETE:
                            if (MarshalServiceProvider.getInstance(apiToken).deleteRating(mCourse.getId(),
                                    mUserRating).execute().isSuccessful()) {
                                tempRating.delete();
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
                    Intent data = new Intent();
                    data.putExtra(Constants.EXTRA_USER_RATING, tempRating);
                    switch (requestType) {
                        case REQUEST_TYPE_POST:
                            setResult(RESULT_POST_SUCCESS, data);
                            finish();
                            break;
                        case REQUEST_TYPE_PUT:
                            setResult(RESULT_PUT_SUCCESS, data);
                            finish();
                            break;
                        case REQUEST_TYPE_DELETE:
                            setResult(RESULT_DELETE_SUCCESS);
                            finish();
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
                        setResult(RESULT_POST_FAILED);
                        finish();
                        break;
                    case REQUEST_TYPE_PUT:
                        setResult(RESULT_PUT_FAILED);
                        finish();
                        break;
                    case REQUEST_TYPE_DELETE:
                        setResult(RESULT_DELETE_FAILED);
                        finish();
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
