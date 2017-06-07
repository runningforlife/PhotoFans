package com.github.runningforlife.photosniffer.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.ui.FavorView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * presenter to get favor image list
 */

public class FavorImagePresenterImpl extends FavorImagePresenter{
    private static final String TAG = "FavorImagePresenter";
    private FavorView mView;
    private Context mContext;
    private RealmManager mRealmMgr;
    private RealmResults<ImageRealm> mFavorList;
    private ExecutorService mExecutor;

    public FavorImagePresenterImpl(Context context, FavorView view){
        mContext = context;
        mView = view;
        mRealmMgr = RealmManager.getInstance();
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public int getItemCount() {
        if(mFavorList == null) return 0;

        return mFavorList.size();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        if(mFavorList == null) return null;

        return mFavorList.get(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        if(pos >= 0 && pos <= mFavorList.size()) {
            mRealmMgr.delete(mFavorList.get(pos));
        }
    }

    @Override
    public void saveImageAtPos(final int pos) {
        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG,"onImageLoadDone()");
                ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap)o), mFavorList.get(pos).getName());
                r.addCallback(FavorImagePresenterImpl.this);
                mExecutor.submit(r);
            }
        });
        GlideLoader.downloadOnly(mContext, mFavorList.get(pos).getUrl(), listener,
                DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public void cancelFavorAtPos(int pos) {
        Log.v(TAG,"cancelFavorAtPos()");
        Realm r =  Realm.getDefaultInstance();
        r.beginTransaction();

        ImageRealm img = mFavorList.get(pos);
        img.setIsFavor(false);

        r.commitTransaction();
    }

    @Override
    public void init() {
        // start loading data
        mRealmMgr.onStart();
    }

    @Override
    public void onUsedRealmDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onUsedRealmDataChange(): data size = " + data.size());
    }

    @Override
    public void onUnusedRealmDataChange(RealmResults<ImageRealm> data) {

    }

    @Override
    public void onFavorRealmDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onFavorRealmDataChange()");
        mFavorList = data;

        if(mView != null) {
            mView.onDataSetChanged();
        }
    }

    @Override
    public void onImageSaveDone(String path) {
        mView.onImageSaveDone(path);
    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        mRealmMgr.addListener(this);
    }

    @Override
    public void onDestroy() {
        mRealmMgr.onDestroy();
    }
}
