package com.basmapp.marshal.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.FcmRegistration;
import com.basmapp.marshal.receivers.FcmRegistrationReceiver;
import com.basmapp.marshal.util.AuthUtil;
import com.basmapp.marshal.util.MarshalServiceProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Response;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class FcmRegistrationService extends IntentService {

    public static final String ACTION_REGISTER_OR_UPDATE = "com.basmapp.marshal.services.action.FCM_REGISTER_OR_UPDATE";
    public static final String ACTION_UPDATE_CHANNELS = "com.basmapp.marshal.services.action.FCM_UPDATE_CHANNELS";

    private static final Set<String> DEFAULT_CHANNELS_SET = new HashSet<>();

    public FcmRegistrationService() {
        super("FcmRegisterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();

        if (action != null) {
            //TODO
            DEFAULT_CHANNELS_SET.add(getResources().getString(R.string.fcm_channel_software));
            DEFAULT_CHANNELS_SET.add(getResources().getString(R.string.fcm_channel_system));
            DEFAULT_CHANNELS_SET.add(getResources().getString(R.string.fcm_channel_cyber));
            DEFAULT_CHANNELS_SET.add(getResources().getString(R.string.fcm_channel_it));
            DEFAULT_CHANNELS_SET.add(getResources().getString(R.string.fcm_channel_tools));

            FcmRegistration fcmRegistration = new FcmRegistration();
            try {
                String fcmToken = FirebaseInstanceId.getInstance().getToken();
                String apiToken = AuthUtil.getApiToken();
                String hardwareId = AuthUtil.getHardwareId(getContentResolver());
                if (hardwareId != null) {
                    if (fcmToken != null) {
                        fcmRegistration.setRegistrationTokenId(fcmToken);
                        fcmRegistration.setHardwareId(hardwareId);
                        fcmRegistration.setLastModified(new Date());
                        Set<String> channels = PreferenceManager.getDefaultSharedPreferences(this)
                                .getStringSet(Constants.PREF_FCM_CHANNELS, DEFAULT_CHANNELS_SET);
                        fcmRegistration.setChannels(new ArrayList<>(channels));
                        if (ACTION_REGISTER_OR_UPDATE.equals(action)) {
                            Response<FcmRegistration> response =
                                    MarshalServiceProvider.getInstance(apiToken).fcmRegisterNewDevice(fcmRegistration).execute();
                            publishResult(response.isSuccessful(), false);
                        } else if (ACTION_UPDATE_CHANNELS.equals(action)) {
                            Set<String> newChannels = null;
                            try {
                                newChannels = (HashSet<String>) intent.getExtras()
                                        .get(Constants.EXTRA_FCM_CHANNELS);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (newChannels != null) {
                                fcmRegistration.setChannels(new ArrayList<>(newChannels));
                                Response<FcmRegistration> response =
                                        MarshalServiceProvider.getInstance(apiToken).fcmRegisterNewDevice(fcmRegistration).execute();
                                if (response.isSuccessful()) {
                                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                                            .putStringSet(Constants.PREF_FCM_CHANNELS, newChannels).apply();
                                }

                                publishResult(response.isSuccessful(), true);

                            } else publishResult(false, true);
                        }
                    } else {
                        if (action.equals(ACTION_UPDATE_CHANNELS)) publishResult(false, true);
                    }
                } else {
                    if (action.equals(ACTION_UPDATE_CHANNELS)) publishResult(false, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (action.equals(ACTION_UPDATE_CHANNELS)) publishResult(false, true);
            }
        } else {
            publishResult(false, true);
        }
    }

    private void publishResult(boolean result, boolean showResultToUser) {
        if (result) {
            Log.d("FCM_RESPONSE -- ", "success");
            FcmRegistrationService.setDeviceRegistrationState(this, true);
        } else {
            Log.d("FCM_RESPONSE -- ", "failed");
            FcmRegistrationService.setDeviceRegistrationState(this, false);
        }

        if (showResultToUser) {
            Intent intent = new Intent(FcmRegistrationReceiver.ACTION_RESULT);
            intent.putExtra(FcmRegistrationReceiver.EXTRA_RESULT, result);
            sendBroadcast(intent);
        }
    }

    public static boolean isDeviceRegistered(Context context) {
        boolean state = false;
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            state = sharedPreferences.getBoolean(Constants.PREF_IS_DEVICE_REGISTERED, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return state;
    }

    public static void setDeviceRegistrationState(Context context, boolean state) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putBoolean(Constants.PREF_IS_DEVICE_REGISTERED, state).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
