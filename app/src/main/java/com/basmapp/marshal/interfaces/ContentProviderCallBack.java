package com.basmapp.marshal.interfaces;

import com.basmapp.marshal.localdb.DBObject;

import java.util.ArrayList;

public interface ContentProviderCallBack {
    void onDataReady(ArrayList<? extends DBObject> data, Object extra);

    void onError(Exception e);
}
