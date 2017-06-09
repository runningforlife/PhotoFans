package com.github.runningforlife.photosniffer.model;

import android.util.Log;

import com.github.runningforlife.photosniffer.presenter.LifeCycle;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

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

    private static final RealmManager sInstance = new RealmManager();
    private static AtomicInteger sRealmRefCount = new AtomicInteger(0);
    private Realm realm;
    // all the data we have
    private RealmResults<ImageRealm> mAllUsedImages;
    private RealmResults<ImageRealm> mAllUnUsedImages;
    private RealmResults<ImageRealm> mAllFavorImages;
    // callback to listen realm changes: update or query complete
    private Set<RealmDataChangeListener> mListeners;
    private UsedRealmDataChangeListener mUsedDataChangeListener;
    private UnusedRealmDataChangeListener mUnusedDataChangeListener;
    private FavorRealmDataChangeListener mFavorDataChangeListener;

    public interface RealmDataChangeListener{
        void onUsedRealmDataChange(RealmResults<ImageRealm> data);
        void onUnusedRealmDataChange(RealmResults<ImageRealm> data);
        void onFavorRealmDataChange(RealmResults<ImageRealm> data);
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
            if (mAllUsedImages != null) {
                mAllUsedImages.removeAllChangeListeners();
                mAllUsedImages = null;
            }
            if (mAllUnUsedImages != null) {
                mAllUnUsedImages.removeAllChangeListeners();
                mAllUnUsedImages = null;
            }
            if(mAllFavorImages != null){
                mAllFavorImages.removeAllChangeListeners();
                mAllFavorImages = null;
            }
            if (realm != null) {
                realm.close();
                realm = null;
            }
        }
        mListeners.clear();
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
        if(mAllUsedImages != null && mAllUsedImages.isValid()
                && mAllUsedImages.isLoaded()){
            mAllUsedImages.sort("mTimeStamp",Sort.DESCENDING);

            realm.beginTransaction();
            for(int i = maxImgs; i < mAllUsedImages.size(); ++i){
                mAllUsedImages.get(i).deleteFromRealm();
            }
            realm.commitTransaction();
        }
    }

    public void addListener(RealmDataChangeListener listener){
        mListeners.add(listener);
        if(mAllUsedImages != null && mAllUsedImages.isValid()){
            listener.onUsedRealmDataChange(mAllUsedImages);
        }
        if(mAllUnUsedImages != null && mAllUnUsedImages.isValid()){
            listener.onUnusedRealmDataChange(mAllUnUsedImages);
        }
        if(mAllFavorImages != null && mAllFavorImages.isValid()){
            listener.onFavorRealmDataChange(mAllFavorImages);
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

        if (mAllUsedImages == null || !mAllUsedImages.isValid()) {
            mAllUsedImages = realm.where(ImageRealm.class)
                    .equalTo("mIsUsed", true)
                    .equalTo("mIsFavor", false)
                    .findAllAsync()
                    .sort("mTimeStamp", Sort.DESCENDING);
            mAllUsedImages.addChangeListener(mUsedDataChangeListener);
            Log.v(TAG, "query(): image count = " + mAllUsedImages.size());
        }else if(mAllUsedImages.isValid() && !mAllUsedImages.isEmpty()){
            for(RealmDataChangeListener listener : mListeners){
                listener.onUsedRealmDataChange(mAllUsedImages);
            }
        }

        if(mAllUnUsedImages == null || !mAllUnUsedImages.isValid()) {
            mAllUnUsedImages = realm.where(ImageRealm.class)
                    .equalTo("mIsUsed", false)
                    .findAllAsync()
                    .sort("mTimeStamp", Sort.DESCENDING);
            mAllUnUsedImages.addChangeListener(mUnusedDataChangeListener);
        }else if(mAllUnUsedImages.isValid() && !mAllUnUsedImages.isEmpty()){
            for(RealmDataChangeListener listener : mListeners){
                listener.onUnusedRealmDataChange(mAllUnUsedImages);
            }
        }

        if(mAllFavorImages == null || !mAllFavorImages.isValid()){
            mAllFavorImages = realm.where(ImageRealm.class)
                    .equalTo("mIsFavor",true)
                    .findAllAsync()
                    .sort("mTimeStamp",Sort.DESCENDING);
            mAllFavorImages.addChangeListener(mFavorDataChangeListener);
        }else if(mAllFavorImages.isValid() && !mAllFavorImages.isEmpty()){
            for(RealmDataChangeListener listener : mListeners){
                listener.onFavorRealmDataChange(mAllFavorImages);
            }
        }
    }

    private class UsedRealmDataChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{
        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            Log.v(TAG,"onChange(): current used image count = " + element.size());
            //element.sort("mTimeStamp",Sort.DESCENDING);
            for(RealmDataChangeListener listener : mListeners){
                listener.onUsedRealmDataChange(element);
            }
        }
    }

    private class UnusedRealmDataChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{

        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            Log.v(TAG,"onChange(): current unused image count = " + element.size());
            for(RealmDataChangeListener listener : mListeners){
                listener.onUnusedRealmDataChange(element);
            }
        }
    }

    private class FavorRealmDataChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{

        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            Log.v(TAG,"onChange(): current favor image count = " + element.size());
            for(RealmDataChangeListener listener : mListeners){
                listener.onFavorRealmDataChange(element);
            }
        }
    }

}