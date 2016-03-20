package com.basmach.marshal.interfaces;

import com.basmach.marshal.entities.LinkContent;

/**
 * Created by Ido on 3/19/2016.
 */
public interface LinkPreviewCallback {
    void onFailure();
    void onSuccess(LinkContent linkContent, int itemPosition);
}
