package com.basmapp.marshal.ui.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.basmapp.marshal.R;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.util.ThemeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ido on 9/20/2016.
 */

public class NotificationUtils {
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
        NotificationManager mNotificationManager=
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    public static class GeneratePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

        private Context mContext;
        private String message, imageUrl;
        private long[] vibrate;
        private Uri ringtoneUri;
        private int lightColor;

        public GeneratePictureStyleNotification(Context context, String message, String imageUrl, long[] vibrate, Uri ringtoneUri, int lightColor) {
            super();
            this.mContext = context;
            this.message = message;
            this.imageUrl = imageUrl;
            this.vibrate = vibrate;
            this.ringtoneUri = ringtoneUri;
            this.lightColor = lightColor;
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
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext,
                    MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

            Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.stat_notify_basmapp)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(mContext.getString(R.string.app_name))
                    .setContentText(message)
                    .setColor(ThemeUtils.getThemeColor(mContext, R.attr.colorPrimary))
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(result))
                    .setLights(lightColor, 500, 2000)
                    .setSound(ringtoneUri)
                    .setContentIntent(notifyPendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(vibrate);
            NotificationManager mNotificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(2, mBuilder.build());
        }
    }
}
