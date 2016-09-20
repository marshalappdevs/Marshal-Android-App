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
import com.basmapp.marshal.ui.utils.NotificationUtils;
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
            new NotificationUtils.GeneratePictureStyleNotification(this, separated[1].trim(), separated[2].trim(),
                    vibrate, ringtoneUri, lightColor).execute();
        } else {
            if (sharedPreferences.getBoolean("notifications_new_message", true)) {
                PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                        MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationUtils.Notify(this, message, lightColor, ringtoneUri,
                        notifyPendingIntent, vibrate);
            }
        }
    }
}
