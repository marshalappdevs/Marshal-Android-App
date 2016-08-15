package com.basmapp.marshal.interfaces;

import com.basmapp.marshal.entities.MaterialItem;

public interface MaterialLinkPreviewCallback {
    void onSuccess(int position, MaterialItem materialItem);
}
