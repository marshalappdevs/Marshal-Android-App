package com.basmach.marshal.interfaces;

public interface UpdateServiceListener {
    void onFinish(boolean result);
    void onProgressUpdate(String message, int progressPercents);
}
