package com.basmach.marshal.interfaces;

import com.basmach.marshal.entities.LinkContent;

public interface LinkPreviewCallback {
    void onFailure();
    void onSuccess(LinkContent linkContent, int itemPosition);
}
