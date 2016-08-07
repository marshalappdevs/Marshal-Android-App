package com.basmapp.marshal.interfaces;

import com.basmapp.marshal.entities.MaterialItem;

/**
 * Created by Ido on 5/3/2016.
 */
public interface MaterialLinkPreviewCallback {
    void onSuccess(int position, MaterialItem materialItem);
}
