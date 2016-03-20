package com.basmach.marshal.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.basmach.marshal.entities.LinkContent;
import com.basmach.marshal.interfaces.LinkPreviewCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LinkPreviewUtil {
    private static String TAG = LinkPreviewUtil.class.getSimpleName();
    private Context mContext;
    private Handler mHandler;
    private String mTitle = null;
    private String mDescription = null;
    private String mImageLink = null;
    private String mSiteName = null;
    private LinkContent mLinkContent = null;
    private LinkPreviewCallback mLinkPreviewCallback = null;

    public LinkPreviewUtil(Context context) {
        this.mContext = context;
        this.mHandler = new Handler(mContext.getMainLooper());
        this.mLinkContent = new LinkContent();
    }

    public void setData(String title,String description,String image, String site) {
        mTitle = title;
        mDescription = description;
        mImageLink = image;
        mSiteName = site;
        if (mTitle != null) {
            Log.v(TAG, mTitle);
            if (mTitle.length() >= 50) {
                mTitle = mTitle.substring(0, 49) + "...";
            }
            mLinkContent.setTitle(mTitle);
        }
        if (mDescription != null) {
            Log.v(TAG, mDescription);
            if (mDescription.length() >= 100) {
                mDescription = mDescription.substring(0, 99) + "...";
            }
            mLinkContent.setDescription(mDescription);
        }
        if (mImageLink != null) {
            Log.v(TAG, mImageLink);
            mLinkContent.setImageUrl(mImageLink);
            if (mSiteName != null) {
                Log.v(TAG, mSiteName);
                if (mSiteName.length() >= 30) {
                    mSiteName = mSiteName.substring(0, 29) + "...";
                }
                mLinkContent.setSiteName(mSiteName);
            }
        }
    }

    public void getData(final String url, final int itemPosition, LinkPreviewCallback callback)
    {
        mLinkPreviewCallback = callback;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,e.getMessage());
                mLinkPreviewCallback.onFailure();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Elements titleElements;
                Elements descriptionElements;
                Elements imageElements;
                Elements siteElements;
                Document doc = null;
                doc = Jsoup.parse(response.body().string());
                titleElements = doc.select("title");
                descriptionElements = doc.select("meta[name=description]");
                imageElements = doc.select("meta[property=og:image]");
                siteElements=doc.select("meta[property=og:site_name]");

                if (titleElements != null && titleElements.size() > 0) {
                    mTitle = titleElements.get(0).text();
                }
                if (descriptionElements != null && descriptionElements.size() > 0) {
                    mDescription = descriptionElements.get(0).attr("content");
                }
                if (imageElements != null && imageElements.size() > 0) {
                    mImageLink = imageElements.get(0).attr("content");
                }
                else
                {
                    imageElements = doc.select("img[data-old-hires]");
                    if (imageElements != null && imageElements.size() > 0) {
                        mImageLink = imageElements.get(0).attr("data-old-hires");
                    }
                }
                if(siteElements!=null&& siteElements.size()>0)
                {
                    mSiteName = siteElements.get(0).attr("content");
                }

                if (mTitle != null) {
                    Log.v(TAG, mTitle);
                    if(mTitle.length()>=50) {
                        mTitle=mTitle.substring(0,49)+"...";
                    }
                    mLinkContent.setTitle(mTitle);
                }
                if (mDescription != null) {
                    Log.v(TAG, mDescription);
                    if(mDescription.length()>=100) {
                        mDescription=mDescription.substring(0,99)+"...";
                    }
                    mLinkContent.setDescription(mDescription);
                }
                if (mImageLink != null) {
                    Log.v(TAG, mImageLink);
                    mLinkContent.setImageUrl(mImageLink);
                }
                if(url.toLowerCase().contains("amazon"))
                    if(mSiteName==null||mSiteName.equals(""))
                        mSiteName="Amazon";
                if (mSiteName != null) {
                    Log.v(TAG, mSiteName);
                    if(mSiteName.length()>=30) {
                        mSiteName=mSiteName.substring(0,29)+"...";
                    }
                    mLinkContent.setSiteName(mSiteName);
                }

                mLinkPreviewCallback.onSuccess(mLinkContent, itemPosition);
            }
        });
    }

    private void runOnUiThread(Runnable r) {
        mHandler.post(r);
    }
}
