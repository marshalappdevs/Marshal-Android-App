package com.basmapp.marshal.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.basmapp.marshal.R;
import com.basmapp.marshal.interfaces.GcmReceiverListener;

public class GcmRegistrationReceiver extends BroadcastReceiver {
    public static final String EXTRA_RESULT = "extra_result";
    public static final String ACTION_RESULT = "com.basmapp.marshal.services.action.GCM_REGISTRATION_RESULT";
    private GcmReceiverListener callback;

    public GcmRegistrationReceiver() {}

    public GcmRegistrationReceiver(GcmReceiverListener callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("GCM BR","onReceive");
        boolean result = intent.getBooleanExtra(EXTRA_RESULT, false);
        if (callback != null) {
            callback.onFinish(result);
//        } else {
//            publishResultToUI(result, context);
        }
    }
//
//    private void publishResultToUI(boolean result, final Context context) {
//        Handler handler = new Handler(context.getMainLooper());
//        if (result) {
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(context, context.getResources().getString(R.string.gcm_settings_change_success), Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(context, context.getResources().getString(R.string.gcm_settings_change_failed), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }
}
