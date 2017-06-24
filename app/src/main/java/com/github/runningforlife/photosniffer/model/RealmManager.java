package com.github.runningforlife.photosniffer.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.app.AppGlobals;
import com.github.runningforlife.photosniffer.presenter.LifeCycle;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * an helper class to CRUD realm database
 */

public class RealmManager implements LifeCycle{
    private static final String TAG = "RealmManager";

    private static final RealmManager sInstance = new RealmManager();
    private static AtomicInteger sRealmRefCount = new AtomicInteger(0);
    private Realm realm;
    // all the data we have
    private RealmResults<ImageRealm> mAllUsed;
    private RealmResults<ImageRealm> mAllUnUsed;
    private RealmResults<ImageRealm> mAllFavor;
    private RealmResults<ImageRealm> mAllWallpaper;
    // callback to listen realm changes: update or query complete
    private Set<RealmDataChangeListener> mListeners;
    private UsedRealmDataChangeListener mUsedDataChangeListener;
    private UnusedRealmDataChangeListener mUnusedDataChangeListener;
    private FavorRealmDataChangeListener mFavorDataChangeListener;
    private WallpaperDataChangeListener mWallpaperDataChangeListener;
    // async task
    private RealmAsyncTask mAsyncTask;

    public interface RealmDataChangeListener{
        void onUsedDataChange(RealmResults<ImageRealm> data);
        void onUnusedDataChange(RealmResults<ImageRealm> data);
        void onFavorDataChange(RealmResults<ImageRealm> data);
        void onWallpaperDataChange(RealmResults<ImageRealm> data);
    }

    public static RealmManager getInstance() {
        return sInstance;
    }

    private RealmManager() {
        realm = Realm.getDefaultInstance();
        mListeners = new HashSet<>();
        mUsedDataChangeListener = new UsedRealmDataChangeListener();
        mUnusedDataChangeListener = new UnusedRealmDataChangeListener();
        mFavorDataChangeListener = new FavorRealmDataChangeListener();
        mWallpaperDataChangeListener = new WallpaperDataChangeListener();
    }

    // this should be consistent with UI lifecycle: onCreate() or onStart()
    @Override
    public void onStart(){
        query();
        Log.d(TAG,"onStart(): ref count = " + sRealmRefCount.incrementAndGet());
    }

    // this should be consistent with UI lifecycle: onDestroy()
    @Override
    public void onDestroy(){
        Log.d(TAG,"onDestroy(): ref count = " + sRealmRefCount.get());
        if(sRealmRefCount.decrementAndGet() == 0) {
            //trimData();
            if (mAllUsed != null) {
                mAllUsed.removeAllChangeListeners();
                mAllUsed = null;
            }
            if (mAllUnUsed != null) {
                mAllUnUsed.removeAllChangeListeners();
                mAllUnUsed = null;
            }
            if(mAllFavor != null){
                mAllFavor.removeAllChangeListeners();
                mAllFavor = null;
            }
            if(mAllWallpaper != null){
                mAllWallpaper.removeAllChangeListeners();
                mAllWallpaper = null;
            }
            if (realm != null) {
                realm.close();
                realm = null;
            }
        }
        mListeners.clear();
        // cancel transaction
        if(mAsyncTask != null && !mAsyncTask.isCancelled()){
            mAsyncTask.cancel();
        }
    }

    //Note: Realm objects can only be accessed on the thread they were created
    public void savePageAsync(final VisitedPageInfo info) {
        Realm r = Realm.getDefaultInstance();
        try {
            r.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<VisitedPageInfo> all = realm.where(VisitedPageInfo.class)
                            .findAll();
                    if(!all.contains(info)) {
                        realm.copyToRealmOrUpdate(info);
                    }
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Log.v(TAG,"savePageAsync(): success");
                    //cannot access from this thread
                    //trimData();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    Log.v(TAG, "onError()" + error);
                    //write(info);
                }
            });
        }finally {
            r.close();
        }
    }

    public void saveImageRealmAsync(final List<ImageRealm> data){
        Log.v(TAG,"saveImageRealmAsync()");
        if(data == null || data.size() <= 0) return;
        Realm r = Realm.getDefaultInstance();
        try {
            r.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Log.v(TAG, "execute(): saved data size = " + data.size());
                    RealmResults<ImageRealm> all = realm.where(ImageRealm.class)
                            .findAll();
                    for(ImageRealm ir : data) {
                        if(!all.contains(ir)) {
                            realm.copyToRealmOrUpdate(data);
                        }
                    }
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Log.v(TAG,"savePageAsync(): success");
                }
            });
        }finally {
            r.close();
        }
    }

    public void savePageAsync(final List<VisitedPageInfo> data) {
        if(data == null || data.size() <= 0) return;
        Realm r = Realm.getDefaultInstance();
        try {
            r.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Log.v(TAG, "execute(): saved data size = " + data.size());
                    RealmResults<VisitedPageInfo> all = realm.where(VisitedPageInfo.class)
                            .findAll();
                    for(VisitedPageInfo page : data) {
                        if(!all.contains(page)) {
                            realm.copyToRealmOrUpdate(data);
                        }
                    }
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Log.v(TAG,"savePageAsync(): success");
                }
            });
        }finally {
            r.close();
        }
    }

    public void queryAllAsync(){
        query();
    }

    public RealmResults<VisitedPageInfo> getAllVisitedPages(Realm r){
        RealmResults<VisitedPageInfo> visited = r.where(VisitedPageInfo.class)
                .equalTo("mIsVisited", true)
                .isNotNull("mUrl")
                .findAll();

        return visited;
    }

    public RealmResults<VisitedPageInfo> getAllUnvisitedPages(Realm r){
        RealmResults<VisitedPageInfo> unVisited = r.where(VisitedPageInfo.class)
                .equalTo("mIsVisited",false)
                .isNotNull("mUrl")
                .findAll();

        return unVisited;
    }

    public void delete(final RealmObject object){
        if(object == null) return;
        Realm r = Realm.getDefaultInstance();

        r.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                object.deleteFromRealm();
            }
        });
    }

    // keep latest images
    public void trimData(final int maxImgs){
        mAllUsed.removeChangeListener(mUsedDataChangeListener);
        new Handler(Looper.myLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // add listener
                        mAllUsed.addChangeListener(mUsedDataChangeListener);
                    }
                },10);
        // delete item
        mAsyncTask = realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<ImageRealm> used = realm.where(ImageRealm.class)
                        .equalTo("mIsUsed", true)
                        .findAllSorted("mTimeStamp",Sort.ASCENDING);
                int total = used.size();
                for(int i = 0; i <  total - maxImgs; ++i){
                    // realm object cannot be accessed by multiple threads
                    ImageRealm item = used.get(i);
                    item.deleteFromRealm();
                }
            }
        });
    }

    // mark unused images to used
    public void markUnusedRealm(final int number){
        if(mAllUnUsed == null || !mAllUnUsed.isValid()){
            mAllUnUsed = realm.where(ImageRealm.class)
                    .equalTo("mIsUsed", false)
                    .findAllAsync()
                    .sort("mTimeStamp", Sort.DESCENDING);
            mAllUnUsed.addChangeListener(mUnusedDataChangeListener);
        }else{
            mAsyncTask = realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<ImageRealm> unused = realm.where(ImageRealm.class)
                            .equalTo("mIsUsed", false)
                            .findAll();
                    for (int i = 0; i < number && i < unused.size(); ++i) {
                        ImageRealm item = unused.get(i);
                        item.setUsed(true);
                        item.setTimeStamp(System.currentTimeMillis());
                    }
                }
            });
        }
    }

    public void setWallpaper(final String url){
        if(TextUtils.isEmpty(url)) return;

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ImageRealm item = realm.where(ImageRealm.class)
                        .equalTo("mUrl", url)
                        .findFirst();
                item.setIsWallpaper(true);
            }
        });
    }

    public void addListener(RealmDataChangeListener listener){
        mListeners.add(listener);
        if(mAllUsed != null && mAllUsed.isValid()){
            listener.onUsedDataChange(mAllUsed);
        }
        if(mAllUnUsed != null && mAllUnUsed.isValid()){
            listener.onUnusedDataChange(mAllUnUsed);
        }
        if(mAllFavor != null && mAllFavor.isValid()){
            listener.onFavorDataChange(mAllFavor);
        }
        if(mAllWallpaper != null && mAllWallpaper.isValid()){
            listener.onWallpaperDataChange(mAllWallpaper);
        }
    }

    public void removeListener(RealmDataChangeListener listener){
        mListeners.remove(listener);
    }

    private void query(){
        Log.v(TAG,"query(): current thread = " + Thread.currentThread().getId());
        if(realm == null || realm.isClosed()){
            realm = Realm.getDefaultInstance();
        }

        if (mAllUsed == null || !mAllUsed.isValid()) {
            mAllUsed = realm.where(ImageRealm.class)
                    .equalTo("mIsUsed", true)
                    .equalTo("mIsFavor", false)
                    .equalTo("mIsWallpaper", false)
                    .findAllAsync()
                    .sort("mTimeStamp", Sort.DESCENDING);
            mAllUsed.addChangeListener(mUsedDataChangeListener);
            Log.v(TAG, "query(): image count = " + mAllUsed.size());
        }else if(mAllUsed.isValid() && !mAllUsed.isEmpty()){
            for(RealmDataChangeListener listener : mListeners){
                listener.onUsedDataChange(mAllUsed);
            }
        }

        if(mAllUnUsed == null || !mAllUnUsed.isValid()) {
            mAllUnUsed = realm.where(ImageRealm.class)
                    .equalTo("mIsUsed", false)
                    .findAllAsync()
                    .sort("mTimeStamp", Sort.DESCENDING);
            mAllUnUsed.addChangeListener(mUnusedDataChangeListener);
        }else if(mAllUnUsed.isValid() && !mAllUnUsed.isEmpty()){
            for(RealmDataChangeListener listener : mListeners){
                listener.onUnusedDataChange(mAllUnUsed);
            }
        }

        if(mAllFavor == null || !mAllFavor.isValid()){
            mAllFavor = realm.where(ImageRealm.class)
                    .equalTo("mIsFavor",true)
                    .equalTo("mIsWallpaper", false)
                    .findAllAsync()
                    .sort("mTimeStamp",Sort.DESCENDING);
            mAllFavor.addChangeListener(mFavorDataChangeListener);
        }else if(mAllFavor.isValid() && !mAllFavor.isEmpty()){
            for(RealmDataChangeListener listener : mListeners){
                listener.onFavorDataChange(mAllFavor);
            }
        }

        if(mAllWallpaper == null || !mAllWallpaper.isValid()){
            mAllWallpaper = realm.where(ImageRealm.class)
                    .equalTo("mIsWallpaper", true)
                    .findAllAsync();
            mAllWallpaper.addChangeListener(mWallpaperDataChangeListener);
        }else if(mAllWallpaper.isValid()){
            for(RealmDataChangeListener listener : mListeners){
                listener.onWallpaperDataChange(mAllWallpaper);
            }
        }
    }

    private class UsedRealmDataChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{
        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            Log.v(TAG,"onChange(): current used image count = " + element.size());
            //element.sort("mTimeStamp",Sort.DESCENDING);
            for(RealmDataChangeListener listener : mListeners){
                listener.onUsedDataChange(element);
            }
        }
    }

    private class UnusedRealmDataChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{

        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            Log.v(TAG,"onChange(): current unused image count = " + element.size());
            for(RealmDataChangeListener listener : mListeners){
                listener.onUnusedDataChange(element);
            }
        }
    }

    private class FavorRealmDataChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{

        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            Log.v(TAG,"onChange(): current favor image count = " + element.size());
            for(RealmDataChangeListener listener : mListeners){
                listener.onFavorDataChange(element);
            }
        }
    }

    private class WallpaperDataChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{

        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            for(RealmDataChangeListener listener : mListeners){
                listener.onWallpaperDataChange(element);
            }
        }
    }

}