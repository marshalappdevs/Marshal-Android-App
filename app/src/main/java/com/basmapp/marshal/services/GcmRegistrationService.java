package com.basmapp.marshal.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.GcmRegistration;
import com.basmapp.marshal.utils.MarshalServiceProvider;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.Date;

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

    private static final String PREFERENCE_IS_DEVICE_REGISTERED = "is_device_registered_to_gcm";

    public GcmRegistrationService() {
        super("GcmRegisterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GcmRegistration gcmRegistration= new GcmRegistration();
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            String apiToken = UpdateIntentService.getApiToken();
            String hardwareId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            if(hardwareId != null) {
                String token = instanceID.getToken(this.getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                if (token != null) {
                    gcmRegistration.setRegisterationTokenId(token);
                    gcmRegistration.setHardwareId(hardwareId);
                    gcmRegistration.setLastModified(new Date());

                    if (intent != null) {
                        final String action = intent.getAction();
                        if (ACTION_REGISTER_NEW.equals(action)) {
                            Response<GcmRegistration> response =
                                    MarshalServiceProvider.getInstance(apiToken).gcmRegisterNewDevice(gcmRegistration).execute();
                            publishResponse(response);
                        } else if (ACTION_REGISTER_EXIST.equals(action)) {
                            Response<GcmRegistration> response =
                                    MarshalServiceProvider.getInstance(apiToken).gcmRegisterExistDevice(gcmRegistration).execute();
                            publishResponse(response);
                        }
                    }
                } else {
                    Log.e("GCM_REGISTRATION -- ","NULL TOKEN");
                }
            } else {
                Log.e("GCM_REGISTRATION -- ","NULL HARDWARE_ID");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("GCM_REGISTRATION -- ","failed");
        }
    }

    private void publishResponse(Response<GcmRegistration> response) {
        if (response.isSuccessful()) {
            Log.d("GCM_RESPONSE -- ","success");
            GcmRegistrationService.setDeviceRegistrationState(this, true);
        } else {
            Log.d("GCM_RESPONSE -- ","failed");
            GcmRegistrationService.setDeviceRegistrationState(this, false);
        }
    }

    public static boolean isDeviceRegistered(Context context) {
        boolean state = false;
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            state = sharedPreferences.getBoolean(PREFERENCE_IS_DEVICE_REGISTERED,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return state;
    }

    public static void setDeviceRegistrationState(Context context, boolean state){
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putBoolean(PREFERENCE_IS_DEVICE_REGISTERED, state).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    /**
//     * Handle action Foo in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleActionRegister(String param1, String param2) {
//        // TODO: Handle action Foo
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    /**
//     * Handle action Baz in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleActionBaz(String param1, String param2) {
//        // TODO: Handle action Baz
//    }
}
