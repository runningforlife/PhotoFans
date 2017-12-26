package com.github.runningforlife.photosniffer.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.data.cache.DiskCache;
import com.github.runningforlife.photosniffer.data.cache.DiskCacheManager;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;

/**
 * Created by jason on 12/17/17.
 */

public class FullScreenPresenterImpl implements FullScreenPresenter, ImageSaveRunnable.ImageSaveCallback {
    private static final String TAG = "FullScreenPresenter";

    private Context mContext;
    private RealmApi mRealmApi;
    private RealmResults mImgList;
    private ExecutorService mImageExecutor;

    public FullScreenPresenterImpl(Context context) {
        mContext = context;
        mRealmApi = RealmApiImpl.getInstance();
        mImageExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onStart() {
        HashMap<String,String> params = new HashMap<>();
        params.put("mIsFavor", Boolean.toString(Boolean.TRUE));
        params.put("mIsWallpaper", Boolean.toString(Boolean.TRUE));

        mImgList = mRealmApi.queryAsync(ImageRealm.class, params);
        mImgList.addChangeListener(new OrderReamChangeListener());
    }

    @Override
    public void onDestroy() {
        mImgList.removeAllChangeListeners();
        mRealmApi.closeRealm();
    }

    //FIXME: also cache it
    @Override
    public void setAsWallpaper(String url) {
        // mark it as wall paper
        HashMap<String,String> params = new HashMap<>();
        HashMap<String, String> updated = new HashMap<>();

        params.put("mUrl", DiskCacheManager.getInstance().getFilePath(url));
        updated.put("mIsWallpaper", Boolean.toString(true));
        updated.put("mIsCached", Boolean.toString(Boolean.TRUE));
        mRealmApi.updateAsync(ImageRealm.class, params, updated);
    }

    @Override
    public void saveImage(final String url) {
        GlideLoaderListener listener = new GlideLoaderListener();
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG, "onImageLoadDone()");
                if(o instanceof Bitmap) {
                    ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap) o));
                    mImageExecutor.submit(r);
                }
            }
        });

        GlideLoader.downloadOnly(mContext, url, listener, Priority.HIGH,
                Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT, false);
    }

    @Override
    public void onImageSaveDone(String path) {

    }

    private final class OrderReamChangeListener implements OrderedRealmCollectionChangeListener<RealmResults<ImageRealm>> {

        @Override
        public void onChange(RealmResults<ImageRealm> images, OrderedCollectionChangeSet changeSet) {
            Log.v(TAG,"onChange(): size = " + images.size());
            //images.sort("mTimeStamp", Sort.DESCENDING);
            if (changeSet == null) {
                mImgList = images;
            }
        }
    }
}
