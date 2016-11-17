package com.basmapp.marshal.ui;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;

import com.basmapp.marshal.BuildConfig;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.bumptech.glide.Glide;

import java.io.File;

/**
 * An AsyncTask which retrieves a File from the Glide cache then shares it.
 */
class ShareCourseImageTask extends AsyncTask<Void, Void, File> {

    private Course mCourse;
    private Context mContext;

    ShareCourseImageTask(Context context, Course course) {
        this.mContext = context;
        this.mCourse = course;
    }

    @Override
    protected File doInBackground(Void... params) {
        final String url = mCourse.getImageUrl();
        try {
            return Glide
                    .with(mContext)
                    .load(url)
                    .downloadOnly(500, 500)
                    .get();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(File result) {
        if (result == null) {
            return;
        }
        // glide cache uses an unfriendly & extension-less name,
        // massage it based on the original
        String fileName = mCourse.getImageUrl();
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        File renamed = new File(result.getParent(), fileName);
        result.renameTo(renamed);
        Uri uri = FileProvider.getUriForFile(mContext,
                BuildConfig.APPLICATION_ID + ".shareprovider", renamed);
        ShareCompat.IntentBuilder.from((Activity) mContext)
                .setText(getShareText())
                .setType(getImageMimeType(fileName))
                .setStream(uri)
                .startChooser();
    }

    private String getShareText() {
        String url = "https://play.google.com/store/apps/details?id=com.basmapp.marshal";
        return String.format(mContext.getString(R.string.share_course_text), mCourse.getName(), url);
    }

    private String getImageMimeType(@NonNull String fileName) {
        if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }
}