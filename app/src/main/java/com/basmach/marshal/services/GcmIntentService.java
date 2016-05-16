package com.basmach.marshal.services;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.basmach.marshal.R;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by Ido on 5/16/2016.
 */
public class GcmIntentService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        Log.i("GCM", "onMessageReceived from:" + from);

        String message = data.getString("message");
        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_drawer_info)
                .setContentTitle("Marshal")
                .setContentText(message);
        notificationManager.notify(1, mBuilder.build());
    }
}
