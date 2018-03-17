package com.github.runningforlife.photosniffer.presenter;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.cache.CacheApi;
import com.github.runningforlife.photosniffer.data.cache.DiskCacheManager;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.ui.UI;
import com.github.runningforlife.photosniffer.ui.fragment.BatchAction;
import com.github.runningforlife.photosniffer.utils.BitmapUtil;
import com.github.runningforlife.photosniffer.utils.MediaStoreUtil;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;

import static com.github.runningforlife.photosniffer.glide.ImageSizer.DEFAULT_IMG_HEIGHT;
import static com.github.runningforlife.photosniffer.glide.ImageSizer.DEFAULT_IMG_WIDTH;
import static com.github.runningforlife.photosniffer.ui.fragment.BatchAction.BATCH_DELETE;
import static com.github.runningforlife.photosniffer.ui.fragment.BatchAction.BATCH_FAVOR;
import static com.github.runningforlife.photosniffer.ui.fragment.BatchAction.BATCH_SAVE_AS_WALLPAPER;

/**
 * base class for presenter
 */

abstract class PresenterBase implements Presenter {
    private static final String TAG = "PresenterBase";

    private RequestManager mGlideManager;
    private RequestOptions mRequestOptionsThumb;
    private RequestOptions mRequestOptionsFull;
    private CacheApi mCacheMgr;
    private ExecutorService mExecutors;
    private CountDownLatch mSavingLatch;
    private H mMainHandler;
    private boolean mIsFirstPager;
    // wallpaper setting action
    private WallpaperSettingReceiver mWallpaperChangeReceiver;
    private HashMap<String, Boolean> mSettingAsWallpaper;

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

        mExecutors = Executors.newFixedThreadPool(2);

        mRequestOptionsThumb = new RequestOptions();
        mRequestOptionsThumb.dontAnimate()
                       .error(R.drawable.ic_broken_image_white_24dp)
                       .centerCrop()
                       .override(800, 800);

        mRequestOptionsFull = new RequestOptions();
        mRequestOptionsFull.error(R.drawable.ic_broken_image_white_24dp)
                .fitCenter()
                .override(DEFAULT_IMG_WIDTH, DEFAULT_IMG_HEIGHT);
        // default value
        mMaxImagesAllowed = Integer.MAX_VALUE;

        mGlideManager = requestManager;

        mMainHandler = new H(Looper.myLooper());

        mIsFirstPager = false;

        mWallpaperChangeReceiver = new WallpaperSettingReceiver();
        IntentFilter filter = new IntentFilter(WallpaperSettingReceiver.ACTION_WALLPAPER_SETTING_DONE);
        mContext.registerReceiver(mWallpaperChangeReceiver, filter);

        mSettingAsWallpaper = new HashMap<>();
    }

    @Override
    public void onImageLoadStart(int pos) {
       // for detail presenter to use
    }

    @Override
    public void onImageLoadDone(boolean isSuccess) {
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
            final ImageRealm ir = mImageList.get(pos);
            String url = ir.getUrl();
            if (!TextUtils.isEmpty(ir.getHighResUrl())) {
                url = ir.getHighResUrl();
            }
            setWallpaper(url);
        }
    }

    @Override
    public void saveImageAtPos(int pos) {
        if (pos >= 0 && pos < mImageList.size()) {
            final ImageRealm ir = mImageList.get(pos);
            final String url = ir.getUrl();
            final String highRes = ir.getHighResUrl();
            mExecutors.execute(new Runnable() {
                @Override
                public void run() {
                    String imgUrl = url;
                    if (!TextUtils.isEmpty(highRes)) {
                        imgUrl = highRes;
                    }
                    saveBitmap(imgUrl);
                }
            });
        }
    }

    @Override
    public void removeItemAtPos(int pos) {
        Log.d(TAG,"removeItemAtPos()");
        if(pos >= 0 && pos < mImageList.size()) {
            final ImageRealm ir = mImageList.get(pos);
            String url = ir.getUrl();
            if (!url.startsWith("http") && mCacheMgr.isExist(url)) {
                mCacheMgr.remove(url);
            }
            mRealmApi.deleteSync(ir);
        }
    }

    @Override
    public void loadImageIntoView(final int pos, final ImageView iv, Priority priority, int w, int h, final ImageView.ScaleType scaleType) {
        Log.v(TAG,"loadImageIntoView()");
        final ImageRealm ir = getItemAtPos(pos);
        if (ir != null && ir.isValid()) {
            final String url = ir.getUrl();
            if (scaleType == ImageView.ScaleType.CENTER_CROP) {
                mGlideManager.setDefaultRequestOptions(mRequestOptionsThumb)
                        .load(url)
                        .thumbnail(0.5f)
                        .into(iv);
            } else {
                String imgUrl = url;
                if (!TextUtils.isEmpty(ir.getHighResUrl())) {
                    imgUrl = ir.getHighResUrl();
                }
                mGlideManager.setDefaultRequestOptions(mRequestOptionsFull)
                        .load(imgUrl)
                        .thumbnail(0.5f)
                        .into(iv);

                if (!mIsFirstPager) {
                    onImageLoadStart(pos);
                    mMainHandler.sendEmptyMessageDelayed(EVENT_IMAGE_LOAD_DONE, 800);
                    mIsFirstPager = true;
                }
            }
        } else {
            mGlideManager.clear(iv);
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
            String photoUri = photoUris.get(i);
            if (!photoUri.startsWith(MiscUtil.getWallpaperCacheDir())) {
                ImageRealm ir = new ImageRealm();
                ir.setUrl(photoUris.get(i));
                ir.setHighResUrl(photoUris.get(i));
                ir.setIsWallpaper(true);
                ir.setIsCached(true);
                ir.setUsed(true);
                ir.setTimeStamp(System.currentTimeMillis());

                photos.add(ir);
            }
        }

        mRealmApi.insertAsync(photos);
    }

    @Override
    public void batchEdit(List<String> images, @BatchAction String action) {
        Log.v(TAG,"batchEdit():size=" + images.size());
        if (images.size() == 0) return;

        switch (action) {
            case BATCH_FAVOR:
                markAsFavor(images);
                break;
            case BATCH_DELETE:
                batchRemove(images);
                break;
            case BATCH_SAVE_AS_WALLPAPER:
                saveAsWallpapers(images);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"onDestroy()");
        if (mImageList.isValid()) {
            mImageList.removeChangeListener(mOrderRealmChangeListener);
        }
        mRealmApi.closeRealm();

        if (mWallpaperChangeReceiver != null) {
            mContext.unregisterReceiver(mWallpaperChangeReceiver);
        }

        if (mSettingAsWallpaper != null) {
            mSettingAsWallpaper.clear();
        }
    }

    private void setWallpaper(final String url) {
        Log.v(TAG,"setWallpaper()");

        if(TextUtils.isEmpty(url)) return;

        mExecutors.execute(new Runnable() {
            @Override
            public void run() {
                startWallpaperChooser(url);
            }
        });
    }


    // NOTICE: do not use it on UI thread
    private void startWallpaperChooser(final String imgUrl) {
        FutureTarget<Bitmap> bitmapTarget = Glide.with(mContext)
                .asBitmap()
                .load(imgUrl)
                .apply(new RequestOptions().centerCrop())
                .submit(DEFAULT_IMG_WIDTH,DEFAULT_IMG_HEIGHT);

        try {
            Bitmap bitmap = bitmapTarget.get(5, TimeUnit.SECONDS);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US);
            String wallpaperName = "wallpaper_" + df.format(new Date());
            Uri imgUri1 = Uri.parse(MediaStore.Images.Media
                    .insertImage(mContext.getContentResolver(), bitmap , wallpaperName, wallpaperName));

            Intent wallpaperSetting = new Intent(Intent.ACTION_ATTACH_DATA);
            wallpaperSetting.setData(imgUri1);
            wallpaperSetting.putExtra("mimeType", "image/*");
            wallpaperSetting.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


            String title = mContext.getString(R.string.set_wallpaper);
            if (Build.VERSION.SDK_INT >= 22) {
                Intent setWallpaperDone = new Intent(WallpaperSettingReceiver.ACTION_WALLPAPER_SETTING_DONE);
                setWallpaperDone.putExtra(EXTRA_WALLPAPER_URL, imgUrl);
                PendingIntent pi = PendingIntent.getBroadcast(mContext, 0x30, setWallpaperDone, PendingIntent.FLAG_CANCEL_CURRENT);
                IntentSender sender = pi.getIntentSender();
                mContext.startActivity(Intent.createChooser(wallpaperSetting, title, sender));
            } else {
                mContext.startActivity(Intent.createChooser(wallpaperSetting, title));
            }
            mSettingAsWallpaper.put(imgUrl, false);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            mView.onWallpaperSetDone(false);
        }

        // clear target
        Glide.with(mContext).clear(bitmapTarget);
    }

    private void markAsFavor(List<String> images) {
        for (String url : images) {
            markAsFavor(url);
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

    private void batchRemove(List<String> urls) {
        String imgUrl = urls.get(0);
        if (!imgUrl.startsWith("http")) {
            mView.notifyJobState(true, null);
            // 2s timeout, we may want to be more specific
            mMainHandler.sendEmptyMessageDelayed(EVENT_BATCH_REMOVE_TIMEOUT, 2000);
        }
        mRealmApi.deleteSync(urls);
        for (String url : urls) {
            if (url.startsWith("http")) {
                break;
            }
            if (!mCacheMgr.isExist(url)) {
                mCacheMgr.remove(url);
            }
        }
    }

    private void saveAsWallpapers(List<String> urls) {
        if (mSavingLatch == null) {
            mSavingLatch = new CountDownLatch(urls.size());
        }

        for (final String url : urls) {
            if (url.startsWith("http")) {
                mExecutors.submit(new Runnable() {
                    @Override
                    public void run() {
                        saveImageAsWallpaper(url);
                        mSavingLatch.countDown();
                    }
                });
            } else {
                mSavingLatch.countDown();
            }
        }

        mView.notifyJobState(true, null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSavingLatch.await(5,TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mMainHandler.sendEmptyMessage(EVENT_BATCH_SAVE_TIMEOUT);
            }
        }).start();
    }

    private void markAsWallpaper(String url) {
        Log.v(TAG,"markAsWallpaper()");
        // mark it as wall paper
        HashMap<String,String> params = new HashMap<>();
        params.put("mUrl", url);

        mRealmApi.deleteAsync(ImageRealm.class, params);

        ImageRealm ir = new ImageRealm();
        ir.setUrl(mCacheMgr.getFilePath(url));
        ir.setUsed(true);
        ir.setIsFavor(false);
        ir.setIsWallpaper(true);
        ir.setIsCached(true);
        mRealmApi.insertAsync(ir);
    }

    private void saveImageAsWallpaper(String imgUrl) {
        FutureTarget<Bitmap> target = Glide.with(mContext)
                .asBitmap()
                .apply(new RequestOptions().centerCrop())
                .load(imgUrl)
                .submit(DEFAULT_IMG_WIDTH, DEFAULT_IMG_HEIGHT);

        try {
            Bitmap bitmap = target.get(5, TimeUnit.SECONDS);
            if (imgUrl.startsWith("http") && saveBitmap(mCacheMgr.getFilePath(imgUrl), bitmap)) {
                // ok, we will mark it as wallpaper
                Message message = mMainHandler.obtainMessage(EVENT_WALLPAPER_SET_DONE);
                message.obj = imgUrl;
                message.sendToTarget();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
        }

        // clear target
        Glide.with(mContext).clear(target);
    }

    private boolean saveBitmap(String path, Bitmap bitmap) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.flush();
            fos.close();
            return true;
        } catch (IOException  e) {
        }

        return false;
    }

    private void saveBitmap(String url) {
        FutureTarget<Bitmap> target = Glide.with(mContext)
                .asBitmap()
                .apply(new RequestOptions().centerCrop())
                .load(url)
                .submit(DEFAULT_IMG_WIDTH, DEFAULT_IMG_HEIGHT);
        try {
            Bitmap bitmap = target.get(5000, TimeUnit.MILLISECONDS);
            String imageDir = MiscUtil.getPhotoDir();
            String filePath = BitmapUtil.saveToFile(bitmap, imageDir);
            MediaStoreUtil.addImageToGallery(mContext,new File(filePath));

            mView.onImageSaveDone(filePath);
            return;
        } catch (FileNotFoundException | InterruptedException | ExecutionException | TimeoutException e) {
        }

        mView.onImageSaveDone(null);

        Glide.with(mContext).clear(target);
    }

    private final class H extends Handler {

        H(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case EVENT_BATCH_REMOVE_TIMEOUT:
                case EVENT_BATCH_SAVE_TIMEOUT:
                    mView.notifyJobState(false, null);
                    break;
                case EVENT_IMAGE_LOAD_DONE:
                    onImageLoadDone(true);
                    break;
                case EVENT_WALLPAPER_SET_DONE:
                    markAsWallpaper((String) message.obj);
                    break;
            }
        }
    }


    private final class OrderReamChangeListener implements OrderedRealmCollectionChangeListener<RealmResults<ImageRealm>> {

        @Override
        public void onChange(RealmResults<ImageRealm> images, OrderedCollectionChangeSet changeSet) {
            Log.v(TAG,"onChange(): size = " + images.size());
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


    // broadcast to listen wallpaper setting action
    private class WallpaperSettingReceiver extends BroadcastReceiver {
        public static final String ACTION_WALLPAPER_SETTING_DONE = "com.github.photosniffer.wallpaper_setting_done";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG,"onReceive(): action = " + intent.getAction());
            if (ACTION_WALLPAPER_SETTING_DONE.equals(intent.getAction())) {
                String imgUrl = intent.getStringExtra(EXTRA_WALLPAPER_URL);
                boolean isSetAsWallpaper = mSettingAsWallpaper.get(imgUrl);
                if (imgUrl.startsWith("http") && !isSetAsWallpaper) {
                    mSettingAsWallpaper.put(imgUrl, true);
                    saveImageAsWallpaper(imgUrl);
                }
            }
        }
    }
}
