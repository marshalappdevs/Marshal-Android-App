package com.basmapp.marshal.ui.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.util.ThemeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationUtils {

    SharedPreferences mSharedPreferences;
    Context mContext;

    public NotificationUtils(Context context) {
        this.mContext = context;
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getLightColor() {
        int lightColor = Color.parseColor(mSharedPreferences
                .getString(Constants.PREF_NOTIFICATIONS_COLOR, "#FFFFFF"));
        return lightColor;
    }

    public long[] getVibrate() {
        long[] vibrate = new long[]{0};
        if (mSharedPreferences.getBoolean(Constants.PREF_NOTIFICATIONS_NEW_MESSAGE_VIBRATE, false)) {
            vibrate = new long[]{0, 1000};
        }

        return vibrate;
    }

    public Uri getRingtoneUri() {
        String ringtonePref = mSharedPreferences.getString(Constants.PREF_NOTIFICATIONS_NEW_RINGTONE, null);
        Uri ringtoneUri;
        if (ringtonePref != null) {
            ringtoneUri = Uri.parse(ringtonePref);
        } else {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        return ringtoneUri;
    }

    public void notify(String message, PendingIntent pendingIntent) {
        if (mSharedPreferences.getBoolean(Constants.PREF_NOTIFY_NEW_MESSAGE, true)) {
            Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.stat_notify_basmapp)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(mContext.getString(R.string.app_name))
                    .setContentText(message)
                    .setColor(ThemeUtils.getThemeColor(mContext, R.attr.colorPrimary))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message))
                    .setLights(getLightColor(), 500, 2000)
                    .setSound(getRingtoneUri())
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(getVibrate());
            NotificationManager mNotificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
        }
    }

    public void notify(String title, String message, PendingIntent pendingIntent) {
        if (mSharedPreferences.getBoolean(Constants.PREF_NOTIFY_NEW_MESSAGE, true)) {
            Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.stat_notify_basmapp)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setColor(ThemeUtils.getThemeColor(mContext, R.attr.colorPrimary))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message))
                    .setLights(getLightColor(), 500, 2000)
                    .setSound(getRingtoneUri())
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(getVibrate());
            NotificationManager mNotificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
        }
    }

    public static void Notify(Context context, String message, int lightColor, Uri ringtoneUri,
                              PendingIntent pendingIntent, long[] vibrate) {
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.stat_notify_basmapp)
                .setLargeIcon(largeIcon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setColor(ThemeUtils.getThemeColor(context, R.attr.colorPrimary))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setLights(lightColor, 500, 2000)
                .setSound(ringtoneUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(vibrate);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    public static class GeneratePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

        private Context mContext;
        private String message, imageUrl;
        private long[] vibrate;
        private Uri ringtoneUri;
        private int lightColor;
        private PendingIntent pendingIntent;

        public GeneratePictureStyleNotification(Context context, String message, String imageUrl, PendingIntent pendingIntent) {
            super();
            this.mContext = context;
            this.message = message;
            this.imageUrl = imageUrl;
            this.pendingIntent = pendingIntent;
            initializeDefaultValues(context);
        }

        public GeneratePictureStyleNotification(Context context, String message, String imageUrl, long[] vibrate, Uri ringtoneUri, int lightColor, PendingIntent pendingIntent) {
            super();
            this.mContext = context;
            this.message = message;
            this.imageUrl = imageUrl;
            this.vibrate = vibrate;
            this.ringtoneUri = ringtoneUri;
            this.lightColor = lightColor;
            this.pendingIntent = pendingIntent;
        }

        private void initializeDefaultValues(Context context) {
            NotificationUtils notificationUtils = new NotificationUtils(context);
            this.vibrate = notificationUtils.getVibrate();
            this.ringtoneUri = notificationUtils.getRingtoneUri();
            this.lightColor = notificationUtils.getLightColor();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream inputStream;
            try {
                URL url = new URL(this.imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                inputStream = connection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
//            PendingIntent notifyPendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext,
//                    MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

            Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.stat_notify_basmapp)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(mContext.getString(R.string.app_name))
                    .setContentText(message)
                    .setColor(ThemeUtils.getThemeColor(mContext, R.attr.colorPrimary))
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(result)
                            .setSummaryText(message))
                    .setLights(lightColor, 500, 2000)
                    .setSound(ringtoneUri)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(vibrate);
            NotificationManager mNotificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(2, mBuilder.build());
        }
    }
}
