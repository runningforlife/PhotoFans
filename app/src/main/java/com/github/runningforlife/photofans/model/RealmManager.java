package com.github.runningforlife.photofans.model;

import android.util.Log;

import com.github.runningforlife.photofans.presenter.LifeCycle;
import com.github.runningforlife.photofans.utils.SharedPrefUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * an helper class to CRUD realm database
 */

public class RealmManager implements LifeCycle{
    private static final String TAG = "RealmManager";

    private static RealmManager sInstance = new RealmManager();
    private AtomicInteger mRealRefCount = new AtomicInteger(0);
    private Realm realm;
    // all the data we have
    private RealmResults<ImageRealm> mAllImages;
    private RealmResults<ImageRealm> mAllUnUsedImages;
    // callback to listen realm changes: update or query complete
    private Set<RealmDataChangeListener> mListeners;
    private RealmDataSetChangeListener mDataSetChangeListener;

    public interface RealmDataChangeListener{
        void onRealmDataChange(RealmResults<ImageRealm> data);
    }

    public static RealmManager getInstance() {
        return sInstance;
    }

    private RealmManager() {
        realm = Realm.getDefaultInstance();
        mListeners = new HashSet<>();
        mDataSetChangeListener = new RealmDataSetChangeListener();
    }

    // this should be consistent with UI lifecycle: onCreate() or onStart()
    @Override
    public void onStart(){
        query();
        Log.d(TAG,"onStart(): ref count = " + mRealRefCount.incrementAndGet());
    }

    // this should be consistent with UI lifecycle: onDestroy()
    @Override
    public void onDestroy(){
        Log.d(TAG,"onDestroy(): ref count = " + mRealRefCount.get());
        if(mRealRefCount.decrementAndGet() == 0) {
            //trimData();
            if (mAllImages != null) {
                mAllImages.removeAllChangeListeners();
                mAllImages = null;
            }
            if (mAllUnUsedImages != null) {
                mAllUnUsedImages.removeAllChangeListeners();
                mAllUnUsedImages = null;
            }
            if (realm != null) {
                realm.close();
                realm = null;
            }
        }
        mListeners.clear();
        // trim data size
        //trimData();
    }

    //Note: Realm objects can only be accessed on the thread they were created
    public void writeAsync(final RealmObject info) {
        Realm r = Realm.getDefaultInstance();
        try {
            r.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(info);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Log.v(TAG,"writeAsync(): success");
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

        //trimData();
    }

    public void writeAsync(final List<? extends RealmObject> data) {
        if(data == null || data.size() <= 0) return;
        Realm r = Realm.getDefaultInstance();
        try {
            r.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Log.v(TAG, "execute(): saved data size = " + data.size());
                    realm.copyToRealmOrUpdate(data);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Log.v(TAG,"writeAsync(): success");
                }
            });
        }finally {
            r.close();
        }

        //trimData();
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
    public void trimData(){
        int maxImgs = SharedPrefUtil.getMaxReservedImages();
        if(mAllImages != null && mAllImages.isValid()
                && mAllImages.isLoaded()){
            mAllImages.sort("mTimeStamp",Sort.DESCENDING);

            realm.beginTransaction();
            for(int i = maxImgs; i < mAllImages.size(); ++i){
                mAllImages.get(i).deleteFromRealm();
            }
            realm.commitTransaction();
        }
    }

    public void addListener(RealmDataChangeListener listener){
        mListeners.add(listener);
        if(mAllImages != null && !mAllImages.isEmpty()){
            listener.onRealmDataChange(mAllImages);
        }
    }

    public void removeListener(RealmDataChangeListener listener){
        mListeners.remove(listener);
    }

    private class RealmDataSetChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{
        //FIXME: sometimes, Realm will call this too many times, which may cause sluggish
        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            Log.v(TAG,"onChange(): current image count = " + element.size());
            //element.sort("mTimeStamp",Sort.DESCENDING);
            RealmManager.this.notify(element);
        }
    }

    private void query(){
        Log.v(TAG,"query(): current thread = " + Thread.currentThread().getId());
        if(realm == null || realm.isClosed()){
            realm = Realm.getDefaultInstance();
        }

        if (mAllImages == null || !mAllImages.isValid()) {
            mAllImages = realm.where(ImageRealm.class)
                    .equalTo("mIsUsed", true)
                    .findAllAsync()
                    .sort("mTimeStamp", Sort.DESCENDING);
            mAllImages.addChangeListener(mDataSetChangeListener);
            Log.v(TAG, "query(): image count = " + mAllImages.size());

        }else if(mAllImages.isValid() && !mAllImages.isEmpty()){
            //mAllImages.addChangeListener(new RealmDataSetChangeListener());
            notify(mAllImages);
            //trimData();
        }

        //trimData();

        if(mAllUnUsedImages == null || !mAllUnUsedImages.isValid()) {
            mAllUnUsedImages = realm.where(ImageRealm.class)
                    .equalTo("mIsUsed", false)
                    .findAllAsync()
                    .sort("mTimeStamp", Sort.DESCENDING);
            mAllUnUsedImages.addChangeListener(mDataSetChangeListener);
        }
    }

    private void notify(RealmResults<ImageRealm> element){
        if(mListeners.isEmpty()) return;

        for (RealmDataChangeListener listener : mListeners) {
            listener.onRealmDataChange(element);
        }
    }

}