package com.basmapp.marshal.services;


import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceIdService;

public class FcmIDListenerService extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        FcmRegistrationService.setDeviceRegistrationState(this, false);
        Intent intent = new Intent(this, FcmRegistrationService.class);
        intent.setAction(FcmRegistrationService.ACTION_REGISTER_OR_UPDATE);
        startService(intent);
    }

}