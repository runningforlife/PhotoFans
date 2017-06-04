package com.github.runningforlife.photofans.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.github.runningforlife.photofans.app.AppGlobals;
import com.github.runningforlife.photofans.loader.GlideLoader;
import com.github.runningforlife.photofans.loader.GlideLoaderListener;
import com.github.runningforlife.photofans.model.RealmManager;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import com.github.runningforlife.photofans.model.ImageRealm;
import com.github.runningforlife.photofans.ui.ImageDetailView;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jason on 4/6/17.
 */

public class ImageDetailPresenterImpl implements ImageDetailPresenter {
    private static final String TAG = "ImageDetailPresenter";

    private Context mContext;
    private RealmResults<ImageRealm> mImgList;
    private ImageDetailView mView;
    private RealmManager mRealmMgr;
    private ExecutorService mExecutor;

    public ImageDetailPresenterImpl(Context context, ImageDetailView view){
        mContext = context;
        mView = view;
        mRealmMgr = RealmManager.getInstance();
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mImgList.get(pos);
    }

    @Override
    public int getItemCount() {
        if(mImgList == null) return 0;

        return mImgList.size();
    }

    @Override
    public void removeItemAtPos(int pos) {
        Log.d(TAG,"removeItemAtPos()");
        mRealmMgr.delete(mImgList.get(pos));
    }

    @Override
    public void saveImageAtPos(final int pos) {
        Log.v(TAG,"saveImageAtPos(): pos " + pos);

        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG,"onImageLoadDone()");
                ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap)o), mImgList.get(pos).getName());
                r.addCallback(ImageDetailPresenterImpl.this);
                mExecutor.submit(r);
            }
        });
        GlideLoader.downloadOnly(mContext, mImgList.get(pos).getUrl(), listener,
                DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public void favorImageAtPos(int pos) {
        Log.d(TAG,"favorImageAtPos(): pos = " + pos);
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();

        ImageRealm favor = mImgList.get(pos);
        favor.setIsFavor(true);

        r.commitTransaction();
    }

    @Override
    public void init() {
        //mImgList = new ArrayList<>();
        mRealmMgr.onStart();
    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        mRealmMgr.addListener(this);
    }

    @Override
    public void onDestroy() {
        mRealmMgr.removeListener(this);
        mRealmMgr.onDestroy();
    }

    @Override
    public void onRealmDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onRealmDataChange(): data size = " + data.size());
        mImgList = data;
        // keep sorted
        sort();
        mView.onDataSetChanged();
    }

    private void sort(){
        mImgList.sort("mTimeStamp", Sort.DESCENDING);
    }

    @Override
    public void onImageSaveDone(String path) {
        Log.d(TAG,"onImageSaveDone()");
        mView.onImageSaveDone(path);
    }
}
