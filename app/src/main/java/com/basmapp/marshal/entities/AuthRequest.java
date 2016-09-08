package com.basmapp.marshal.entities;

import com.basmapp.marshal.BuildConfig;
import com.basmapp.marshal.util.HashUtil;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AuthRequest {

    @Expose
    @SerializedName("authReq")
    private String authReq;

    public String getAuthReq() {
        return authReq;
    }

    public void setAuthReq(String authReq) {
        this.authReq = authReq;
    }

    public AuthRequest() {
        setAuthHash();
    }

    private void setAuthHash() {
        DateFormat format = new SimpleDateFormat("dd/MM kk:mm", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String authBeforeHash = BuildConfig.AUTH_KEY + " " + format.format(new Date());
        authReq = HashUtil.SHA(authBeforeHash);
    }
}
