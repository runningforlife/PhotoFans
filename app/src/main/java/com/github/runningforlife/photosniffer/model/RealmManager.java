package com.github.runningforlife.photosniffer.model;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.github.runningforlife.photosniffer.presenter.LifeCycle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
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

    private RealmResults<QuotePageInfo> mAllQuotePage;
    private RealmResults<QuoteRealm> mAllUsedQuotes;
    private RealmResults<QuoteRealm> mAllUnUsedQuotes;
    // callback to listen realm changes: update or query complete
    private List<UsedDataChangeListener> mUsedChangeListener;
    private List<UnusedDataChangeListener> mUnusedChangeListener;
    private List<FavorDataChangeListener> mFavorChangeListener;
    private List<WallpaperDataChangeListener> mWallpaperChangeListener;
    private List<QuoteDataChangeListener> mQuoteDataChangeListener;

    private UsedRealmDataChangeListener mUsedDataChangeListener;
    private UnusedRealmDataChangeListener mUnusedDataChangeListener;
    // async task
    private RealmAsyncTask mAsyncTask;
    // remove item lock
    private final Object mRemoveLock = new Object();
    private BlockingDeque<RemoveItemTransaction> mRemovedQue;

    public interface UsedDataChangeListener{
        void onUsedDataChange(RealmResults<ImageRealm> data);
    }

    public interface UnusedDataChangeListener{
        void onUnusedDataChange(RealmResults<ImageRealm> data);
    }

    public interface FavorDataChangeListener{
        void onFavorDataChange(RealmResults<ImageRealm> data);
    }

    public interface WallpaperDataChangeListener{
        void onWallpaperDataChange(RealmResults<ImageRealm> data);
    }

    public interface QuoteDataChangeListener{
        void onQuoteDataChange(RealmResults<QuoteRealm> data);
    }

    public void addUsedDataChangeListener(UsedDataChangeListener listener){
        if (!mUsedChangeListener.contains(listener)) {
            mUsedChangeListener.add(listener);
            if(mAllUnUsed != null && mAllUsed.isLoaded()){
                listener.onUsedDataChange(mAllUsed);
            }
        }
    }

    public void removeUsedDataChangeListener(UsedDataChangeListener listener){
        mUsedChangeListener.remove(listener);
    }

    public void addUnusedDataChangeListener(UnusedDataChangeListener listener){
        if(!mUnusedChangeListener.contains(listener)){
            mUnusedChangeListener.add(listener);
            if(mAllUnUsed != null && mAllUnUsed.isLoaded()){
                listener.onUnusedDataChange(mAllUnUsed);
            }
        }
    }

    public void removeUnusedDataChangeListener(UnusedDataChangeListener listener){
        if(listener != null){
            mUnusedChangeListener.remove(listener);
        }
    }

    public void addFavorDataChangeListener(FavorDataChangeListener listener){
        if(listener != null){
            mFavorChangeListener.add(listener);
            if(mAllFavor != null && mAllFavor.isLoaded()){
                listener.onFavorDataChange(mAllFavor);
            }
        }
    }

    public void removeFavorDataChangeListener(FavorDataChangeListener listener){
        if(listener != null){
            mFavorChangeListener.remove(listener);
        }
    }

    public void addWallpaperDataChangeListener(WallpaperDataChangeListener listener){
        if(listener != null){
            mWallpaperChangeListener.add(listener);
            if(mAllWallpaper != null && mAllWallpaper.isLoaded()){
                listener.onWallpaperDataChange(mAllWallpaper);
            }
        }
    }

    public void removeWallpaperDataChangeListener(WallpaperDataChangeListener listener){
        if(listener != null){
            mWallpaperChangeListener.remove(listener);
        }
    }

    public void addQuoteDataChangeListener(QuoteDataChangeListener listener){
        if(listener != null){
            mQuoteDataChangeListener.add(listener);
        }
    }

    public void removeQuoteDataChangeListener(QuoteDataChangeListener listener){
        mQuoteDataChangeListener.remove(listener);
    }

    public static RealmManager getInstance() {
        return sInstance;
    }

    private RealmManager() {
        realm = Realm.getDefaultInstance();
        mUsedDataChangeListener = new UsedRealmDataChangeListener();
        mUnusedDataChangeListener = new UnusedRealmDataChangeListener();

        mUsedChangeListener = new ArrayList<>();
        mUnusedChangeListener = new ArrayList<>();
        mFavorChangeListener = new ArrayList<>();
        mWallpaperChangeListener = new ArrayList<>();
        mQuoteDataChangeListener = new ArrayList<>();

        queryAllAsync();
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
            if(mAllUsedQuotes != null){
                mAllUsedQuotes.removeAllChangeListeners();
                mAllUsedQuotes = null;
            }
            if (realm != null) {
                realm.close();
                realm = null;
            }
        }
        // cancel transaction
        if(mAsyncTask != null && !mAsyncTask.isCancelled()){
            mAsyncTask.cancel();
        }
    }

    public void saveQuotePage(final List<QuotePageInfo> pageList){
        Realm r = Realm.getDefaultInstance();

        try{
            r.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(pageList);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Log.v(TAG,"saveQuotePage(): success");
                }
            });
        }finally {
            r.close();
        }
    }

    public void saveQuoteRealm(final List<QuoteRealm> data){
        Realm r = Realm.getDefaultInstance();

        try{
            r.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(data);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Log.v(TAG,"saveQuoteRealm(): success");
                }
            });
        } finally {
            r.close();
        }
    }

    //Note: Realm objects can only be accessed on the thread they were created
    public void savePageAsync(final ImagePageInfo info) {
        Realm r = Realm.getDefaultInstance();
        try {
            r.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<ImagePageInfo> all = realm.where(ImagePageInfo.class)
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

    public void savePageAsync(final List<ImagePageInfo> data) {
        if(data == null || data.size() <= 0) return;
        Realm r = Realm.getDefaultInstance();
        try {
            r.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Log.v(TAG, "execute(): saved data size = " + data.size());
                    RealmResults<ImagePageInfo> all = realm.where(ImagePageInfo.class)
                            .findAll();
                    for(ImagePageInfo page : data) {
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

    public static RealmResults<ImagePageInfo> getAllVisitedPages(Realm r){
        RealmResults<ImagePageInfo> visited = r.where(ImagePageInfo.class)
                .equalTo("mIsVisited", true)
                .isNotNull("mUrl")
                .findAll();

        return visited;
    }

    public static RealmResults<ImagePageInfo> getAllUnvisitedImagePages(Realm r){
        RealmResults<ImagePageInfo> unVisited = r.where(ImagePageInfo.class)
                .equalTo("mIsVisited",false)
                .isNotNull("mUrl")
                .findAll();

        return unVisited;
    }

    public static RealmResults<QuotePageInfo> getAllUnvisitedQuotePages(Realm r){
        return r.where(QuotePageInfo.class)
                .equalTo("isVisited", false)
                .isNotNull("url")
                .findAll();
    }

    public void delete(final RealmObject object){
        if(object == null) return;

            Realm r = Realm.getDefaultInstance();

            r.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    synchronized (mRemoveLock) {
                        object.deleteFromRealm();
                    }
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
                        .equalTo("mIsFavor", false)
                        .equalTo("mIsWallpaper", false)
                        .findAllSorted("mTimeStamp",Sort.ASCENDING);
                int diff = used.size() - maxImgs;
                for(int i = 0; i <  diff; ++i){
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
        }else if(mAllUsed.isValid()){
            for(UsedDataChangeListener listener : mUsedChangeListener){
                listener.onUsedDataChange(mAllUsed);
            }
        }

        if(mAllUnUsed == null || !mAllUnUsed.isValid()) {
            mAllUnUsed = realm.where(ImageRealm.class)
                    .equalTo("mIsUsed", false)
                    .findAllAsync()
                    .sort("mTimeStamp", Sort.DESCENDING);
            mAllUnUsed.addChangeListener(mUnusedDataChangeListener);
        }else if(mAllUnUsed.isValid()){
            for(UnusedDataChangeListener listener : mUnusedChangeListener){
                listener.onUnusedDataChange(mAllUnUsed);
            }
        }

        if(mAllFavor == null || !mAllFavor.isValid()){
            mAllFavor = realm.where(ImageRealm.class)
                    .equalTo("mIsFavor",true)
                    .findAllAsync()
                    .sort("mTimeStamp",Sort.DESCENDING);
            mAllFavor.addChangeListener(new FavorRealmDataChangeListener());
        }else if(mAllFavor.isValid() && !mAllFavor.isEmpty()){
            for(FavorDataChangeListener listener : mFavorChangeListener){
                listener.onFavorDataChange(mAllFavor);
            }
        }

        if(mAllWallpaper == null || !mAllWallpaper.isValid()){
            mAllWallpaper = realm.where(ImageRealm.class)
                    .equalTo("mIsWallpaper", true)
                    .findAllAsync();
            mAllWallpaper.addChangeListener(new WallpaperRealmDataChangeListener());
        }else if(mAllWallpaper.isValid()){
            for(WallpaperDataChangeListener listener : mWallpaperChangeListener){
                listener.onWallpaperDataChange(mAllWallpaper);
            }
        }

        if(mAllUsedQuotes == null || !mAllUsedQuotes.isValid()){
            mAllUsedQuotes = realm.where(QuoteRealm.class)
                    .equalTo("isUsed", true)
                    .findAllAsync()
                    .sort("savedTime", Sort.DESCENDING);
            mAllUsedQuotes.addChangeListener(new QuoteRealmDataChangeListener());
        }else if(mAllUsedQuotes.isValid()){
            for(QuoteDataChangeListener listener : mQuoteDataChangeListener){
                listener.onQuoteDataChange(mAllUsedQuotes);
            }
        }
    }

    private class UsedRealmDataChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{
        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            Log.v(TAG,"onChange(): current used image count = " + element.size());
            //element.sort("mTimeStamp",Sort.DESCENDING);
            for(UsedDataChangeListener listener : mUsedChangeListener){
                listener.onUsedDataChange(element);
            }
        }
    }

    private class UnusedRealmDataChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{

        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            Log.v(TAG,"onChange(): current unused image count = " + element.size());
            for(UnusedDataChangeListener listener : mUnusedChangeListener){
                listener.onUnusedDataChange(element);
            }
        }
    }

    private class FavorRealmDataChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{

        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            Log.v(TAG,"onChange(): current favor image count = " + element.size());
            for(FavorDataChangeListener listener : mFavorChangeListener){
                listener.onFavorDataChange(element);
            }
        }
    }

    private class WallpaperRealmDataChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{

        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            for(WallpaperDataChangeListener listener : mWallpaperChangeListener){
                listener.onWallpaperDataChange(element);
            }
        }
    }

    private class QuoteRealmDataChangeListener implements RealmChangeListener<RealmResults<QuoteRealm>>{

        @Override
        public void onChange(RealmResults<QuoteRealm> element) {
            for(QuoteDataChangeListener listener : mQuoteDataChangeListener){
                listener.onQuoteDataChange(element);
            }
        }
    }

}