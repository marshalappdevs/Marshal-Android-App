package com.basmapp.marshal.interfaces;

import com.simplite.orm.DBObject;
import java.util.ArrayList;

public interface ContentProviderCallBack {
    void onDataReady(ArrayList<? extends DBObject> data, Object extra);

    void onError(Exception e);
}
