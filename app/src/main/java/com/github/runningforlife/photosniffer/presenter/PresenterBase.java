package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;
import com.github.runningforlife.photosniffer.data.cache.Cache;
import com.github.runningforlife.photosniffer.data.cache.CacheApi;
import com.github.runningforlife.photosniffer.data.cache.DiskCacheManager;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.ui.UI;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import static com.github.runningforlife.photosniffer.presenter.ImageSaveRunnable.*;

/**
 * base class for presenter
 */

abstract class PresenterBase implements Presenter, ImageSaveCallback{
    private static final String TAG = "PresenterBase";

    private int mNetworkErrorCount;
    // for image save or wallpaper setting
    private ExecutorService mImageExecutor;
    //DiskCache mDiskCache;
    private CacheApi mCacheMgr;
    private OkHttpClient mHttpClient;
    private boolean mIsNetworkStateReported;

    int mMaxImagesAllowed;

    Context mContext;
    UI mView;

    RealmResults<ImageRealm> mImageList;
    RealmApi mRealmApi;

    OrderReamChangeListener mOrderRealmChangeListener;

    PresenterBase(Context context, UI view) {
        mContext = context;
        mView = view;
        mOrderRealmChangeListener = new OrderReamChangeListener();
        mRealmApi = RealmApiImpl.getInstance();
        //mDiskCache = new DiskCache(new File(MiscUtil.getWallpaperCacheDir()));
        mCacheMgr = DiskCacheManager.getInstance();

        mNetworkErrorCount = 0;
        mImageExecutor = Executors.newSingleThreadExecutor();

        // default value
        mMaxImagesAllowed = Integer.MAX_VALUE;

        mIsNetworkStateReported = false;
    }


    @Override
    public void onImageLoadStart(int pos) {
       // for detail presenter to use
    }

    @Override
    public void onImageLoadDone(int pos, boolean isSuccess) {
        // for detail presenter to use
    }

    @Override
    public int getItemCount() {
        return mImageList.size();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mImageList.get(pos);
    }

    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone()");
        mView.onImageSaveDone(path);
    }

    @Override
    public void setWallpaperAtPos(int pos) {
        if(pos >= 0 && pos < mImageList.size()) {
            String url = mImageList.get(pos).getUrl();
            setWallpaper(url);
        }
    }

    @Override
    public void saveImageAtPos(final int pos) {
        if (pos >= 0 && pos < mImageList.size()) {
            GlideLoaderListener listener = new GlideLoaderListener(null);
            listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
                @Override
                public void onImageLoadDone(Object o) {
                    Log.d(TAG, "onImageLoadDone()");
                    if(o instanceof Bitmap) {
                        ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap) o), mImageList.get(pos).getName());
                        r.addCallback(PresenterBase.this);
                        mImageExecutor.submit(r);
                    }
                }
            });

            String imgUrl = mImageList.get(pos).getUrl();
            GlideLoader.downloadOnly(mContext, imgUrl, listener,Priority.HIGH,
                    Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT, false);
        }
    }

    @Override
    public void removeItemAtPos(int pos) {
        Log.d(TAG,"removeItemAtPos()");
        if(pos >= 0 && pos < mImageList.size()) {
            final ImageRealm ir = mImageList.get(pos);
            String url = ir.getUrl();
            // donot remove user selected images;
            String wallpaperDir = MiscUtil.getWallpaperCacheDir();
            if (mCacheMgr.isExist(url) && url.startsWith(wallpaperDir)) {
                mCacheMgr.remove(url);
            }
            mRealmApi.deleteSync(ir);
        }
    }

    @Override
    public void loadImageIntoView(final int pos, final ImageView iv, Priority priority, int w, int h, ImageView.ScaleType scaleType) {
        Log.v(TAG,"loadImageIntoView()");
        final String url = mImageList.get(pos).getUrl();
        GlideLoaderListener listener = new GlideLoaderListener(iv);
        listener.setScaleType(scaleType);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.v(TAG, "onImageLoadDone()");
                // 404 or socket timeout
                if (o instanceof Exception) {
                    if (!URLUtil.isFileUrl(url)) {
                        ++mNetworkErrorCount;
                        if (MiscUtil.isConnected(mContext)) {
                            mIsNetworkStateReported = false;
                            // network is slow
                            ++mNetworkErrorCount;
                            if (mNetworkErrorCount >= NETWORK_SLOW_ERROR_COUNT && mNetworkErrorCount < NETWORK_HUNG_ERROR_COUNT) {
                                if (!mIsNetworkStateReported) {
                                    mView.onNetworkState(NetState.STATE_SLOW);
                                    mIsNetworkStateReported = true;
                                }
                            } else if (mNetworkErrorCount >= NETWORK_HUNG_ERROR_COUNT) {
                                if (!mIsNetworkStateReported) {
                                    mView.onNetworkState(NetState.STATE_HUNG);
                                    mIsNetworkStateReported = true;
                                }
                            }
                        } else {
                            if (!mIsNetworkStateReported) {
                                mView.onNetworkState(NetState.STATE_DISCONNECT);
                                mIsNetworkStateReported = true;
                            }
                        }
                    } else {
                        // remove from database for image file may be removed by user
                        Map<String, String> params = new HashMap<>();
                        params.put("mUrl", url);
                        params.put("mIsCached", Boolean.toString(Boolean.TRUE));
                        mRealmApi.deleteAsync(ImageRealm.class, (HashMap<String, String>) params);
                    }
                    PresenterBase.this.onImageLoadDone(pos, false);
                } else {
                    PresenterBase.this.onImageLoadDone(pos, true);
                    mNetworkErrorCount = mNetworkErrorCount <= 0 ? 0 ：(mNetworkErrorCount - 1);
                    if (mNetworkErrorCount == 0) {
                         mIsNetworkStateReported = false;
                    }
                }
            }
        });

        GlideLoader.load(mContext, url, listener, priority, w, h, scaleType);
        onImageLoadStart(pos);
    }

    @Override
    public void saveUserPickedPhotos(List<String> photoUris) {
        Log.v(TAG,"saveUserPickedPhotos():size=" + photoUris.size());
        if (photoUris.size() == 0) {
            return;
        }

        List<ImageRealm> photos = new ArrayList<>(photoUris.size());
        for (int i = 0; i < photoUris.size(); ++i) {
            ImageRealm ir = new ImageRealm();
            ir.setUrl(photoUris.get(i));
            ir.setIsWallpaper(true);
            ir.setIsCached(true);
            ir.setUsed(true);
            ir.setTimeStamp(System.currentTimeMillis());

            photos.add(ir);
        }

        mRealmApi.insertAsync(photos);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"onDestroy()");
        if (mImageList.isValid()) {
            mImageList.removeChangeListener(mOrderRealmChangeListener);
        }
        mRealmApi.closeRealm();

        if (mImageExecutor != null && !mImageExecutor.isTerminated()) {
            mImageExecutor.shutdown();
        }
    }

    private void setWallpaper(final String url) {
        Log.v(TAG,"setWallpaper()");

        if(TextUtils.isEmpty(url)) return;

        markAsWallpaper(url);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // for pixels, we can use URL to download hd images
                if (url.startsWith(ImageSource.PIXELS_IMAGE_START)) {
                    int res = dm.heightPixels/2 > 1024 ?  1024 : dm.heightPixels;
                    String imgUrl = buildHighResolutionPixelsUrl(url, res);
                    setWallpaperAndCache(imgUrl);
                } else {
                    setWallpaperAndCache(url);
                }
            }
        }).start();
    }

    private void setWallpaperAndCache(final String url) {
        if (mHttpClient == null) {
            mHttpClient = MiscUtil.buildOkHttpClient();
        }
        // cached?
        if (!mCacheMgr.isExist(url)) {
            okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
            builder.url(url)
                    .get();
            mHttpClient.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mView.onWallpaperSetDone(false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.v(TAG,"onResponse()");
                    if (response.isSuccessful()) {
                        byte[] data = response.body().bytes();

                        WallpaperManager wm = WallpaperManager.getInstance(mContext);
                        wm.setStream(new ByteArrayInputStream(data));

                        mView.onWallpaperSetDone(true);

                        Cache.Entry entry1 = new Cache.Entry(data, System.currentTimeMillis());
                        mCacheMgr.put(url, entry1);
                    } else {
                        mView.onWallpaperSetDone(false);
                    }
                }
            });
        } else {
            Cache.Entry entry = mCacheMgr.get(url);
            WallpaperManager wm = WallpaperManager.getInstance(mContext);
            ByteArrayInputStream bis = new ByteArrayInputStream(entry.data, 0, entry.data.length);
            try {
                wm.setStream(bis);
                mView.onWallpaperSetDone(true);
            } catch (IOException e) {
                e.printStackTrace();
                mView.onWallpaperSetDone(false);
            }
        }
    }

    void markAsFavor(String url) {
        Log.v(TAG,"markAsFavor()");
        // mark it as wall paper
        HashMap<String,String> params = new HashMap<>();
        HashMap<String, String> updated = new HashMap<>();

        params.put("mUrl", url);

        updated.put("mIsUsed", Boolean.toString(Boolean.TRUE));
        updated.put("mIsFavor", Boolean.toString(true));
        mRealmApi.updateAsync(ImageRealm.class, params, updated);
    }

    private void trimData() {
        if (mImageList.size() > mMaxImagesAllowed) {
            ImageRealm ir = mImageList.get(0);
            HashMap<String,String> params = new HashMap<>();
            params.put("mIsUsed", Boolean.toString(ir.getUsed()));
            params.put("mIsFavor", Boolean.toString(ir.getIsFavor()));
            params.put("mIsWallpaper", Boolean.toString(ir.getIsWallpaper()));
            mRealmApi.trimData(ImageRealm.class, params, mMaxImagesAllowed);
        }
    }

    private void markAsWallpaper(String url) {
        Log.v(TAG,"markAsWallpaper()");
        if (url != null && URLUtil.isNetworkUrl(url)) {
            // mark it as wall paper
            HashMap<String,String> params = new HashMap<>();
            HashMap<String, String> updated = new HashMap<>();

            params.put("mUrl", url);

            updated.put("mUrl", mCacheMgr.getFilePath(url));
            updated.put("mIsWallpaper", Boolean.toString(true));
            updated.put("mIsCached", Boolean.toString(Boolean.TRUE));
            mRealmApi.updateAsync(ImageRealm.class, params, updated);
        }
    }

    private String buildHighResolutionPixelsUrl(String url, int px) {
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
                // trim data if needed only for non-favor;non-wallpaper
                if (mImageList.size() > 0) {
                    ImageRealm ir = mImageList.get(0);
                    if (ir != null && !ir.getIsWallpaper() && !ir.getIsFavor()) {
                        trimData();
                    }
                }
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
