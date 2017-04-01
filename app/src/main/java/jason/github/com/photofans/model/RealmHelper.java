package jason.github.com.photofans.model;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * an helper class to CRUD realm database
 */

public class RealmHelper {
    private static final String TAG = "RealmHelper";

    private Realm realm;
    private static RealmHelper sInstance;
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


    public boolean isEmpty(){
        return mAllImages.size() == 0;
    }

    // this should be consistent with UI lifecycle: onCreate() or onResume()
    public void onStart(){
        queryAndListen();

        RealmResults<VisitedPageInfo> allPages = realm.where(VisitedPageInfo.class)
                .equalTo("mIsVisited",false)
                .findAll();
        allPages.addChangeListener(new VisitPageChangeListener());
    }

    // this should be consistent with UI lifecycle: onDestroy()
    public void onDestroy(){
        mAllImages.removeAllChangeListeners();
        //realm.close();
    }

    public void write(final ImageRealm info) {
        Log.v(TAG,"image info = " + info);
        realm.beginTransaction();

        realm.copyToRealmOrUpdate(info);

        realm.commitTransaction();
    }

    public void writeAsync(final ImageRealm info) {
        realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyFromRealm(info);
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Log.v(TAG,"onSuccess()");
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        Log.v(TAG,"onError()" + error);
                        //write(info);
                    }
                });
    }

    public void writeAsync(final List<ImageRealm> data) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Log.v(TAG,"execute(): data size = " + data.size());
                realm.copyToRealmOrUpdate(data);
            }
        });
    }

    public RealmResults<ImageRealm> queryAll(){
        if(mAllImages != null && !mAllImages.isLoaded()) {
            mAllImages.load();
        }

        return mAllImages;
    }

    public RealmResults<ImageRealm> queryAllUnusedImages(){
        if(!mAllUnUsedImages.isLoaded()){
            mAllUnUsedImages.load();
        }

        return mAllUnUsedImages;
    }

    public void queryAllAsync(){
        queryAndListen();
    }

    public void delete(final String url) {
        if(TextUtils.isEmpty(url)) return;

        if (mAllImages != null && !mAllImages.isLoaded()) {
            // force load data
            mAllImages.load();
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for(ImageRealm info: mAllImages){
                    if(url.equals(info.getUrl())){
                        info.deleteFromRealm();
                    }
                }
            }
        });
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
    }

    public void removeListener(RealmDataChangeListener listener){
        mListeners.remove(listener);
    }

    private class RealmDataSetChangeListener implements RealmChangeListener<RealmResults<ImageRealm>>{

        @Override
        public void onChange(RealmResults<ImageRealm> element) {
            Log.v(TAG,"onChange(): current image count = " + element.size());
            for (RealmDataChangeListener listener : mListeners) {
                listener.onRealmDataChange(element);
            }
        }
    }

    private class VisitPageChangeListener implements RealmChangeListener<RealmResults<VisitedPageInfo>>{
        @Override
        public void onChange(RealmResults<VisitedPageInfo> element){
            Log.v(TAG,"onChange():current marked pages size " + element.size());
        }
    }

    private void queryAndListen(){
        Log.v(TAG,"queryAndListen(): current thread = " + Thread.currentThread().getId());
        //FIXME: findALlSortedAsync has a problem: always call onChange event there
        // is no any change in database
        mAllImages = realm.where(ImageRealm.class)
                .equalTo("mIsUsed",true)
                .findAllAsync()
                .sort("mTimeStamp", Sort.DESCENDING);
        mAllImages.addChangeListener(new RealmDataSetChangeListener());
        // force to load
/*        if(!mAllImages.isLoaded()) {
            mAllImages.load();
        }*/

        mAllUnUsedImages = realm.where(ImageRealm.class)
                .equalTo("mIsUsed",false)
                .findAllAsync()
                .sort("mTimeStamp",Sort.DESCENDING);
        //mAllUnUsedImages.load();
        mAllUnUsedImages.addChangeListener(new RealmDataSetChangeListener());
    }

}