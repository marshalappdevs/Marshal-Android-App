package com.basmapp.marshal.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class GcmRegistration {
    @Expose
    @SerializedName("registerationTokenId")
    private String registerationTokenId;

    @Expose
    @SerializedName("hardwareId")
    private String hardwareId;

    @Expose
    @SerializedName("lastModified")
    private Date lastModified;

    public String getRegisterationTokenId() {
        return registerationTokenId;
    }

    public void setRegisterationTokenId(String registerationTokenId) {
        this.registerationTokenId = registerationTokenId;
    }

    public String getHardwareId() {
        return hardwareId;
    }

    public void setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
