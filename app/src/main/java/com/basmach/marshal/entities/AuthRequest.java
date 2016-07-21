package com.basmach.marshal.entities;

import android.util.Log;

import com.basmach.marshal.utils.HashUtil;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        DateFormat format = new SimpleDateFormat("dd/MM hh:mm", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String authBeforeHash = "marshaldevapps " + format.format(new Date());
        Log.i("AUTH", authBeforeHash);
        authReq = HashUtil.SHA(authBeforeHash);
        Log.i("AUTH", authReq);
    }
}
