package com.basmach.marshal.utils;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.LinkContent;
import com.basmach.marshal.interfaces.LinkPreviewCallback;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by Ido on 3/19/2016.
 */
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

    public void setData(String title,String description,String image, String site)
    {
        mTitle=title;
        mDescription=description;
        mImageLink=image;
        mSiteName=site;
        if (mTitle != null) {
            Log.v(TAG, mTitle);
            if(mTitle.length()>=50) {
                mTitle=mTitle.substring(0,49)+"...";
            }
            mLinkContent.setTitle(mTitle);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mTxtViewTitle.setText(mTitle);
//                }
//            });
        }
        if (mDescription != null) {
            Log.v(TAG, mDescription);
            if(mDescription.length()>=100) {
                mDescription=mDescription.substring(0,99)+"...";
            }
            mLinkContent.setDescription(mDescription);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mTxtViewDescription.setText(mDescription);
//                }
//            });
        }
        if (mImageLink != null) {
            Log.v(TAG, mImageLink);
            mLinkContent.setImageUrl(mImageLink);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Glide.with(mContext)
//                            .load(mImageLink)
//                            .into(mImgViewImage);
//                }
//            });
        }
        if (mSiteName != null) {
            Log.v(TAG, mSiteName);
            if(mSiteName.length()>=30) {
                mSiteName=mSiteName.substring(0,29)+"...";
            }
            mLinkContent.setSiteName(mSiteName);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mTxtViewSiteName.setText(mSiteName);
//                }
//            });
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
            @Override public void onFailure(Request request, IOException throwable) {
                Log.e(TAG,throwable.getMessage());
                mLinkPreviewCallback.onFailure();
            }

            @Override public void onResponse(Response response) throws IOException {
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
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mTxtViewTitle.setText(mTitle);
//                        }
//                    });
                }
                if (mDescription != null) {
                    Log.v(TAG, mDescription);
                    if(mDescription.length()>=100) {
                        mDescription=mDescription.substring(0,99)+"...";
                    }
                    mLinkContent.setDescription(mDescription);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mTxtViewDescription.setText(mDescription);
//                        }
//                    });
                }
                if (mImageLink != null) {
                    Log.v(TAG, mImageLink);
                    mLinkContent.setImageUrl(mImageLink);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Picasso.with(mContext)
//                                    .load(mImageLink)
//                                    .placeholder(R.drawable.link_image_placeholder)
//                                    .into(mImageView);
//                        }
//                    });
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
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mTxtViewSiteName.setText(mSiteName);
//                        }
//                    });
                }

                mLinkPreviewCallback.onSuccess(mLinkContent, itemPosition);
            }
        });
    }

    private void runOnUiThread(Runnable r) {
        mHandler.post(r);
    }
}
