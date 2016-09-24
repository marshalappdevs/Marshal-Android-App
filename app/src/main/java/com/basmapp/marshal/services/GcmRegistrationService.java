package com.basmapp.marshal.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.GcmRegistration;
import com.basmapp.marshal.receivers.GcmRegistrationReceiver;
import com.basmapp.marshal.util.AuthUtil;
import com.basmapp.marshal.util.MarshalServiceProvider;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

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
public class GcmRegistrationService extends IntentService {

    public static final String ACTION_REGISTER_NEW = "com.basmapp.marshal.services.action.GCM_REGISTER_NEW";
    public static final String ACTION_REGISTER_EXIST = "com.basmapp.marshal.services.action.GCM_REGISTER_EXIST";
    public static final String ACTION_UPDATE_CHANNELS = "com.basmapp.marshal.services.action.GCM_UPDATE_CHANNELS";

    private static final Set<String> DEFAULT_CHANNELS_SET = new HashSet<>();

    public GcmRegistrationService() {
        super("GcmRegisterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();

        if (action != null) {
            DEFAULT_CHANNELS_SET.add(getResources().getString(R.string.gcm_channel_software));
            DEFAULT_CHANNELS_SET.add(getResources().getString(R.string.gcm_channel_system));
            DEFAULT_CHANNELS_SET.add(getResources().getString(R.string.gcm_channel_cyber));
            DEFAULT_CHANNELS_SET.add(getResources().getString(R.string.gcm_channel_it));
            DEFAULT_CHANNELS_SET.add(getResources().getString(R.string.gcm_channel_tools));

            GcmRegistration gcmRegistration = new GcmRegistration();
            InstanceID instanceID = InstanceID.getInstance(this);
            try {
                String apiToken = AuthUtil.getApiToken();
                String hardwareId = AuthUtil.getHardwareId(getContentResolver());
                if (hardwareId != null) {
                    String token = instanceID.getToken(this.getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    if (token != null) {
                        gcmRegistration.setRegistrationTokenId(token);
                        gcmRegistration.setHardwareId(hardwareId);
                        gcmRegistration.setLastModified(new Date());
                        Set<String> channels = PreferenceManager.getDefaultSharedPreferences(this)
                                .getStringSet(Constants.PREF_GCM_CHANNELS, DEFAULT_CHANNELS_SET);
                        gcmRegistration.setChannels(new ArrayList<>(channels));

                        if (ACTION_REGISTER_NEW.equals(action)) {
                            Response<GcmRegistration> response =
                                    MarshalServiceProvider.getInstance(apiToken).gcmRegisterNewDevice(gcmRegistration).execute();
                            publishResult(response.isSuccessful(), false);
                        } else if (ACTION_REGISTER_EXIST.equals(action)) {
                            Response<GcmRegistration> response =
                                    MarshalServiceProvider.getInstance(apiToken).gcmRegisterExistDevice(gcmRegistration).execute();
                            publishResult(response.isSuccessful(), false);
                        } else if (ACTION_UPDATE_CHANNELS.equals(action)) {
                            Set<String> newChannels = null;
                            try {
                                newChannels = (HashSet<String>) intent.getExtras()
                                        .get(Constants.EXTRA_GCM_CHANNELS);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (newChannels != null) {
                                gcmRegistration.setChannels(new ArrayList<>(newChannels));
                                Response<GcmRegistration> response =
                                        MarshalServiceProvider.getInstance(apiToken).gcmRegisterExistDevice(gcmRegistration).execute();
                                if (response.isSuccessful()) {
                                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                                            .putStringSet(Constants.PREF_GCM_CHANNELS, newChannels).apply();
                                }

                                publishResult(response.isSuccessful(), true);

                            } else publishResult(false, true);
                        }
                    } else {
                        Log.e("GCM_REGISTRATION -- ", "NULL TOKEN");
                        if (action.equals(ACTION_UPDATE_CHANNELS)) publishResult(false, true);
                    }
                } else {
                    Log.e("GCM_REGISTRATION -- ", "NULL HARDWARE_ID");
                    if (action.equals(ACTION_UPDATE_CHANNELS)) publishResult(false, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("GCM_REGISTRATION -- ", "failed");
                if (action.equals(ACTION_UPDATE_CHANNELS)) publishResult(false, true);
            }
        } else {
            publishResult(false, true);
        }
    }

    private void publishResult(boolean result, boolean showResultToUser) {
        if (result) {
            Log.d("GCM_RESPONSE -- ", "success");
            GcmRegistrationService.setDeviceRegistrationState(this, true);
        } else {
            Log.d("GCM_RESPONSE -- ", "failed");
            GcmRegistrationService.setDeviceRegistrationState(this, false);
        }

        if (showResultToUser) {
            Intent intent = new Intent(GcmRegistrationReceiver.ACTION_RESULT);
            intent.putExtra(GcmRegistrationReceiver.EXTRA_RESULT, result);
            sendBroadcast(intent);
        }
    }

//    private void publishResponse(Response<GcmRegistration> response, boolean showResultToUser) {
//        if (response.isSuccessful()) {
//            Log.d("GCM_RESPONSE -- ","success");
//            GcmRegistrationService.setDeviceRegistrationState(this, true);
//        } else {
//            Log.d("GCM_RESPONSE -- ","failed");
//            GcmRegistrationService.setDeviceRegistrationState(this, false);
//        }
//
//        if (showResultToUser) publishResultToUI(response.isSuccessful());
//    }

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
