package jason.github.com.photofans.repository;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import jason.github.com.photofans.model.ImageItem;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.service.ImageRetrieveService;

/**
 * an helper class to CRUD realm database
 */

public class RealmHelper {
    private static final String TAG = "RealmHelper";

    private Realm realm;
    private static RealmHelper sInstance;
    // all the data we have
    private RealmResults<ImageRealm> mAllImages;
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

        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name("PhotoFans")
                .build();

        realm = Realm.getInstance(realmConfig);

        mListeners = new ArrayList<>();
    }


    public boolean isEmpty(){
        return mAllImages.size() == 0;
    }

    // this should be consistent with UI lifecycle: onCreate() or onResume()
    public void onStart(){
        mAllImages = realm.where(ImageRealm.class).findAllAsync();
        mAllImages.addChangeListener(new RealmDataSetChangeListener());
    }

    // this should be consistent with UI lifecycle: onDestroy()
    public void onDestroy(){
        mAllImages.removeAllChangeListeners();
        //realm.close();
    }

    public void write(ImageRealm info) {
        Log.v(TAG,"image info = " + info);
        realm.beginTransaction();

        realm.copyFromRealm(info);

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

    public void writeAsync(final ImageItem item){
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ImageRealm info = realm.createObject(ImageRealm.class);
                info.setName(item.getName());
                info.setUrl(item.getUrl());
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

    public RealmResults<ImageRealm> queryAll(){
        if(mAllImages != null && mAllImages.isLoaded()) {
            mAllImages.load();
        }

        return mAllImages;
    }

    public void queryAllAsync(){
        mAllImages = realm.where(ImageRealm.class).findAllAsync();
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
            for(RealmDataChangeListener listener : mListeners){
                listener.onRealmDataChange(element);
            }
        }
    }

}