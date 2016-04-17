package com.basmach.marshal.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.basmach.marshal.interfaces.OnReceiveListener;
import com.basmach.marshal.services.UpdateIntentService;

public class UpdateBroadcastReceiver extends BroadcastReceiver {

    Context mContext;
    OnReceiveListener mOnReceiveListener;

    public UpdateBroadcastReceiver(Context context, OnReceiveListener onReceiveListener) {
        this.mContext = context;
        this.mOnReceiveListener = onReceiveListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(UpdateIntentService.ACTION_CHECK_FOR_UPDATE)) {
            boolean result = intent.getBooleanExtra(UpdateIntentService.RESULT_CHECK_FOR_UPDATE, false);
            if (result) {
                Toast.makeText(context, R.string.refresh_new_update, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, R.string.refresh_no_update, Toast.LENGTH_SHORT).show();
            }
        }
        else if (intent.getAction().equals(UpdateIntentService.ACTION_UPDATE_DATA)) {
            Toast.makeText(context, R.string.refresh_data_updated, Toast.LENGTH_SHORT).show();
        }

        mOnReceiveListener.onReceive();
    }
}
