package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.local.RealmManager;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.ui.UI;
import com.github.runningforlife.photosniffer.ui.WallpaperView;

import java.io.IOException;
import java.util.HashMap;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * base class for presenter
 */

abstract class PresenterBase implements Presenter {
    private static final String TAG = "PresenterBase";

    Context mContext;
    UI mView;

    RealmResults<ImageRealm> mImageList;
    RealmManager mRealmMgr;
    RealmApi mRealmApi;

    OrderReamChangeListener mOrderRealmChangeListener;

    PresenterBase(Context context, UI view) {
        mContext = context;
        mView = view;
        mRealmMgr = RealmManager.getInstance();
        mOrderRealmChangeListener = new OrderReamChangeListener();
        mRealmApi = RealmApiImpl.getInstance();
    }

    void setWallpaper(String url) {
        Log.v(TAG,"setWallpaper()");

        if(TextUtils.isEmpty(url)) return;

        // for pixels, we can use URL to download hd images
        if (url.startsWith(ImageSource.PIXELS_IMAGE_START)) {
            int res = dm.heightPixels/2 > 1024 ?  1024 : dm.heightPixels;
            url = buildHighResolutionPixelsUrl(url, res);
        }

        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG,"onImageLoadDone()");
                if (o instanceof Bitmap) {
                    WallpaperManager wpm = WallpaperManager.getInstance(mContext);
                    try {
                        wpm.setBitmap((Bitmap)o);
                        mView.onWallpaperSetDone(true);
                    } catch (IOException e) {
                        mView.onWallpaperSetDone(false);
                        e.printStackTrace();
                    }
                } else {
                    //FIXME: try again
                    mView.onWallpaperSetDone(false);
                }
            }
        });
        GlideLoader.downloadOnly(mContext, url, listener, Priority.HIGH,
                dm.widthPixels , dm.heightPixels, true);

        //markAsWallpaper(url);
    }

    void markAsFavor(String url) {
        Log.v(TAG,"markAsFavor()");
        // mark it as wall paper
        HashMap<String,String> params = new HashMap<>();
        HashMap<String, String> updated = new HashMap<>();

        params.put("mUrl", url);
        updated.put("mIsFavor", Boolean.toString(true));
        mRealmApi.updateAsync(ImageRealm.class, params, updated);
    }

    void markAsWallpaper(String url) {
        Log.v(TAG,"markAsWallpaper()");
        // mark it as wall paper
        HashMap<String,String> params = new HashMap<>();
        HashMap<String, String> updated = new HashMap<>();

        params.put("mUrl", url);
        updated.put("mIsWallpaper", Boolean.toString(true));
        mRealmApi.updateAsync(ImageRealm.class, params, updated);
    }

    private String buildHighResolutionPixelsUrl(String url, int px){
        int hIdx = url.indexOf("?");

        return url.substring(0, hIdx)
                + "?"
                + "h=" + px
                + "&auto=compress"
                + "&cs=tinysrgb";
    }


    private final class OrderReamChangeListener implements OrderedRealmCollectionChangeListener<RealmResults<ImageRealm>> {

        @Override
        public void onChange(RealmResults<ImageRealm> images, OrderedCollectionChangeSet changeSet) {
            Log.v(TAG,"onChange(): size = " + images.size());
            //images.sort("mTimeStamp", Sort.DESCENDING);
            if (changeSet == null) {
                mImageList = images;
                mView.onDataSetChange(0, mImageList.size(), RealmOp.OP_REFRESH);
            } else {
                // For deletions, the adapter has to be notified in reverse order.
                OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
                for (int i = deletions.length - 1; i >= 0; i--) {
                    OrderedCollectionChangeSet.Range range = deletions[i];
                    mView.onDataSetChange(range.startIndex, range.length, RealmOp.OP_DELETE);
                }

                // insert
                OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
                for (OrderedCollectionChangeSet.Range range : insertions) {
                    mView.onDataSetChange(range.startIndex, range.length, RealmOp.OP_INSERT);
                }

                // modification
                OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
                for (OrderedCollectionChangeSet.Range range : modifications) {
                    mView.onDataSetChange(range.startIndex, range.length, RealmOp.OP_MODIFY);
                }
            }
        }
    }
}
