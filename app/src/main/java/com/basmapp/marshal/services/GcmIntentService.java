package com.basmapp.marshal.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.util.ThemeUtils;
import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class GcmIntentService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        Log.i("GCM", "onMessageReceived from:" + from);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        long[] vibrate = new long[]{0};
        if (sharedPreferences.getBoolean("notifications_new_message_vibrate", false)) {
            vibrate = new long[]{0,1000};
        }

        String ringtonePref = sharedPreferences.getString("notifications_new_message_ringtone", null);
        Uri ringtoneUri;
        if (ringtonePref != null) {
            ringtoneUri = Uri.parse(ringtonePref);
        } else {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        int lightColor = Color.parseColor(sharedPreferences
                .getString(Constants.PREF_NOTIFICATIONS_COLOR, "#FFFFFF"));

        String message = data.getString("message");
        if(message != null &&
                message.equals("set-registration-state=false")){
            GcmRegistrationService.setDeviceRegistrationState(this, false);
        } else if(message != null && message.equals("data-update-true")) {
            Log.i("GCM","data-update-true");
            UpdateIntentService.startUpdateData(this);
        } else if(message != null && message.equals("reset-gcm-registration-pref")) {
            sharedPreferences.edit().putBoolean(Constants.PREF_IS_DEVICE_REGISTERED, false).apply();
        } else if(message != null && message.equals("show-must-update-dialog")) {
            sharedPreferences.edit().putBoolean(Constants.PREF_MUST_UPDATE, true).apply();
        } else if(message != null && message.contains("show-picture-style-notification")
                && sharedPreferences.getBoolean("notifications_new_message", true)) {
            String[] separated = message.split(";");
            new generatePictureStyleNotification(this, separated[1].trim(), separated[2].trim(),
                    vibrate, ringtoneUri, lightColor).execute();
        } else {
            if (sharedPreferences.getBoolean("notifications_new_message", true)) {
                PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                        MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.stat_notify_basmapp)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setColor(ThemeUtils.getThemeColor(this, R.attr.colorPrimary))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setLights(lightColor, 500, 2000)
                        .setSound(ringtoneUri)
                        .setContentIntent(notifyPendingIntent)
                        .setAutoCancel(true)
                        .setVibrate(vibrate);
                NotificationManager mNotificationManager=
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(1, mBuilder.build());
            }
        }
    }
}

class generatePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

    private Context mContext;
    private String message, imageUrl;
    private long[] vibrate;
    private Uri ringtoneUri;
    private int lightColor;

    public generatePictureStyleNotification(Context context, String message, String imageUrl, long[] vibrate, Uri ringtoneUri, int lightColor) {
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

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.stat_notify_basmapp)
                .setLargeIcon(result)
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
        NotificationManager mNotificationManager=
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(2, mBuilder.build());
    }
}
