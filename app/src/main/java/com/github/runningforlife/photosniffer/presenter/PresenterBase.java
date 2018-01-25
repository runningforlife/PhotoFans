package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.FutureTarget;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.cache.CacheApi;
import com.github.runningforlife.photosniffer.data.cache.DiskCacheManager;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.data.remote.LeanCloudManager;
import com.github.runningforlife.photosniffer.ui.UI;
import com.github.runningforlife.photosniffer.ui.fragment.BatchAction;
import com.github.runningforlife.photosniffer.utils.BitmapUtil;
import com.github.runningforlife.photosniffer.utils.MediaStoreUtil;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;

import static com.github.runningforlife.photosniffer.loader.Loader.DEFAULT_IMG_HEIGHT;
import static com.github.runningforlife.photosniffer.loader.Loader.DEFAULT_IMG_WIDTH;
import static com.github.runningforlife.photosniffer.ui.fragment.BatchAction.BATCH_DELETE;
import static com.github.runningforlife.photosniffer.ui.fragment.BatchAction.BATCH_FAVOR;
import static com.github.runningforlife.photosniffer.ui.fragment.BatchAction.BATCH_SAVE_AS_WALLPAPER;

/**
 * base class for presenter
 */

abstract class PresenterBase implements Presenter {
    private static final String TAG = "PresenterBase";

    private RequestManager mGlideManager;
    private CacheApi mCacheMgr;
    private ExecutorService mExecutors;
    private CountDownLatch mSavingLatch;
    private H mMainHandler;
    private boolean mIsFirstPager;
    private NetworkStateReceiver mNetStateReceiver;
    private TelephonyManager mTm;

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

        // default value
        mMaxImagesAllowed = Integer.MAX_VALUE;

        mGlideManager = requestManager;

        mMainHandler = new H(Looper.myLooper());

        mIsFirstPager = false;

        mTm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    }

    private final class NetworkStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if (!MiscUtil.isConnected(context) && !isInCalling()) {
                    mView.onNetworkState(NetState.STATE_DISCONNECT);
                }else if(MiscUtil.isWifiConnected(context)){
                    uploadLogAndAdviceToCloud();
                }
            }
        }
    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        mNetStateReceiver = new NetworkStateReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mNetStateReceiver, filter);
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String imgUrl = url;
                    if (!TextUtils.isEmpty(highRes)) {
                        imgUrl = highRes;
                    }
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
                mGlideManager.load(url)
                        //.thumbnail(0.5f)
                        .dontTransform()
                        .override(w, h)
                        .error(R.drawable.ic_broken_image_white_24dp)
                        .centerCrop()
                        .into(iv);
            } else {
                String imgUrl = url;
                if (!TextUtils.isEmpty(ir.getHighResUrl())) {
                    imgUrl = ir.getHighResUrl();
                }
                mGlideManager.load(imgUrl)
                        //.thumbnail(0.5f)
                        .dontTransform()
                        .dontAnimate()
                        .override(w, h)
                        .error(R.drawable.ic_broken_image_white_24dp)
                        .fitCenter()
                        .into(iv);

                if (!mIsFirstPager) {
                    onImageLoadStart(pos);
                    mMainHandler.sendEmptyMessageDelayed(EVENT_IMAGE_LOAD_DONE, 800);
                    mIsFirstPager = true;
                }
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
            ir.setHighResUrl(photoUris.get(i));
            ir.setIsWallpaper(true);
            ir.setIsCached(true);
            ir.setUsed(true);
            ir.setTimeStamp(System.currentTimeMillis());

            photos.add(ir);
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

        if (mNetStateReceiver != null) {
            mContext.unregisterReceiver(mNetStateReceiver);
        }
    }

    private void setWallpaper(final String url) {
        Log.v(TAG,"setWallpaper()");

        if(TextUtils.isEmpty(url)) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                setWallpaperAndCache(url);
            }
        }).start();
    }


    private void setWallpaperAndCache(final String imgUrl) {
        FutureTarget<Bitmap> bitmapTarget = mGlideManager.load(imgUrl)
                     .asBitmap()
                     .centerCrop()
                     .into(DEFAULT_IMG_WIDTH, DEFAULT_IMG_HEIGHT);
        try {
            Bitmap bitmap = bitmapTarget.get(5000, TimeUnit.MILLISECONDS);
            WallpaperManager wm = WallpaperManager.getInstance(mContext);
            wm.setBitmap(bitmap);
            mView.onWallpaperSetDone(true);
            if (imgUrl.startsWith("http")) {
                Message message = mMainHandler.obtainMessage(EVENT_WALLPAPER_SET_DONE);
                message.obj = imgUrl;
                message.sendToTarget();
                // cache it
                mCacheMgr.put(imgUrl, bitmap);
            }
            return;
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (TimeoutException e) {
        } catch (IOException e) {
        }

        mView.onWallpaperSetDone(false);
    }

    private void markAsFavor(List<String> images) {
        for (String url : images) {
            markAsFavor(url);
        }
    }

    private boolean isInCalling() {
        return mTm.getCallState() == TelephonyManager.CALL_STATE_RINGING;
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
        HashMap<String,String> params = new HashMap<>();
        params.put("mIsUsed", Boolean.toString(true));
        params.put("mIsFavor", Boolean.toString(false));
        params.put("mIsWallpaper", Boolean.toString(false));
        mRealmApi.trimDataSync(ImageRealm.class, params, mMaxImagesAllowed);
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
        if (mExecutors == null) {
            mExecutors = Executors.newFixedThreadPool(2);
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
        FutureTarget<Bitmap> target = mGlideManager.load(imgUrl)
                .asBitmap()
                .centerCrop()
                .into(DEFAULT_IMG_WIDTH, DEFAULT_IMG_HEIGHT);
        try {
            Bitmap bitmap = target.get(3000, TimeUnit.MILLISECONDS);

            FileOutputStream fos = new FileOutputStream(mCacheMgr.getFilePath(imgUrl));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.flush();
            fos.close();
            // ok, we will mark it as wallpaper
            if (imgUrl.startsWith("http")) {
                Message message = mMainHandler.obtainMessage(EVENT_WALLPAPER_SET_DONE);
                message.obj = imgUrl;
                message.sendToTarget();
            }
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (TimeoutException e) {
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
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

    private void uploadLogAndAdviceToCloud() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String logPath = MiscUtil.getLogDir();
                File file = new File(logPath);
                if (file.exists()) {
                    File[] logs = file.listFiles();
                    for (File log : logs) {
                        if (log.isFile() && log.getName().endsWith("txt") && log.length() > 0) {
                            MiscUtil.saveLogToCloud(log);
                        }
                    }
                }
                // check whether there is advices
                String prefAdvice = mContext.getString(R.string.pref_report_issue_and_advice);
                String advice = SharedPrefUtil.getString(prefAdvice,"");
                if (!TextUtils.isEmpty(advice)) {
                    String subStr[] = advice.split(";");
                    if (subStr.length == 2) {
                        LeanCloudManager.getInstance().saveAdvice(subStr[0], subStr[1], new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                Log.d(TAG,"advice saved done");
                            }
                        });
                    }
                }
            }
        }).start();
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
            //images.sort("mTimeStamp", Sort.DESCENDING);
            // trim data if needed only for non-favor;non-wallpaper
            if (images.size() > 0) {
                ImageRealm ir = images.get(0);
                if (ir != null && !ir.getIsWallpaper() && !ir.getIsFavor()) {
                    trimData();
                }
            }
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
