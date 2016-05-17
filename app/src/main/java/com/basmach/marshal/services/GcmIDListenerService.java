package com.basmach.marshal.services;


import android.content.Intent;
import com.google.android.gms.iid.InstanceIDListenerService;

public class GcmIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        GcmRegistrationService.setDeviceRegistrationState(this, false);
        Intent intent = new Intent(this, GcmRegistrationService.class);
        intent.setAction(GcmRegistrationService.ACTION_REGISTER_EXIST);
        startService(intent);
    }
}