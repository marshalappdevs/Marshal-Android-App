package com.basmapp.marshal.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

public class Settings {

    @Expose
    @SerializedName("lastUpdateAt")
    Date lastUpdateAt;

    @Expose
    @SerializedName("minVersion")
    int minVersion;

    @Expose
    @SerializedName("channels")
    ArrayList<String> channels;

    @Expose
    @SerializedName("categories")
    ArrayList<String> categories;

    public Date getLastUpdateAt() {
        return lastUpdateAt;
    }

    public int getMinVersion() {
        return minVersion;
    }

    public ArrayList<String> getChannels() {
        return channels;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }
}
