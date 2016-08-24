package com.basmapp.marshal.utils;

import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;

import com.basmapp.marshal.entities.AuthRequest;

import retrofit2.Response;

/**
 * Created by Ido on 8/24/2016.
 */
public class AuthUtil {
    public static String getApiToken() throws Exception {
        String token;
        AuthRequest authRequest = new AuthRequest();
        Response<String> authResponse = MarshalServiceProvider.getInstance(null).auth(authRequest).execute();
        if (authResponse.isSuccessful()) {
            token = authResponse.body();
            Log.i("AUTH", token);
            return token;
        } else {
            Log.e("AUTH", " RESPONSE ERROR");
            throw new Exception("RESPONSE ERROR");
        }
    }

    public static String getHardwareId(ContentResolver contentResolver) {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
    }
}
