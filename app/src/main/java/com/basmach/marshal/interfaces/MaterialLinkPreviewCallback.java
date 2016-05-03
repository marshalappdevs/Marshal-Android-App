package com.basmach.marshal.interfaces;

import com.basmach.marshal.entities.MaterialItem;

/**
 * Created by Ido on 5/3/2016.
 */
public interface MaterialLinkPreviewCallback {
    void onSuccess(int position, MaterialItem materialItem);
}
