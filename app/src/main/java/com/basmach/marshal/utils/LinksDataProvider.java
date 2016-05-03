package com.basmach.marshal.utils;

import android.os.AsyncTask;

import com.basmach.marshal.entities.MaterialItem;
import com.basmach.marshal.interfaces.MaterialLinkPreviewCallback;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;

/**
 * Created by Ido on 5/3/2016.
 */
public class LinksDataProvider extends AsyncTask<Void, MaterialItem, Void> {

    int mPosition;
    MaterialItem mMaterialItem;
    MaterialLinkPreviewCallback mCallback;

    TextCrawler textCrawler;

    public LinksDataProvider(int itemPosition, MaterialItem materialItem, MaterialLinkPreviewCallback callback) {
        this.mCallback = callback;
        this.mMaterialItem = materialItem;
        this.textCrawler = new TextCrawler();
        this.mPosition = itemPosition;
    }

    @Override
    protected void onProgressUpdate(MaterialItem... values) {
        super.onProgressUpdate(values);
        mCallback.onSuccess(mPosition, values[0]);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        mMaterialItem.getLinkData(textCrawler, new LinkPreviewCallback() {
            @Override
            public void onPre() {

            }

            @Override
            public void onPos(SourceContent sourceContent, boolean b) {
                if (sourceContent != null) {

                    if (sourceContent.getTitle() != null)
                        mMaterialItem.setTitle(sourceContent.getTitle());
                    if (sourceContent.getDescription() != null)
                        mMaterialItem.setDescription(sourceContent.getDescription());
                    if (sourceContent.getCannonicalUrl() != null)
                        mMaterialItem.setCannonicalUrl(sourceContent.getCannonicalUrl());
                    if (sourceContent.getImages() != null && sourceContent.getImages().size() > 0)
                        mMaterialItem.setImageUrl(sourceContent.getImages().get(0));
                    mMaterialItem.setGetLinkDataExecuted(true);
                    try {
                        mMaterialItem.save();
                        publishProgress(mMaterialItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return null;
    }
}
