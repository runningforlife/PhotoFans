package com.github.runningforlife.photofans.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * an helper class to CRUD realm database
 */

public class RealmHelper {
    private static final String TAG = "RealmHelper";

    private static RealmHelper sInstance;
    private Realm realm;
    // all the data we have
    private RealmResults<ImageRealm> mAllImages;
    private RealmResults<ImageRealm> mAllUnUsedImages;
    // callback to listen realm changes: update or query complete
    private List<RealmDataChangeListener> mListeners;

    public interface RealmDataChangeListener{
        void onRealmDataChange(RealmResults<ImageRealm> data);
    }

    public static RealmHelper getInstance() {
        if (sInstance == null) {

            sInstance = new RealmHelper();
        }

        return sInstance;
    }

    private RealmHelper() {
        realm = Realm.getDefaultInstance();
        mListeners = new ArrayList<>();
    }

    // this should be consistent with UI lifecycle: onCreate() or onResume()
    public void onStart(){
        query();
    }

    // this should be consistent with UI lifecycle: onDestroy()
    public void onDestroy(){
        if(mAllImages != null) {
            mAllImages.removeAllChangeListeners();
            mAllImages = null;
        }
        if(mAllUnUsedImages != null) {
            mAllUnUsedImages.removeAllChangeListeners();
            mAllUnUsedImages = null;
        }
        if(realm != null){
            realm.close();
            realm = null;
        }
    }

    public void writeAsync(final RealmObject info) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(info);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "onSuccess()");
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.v(TAG, "onError()" + error);
                //write(info);
            }
        });
    }

    public void writeAsync(final List<? extends RealmObject> data) {
        if(data == null || data.size() <= 0) return;

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Log.v(TAG,"execute(): saved data size = " + data.size());
                realm.copyToRealmOrUpdate(data);
            }
        });
    }

    public void queryAllAsync(){
        query();
    }

    public RealmResults<VisitedPageInfo> getAllVisitedPages(){
        Realm realm = Realm.getDefaultInstance();

        RealmResults<VisitedPageInfo> visited;
        visited = realm.where(VisitedPageInfo.class)
                .equalTo("mIsVisited", true)
                .findAll();

        return visited;
    }

    public void delete(final ImageRealm info){
        if(info == null) return;

        if (!mAllImages.isLoaded()) {
            mAllImages.load();
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mAllImages.get(mAllImages.indexOf(info))
                        .deleteFromRealm();
            }
        });
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

        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            Log.v(TAG,"onChange(): current image count = " + element.size());
            notifyListeners(element);
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
            mAllImages.addChangeListener(new RealmDataSetChangeListener());
            Log.v(TAG, "query(): image count = " + mAllImages.size());

/*            if(!mAllImages.isEmpty() && mAllImages.isValid()){
                notifyListeners(mAllImages);
            }*/
        }else if(mAllImages.isValid() && !mAllImages.isEmpty()){
            mAllImages.addChangeListener(new RealmDataSetChangeListener());
            notifyListeners(mAllImages);
        }

        if(mAllUnUsedImages == null || !mAllUnUsedImages.isValid()) {
            mAllUnUsedImages = realm.where(ImageRealm.class)
                    .equalTo("mIsUsed", false)
                    .findAllAsync()
                    .sort("mTimeStamp", Sort.DESCENDING);
            mAllUnUsedImages.addChangeListener(new RealmDataSetChangeListener());
        }
    }

    private void notifyListeners(RealmResults<ImageRealm> element){
        if(mListeners.isEmpty()) return;

        for (RealmDataChangeListener listener : mListeners) {
            listener.onRealmDataChange(element);
        }
    }

}