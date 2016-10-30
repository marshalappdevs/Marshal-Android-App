package com.basmapp.marshal.util;

import android.content.ContentResolver;
import android.provider.Settings;
import com.basmapp.marshal.BuildConfig;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class AuthUtil {
    public static String getApiToken() throws Exception {
        String testToken = Jwts.builder()
                .setSubject("hila")
                .signWith(SignatureAlgorithm.HS256, BuildConfig.AUTH_KEY.getBytes())
                .setExpiration(new Date(new Date().getTime() + 10000))
                .compact();
        return testToken;
    }

    public static String getHardwareId(ContentResolver contentResolver) {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
    }
}
