package com.basmach.marshal.interfaces;

public interface UpdateServiceListener {
    void onFinish();
    void onProgressUpdate(String message, int progressPercents);
}
