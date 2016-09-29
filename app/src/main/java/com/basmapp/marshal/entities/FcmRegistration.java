package com.basmapp.marshal.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

public class FcmRegistration {

    @Expose
    @SerializedName("registrationTokenId")
    private String registrationTokenId;

    @Expose
    @SerializedName("hardwareId")
    private String hardwareId;

    @Expose
    @SerializedName("lastModified")
    private Date lastModified;

    @Expose
    @SerializedName("channels")
    private ArrayList<String> channels;

    @Expose
    @SerializedName("courses")
    private ArrayList<String> courses;

    public String getRegistrationTokenId() {
        return registrationTokenId;
    }

    public void setRegistrationTokenId(String registrationTokenId) {
        this.registrationTokenId = registrationTokenId;
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

    public ArrayList<String> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<String> channels) {
        this.channels = channels;
    }

    public ArrayList<String> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<String> courses) {
        this.courses = courses;
    }
}
