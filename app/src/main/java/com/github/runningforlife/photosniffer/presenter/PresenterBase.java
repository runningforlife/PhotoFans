package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.FutureTarget;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;
import com.github.runningforlife.photosniffer.data.cache.CacheApi;
import com.github.runningforlife.photosniffer.data.cache.DiskCacheManager;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.ui.UI;
import com.github.runningforlife.photosniffer.utils.BitmapUtil;
import com.github.runningforlife.photosniffer.utils.MediaStoreUtil;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;

import static com.github.runningforlife.photosniffer.loader.Loader.DEFAULT_IMG_HEIGHT;
import static com.github.runningforlife.photosniffer.loader.Loader.DEFAULT_IMG_WIDTH;

/**
 * base class for presenter
 */

abstract class PresenterBase implements Presenter {
    private static final String TAG = "PresenterBase";

    private int mNetworkErrorCount;
    private boolean mIsNetworkStateReported;
    private RequestManager mGlideManager;
    private CacheApi mCacheMgr;

    int mMaxImagesAllowed;

    Context mContext;
    UI mView;

    RealmResults<ImageRealm> mImageList;
    RealmApi mRealmApi;

    OrderReamChangeListener mOrderRealmChangeListener;

    PresenterBase(RequestManager requestManager, Context context, UI view) {
        mContext = context;
        mView = view;
        mOrderRealmChangeListener = new OrderReamChangeListener();
        mRealmApi = RealmApiImpl.getInstance();
        mCacheMgr = DiskCacheManager.getInstance();

        mNetworkErrorCount = 0;

        // default value
        mMaxImagesAllowed = Integer.MAX_VALUE;

        mIsNetworkStateReported = false;

        mGlideManager = requestManager;
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
        if (pos >= 0 && pos < mImageList.size()) {
            return mImageList.get(pos);
        }

        return null;
    }

    @Override
    public void setWallpaperAtPos(int pos) {
        if(pos >= 0 && pos < mImageList.size()) {
            String url = mImageList.get(pos).getUrl();
            setWallpaper(url);
        }
    }

    @Override
    public void saveImageAtPos(int pos) {
        if (pos >= 0 && pos < mImageList.size()) {
            final String imgUrl = mImageList.get(pos).getUrl();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    saveBitmap(imgUrl);
                }
            }).start();
        }
    }

    @Override
    public void removeItemAtPos(int pos) {
        Log.d(TAG,"removeItemAtPos()");
        if(pos >= 0 && pos < mImageList.size()) {
            final ImageRealm ir = mImageList.get(pos);
            String url = ir.getUrl();
            if (mCacheMgr.isExist(url)) {
                mCacheMgr.remove(url);
            }
            mRealmApi.deleteSync(ir);
        }
    }

    @Override
    public void loadImageIntoView(final int pos, final ImageView iv, Priority priority, int w, int h, final ImageView.ScaleType scaleType) {
        Log.v(TAG,"loadImageIntoView()");
        final ImageRealm ir = getItemAtPos(pos);
        if (ir != null) {
            final String url = ir.getUrl();
            if (scaleType == ImageView.ScaleType.CENTER_CROP) {
                mGlideManager.load(url)
                        //.thumbnail(0.5f)
                        .dontTransform()
                        .override(w, h)
                        .error(R.drawable.ic_broken_image_white_24dp)
                        .centerCrop()
                        .into(iv);
            } else {
                mGlideManager.load(url)
                        .thumbnail(0.5f)
                        .dontTransform()
                        .dontAnimate()
                        .override(w, h)
                        .error(R.drawable.ic_broken_image_white_24dp)
                        .fitCenter()
                        .into(iv);
            }
        }
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
    }

    private void setWallpaper(final String url) {
        Log.v(TAG,"setWallpaper()");

        if(TextUtils.isEmpty(url)) return;

        if (url.startsWith("http")) {

            markAsWallpaper(url);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // for pixels, we can use URL to download hd images
                    if (url.startsWith(ImageSource.PIXELS_IMAGE_START)) {
                        int res = dm.heightPixels / 2 > 1024 ? 1024 : dm.heightPixels;
                        String imgUrl = buildHighResolutionPixelsUrl(url, res);
                        setWallpaperAndCache(imgUrl);
                    } else {
                        setWallpaperAndCache(url);
                    }
                }
            }).start();
        }
    }

    private void setWallpaperAndCache(final String url) {
        FutureTarget<Bitmap> bitmapTarget = mGlideManager.load(url)
                     .asBitmap()
                     .centerCrop()
                     .into(DEFAULT_IMG_WIDTH, DEFAULT_IMG_HEIGHT);
        try {
            Bitmap bitmap = bitmapTarget.get(5000, TimeUnit.MILLISECONDS);
            WallpaperManager wm = WallpaperManager.getInstance(mContext);
            wm.setBitmap(bitmap);
            mView.onWallpaperSetDone(true);
            // cache it
            if (!mCacheMgr.isExist(url)) {
                mCacheMgr.put(url, bitmap);
            }
            return;
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (TimeoutException e) {
        } catch (IOException e) {
        }

        mView.onWallpaperSetDone(false);
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
        // mark it as wall paper
        HashMap<String,String> params = new HashMap<>();
        HashMap<String, String> updated = new HashMap<>();

        params.put("mUrl", url);

        updated.put("mUrl", mCacheMgr.getFilePath(url));
        updated.put("mIsWallpaper", Boolean.toString(true));
        updated.put("mIsFavor", Boolean.toString(Boolean.FALSE));
        updated.put("mIsCached", Boolean.toString(Boolean.TRUE));
        mRealmApi.updateAsync(ImageRealm.class, params, updated);
    }

    private String buildHighResolutionPixelsUrl(String url, int px) {
        int hIdx = url.indexOf("?");

        return url.substring(0, hIdx)
                + "?"
                + "h=" + px
                + "&auto=compress"
                + "&cs=tinysrgb";
    }


    private void saveBitmap(String url) {
        FutureTarget<Bitmap> target = mGlideManager.load(url)
                .asBitmap()
                .centerCrop()
                .into(DEFAULT_IMG_WIDTH, DEFAULT_IMG_HEIGHT);
        try {
            Bitmap bitmap = target.get(5000, TimeUnit.MILLISECONDS);
            String imageDir = MiscUtil.getPhotoDir();
            String filePath = BitmapUtil.saveToFile(bitmap, imageDir);
            MediaStoreUtil.addImageToGallery(mContext,new File(filePath));

            mView.onImageSaveDone(filePath);
            return;
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (TimeoutException e) {
        } catch (FileNotFoundException e) {
        }

        mView.onImageSaveDone(null);
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
