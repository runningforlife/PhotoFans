package com.github.runningforlife.photosniffer.data.local;

import android.util.Log;

import com.github.runningforlife.photosniffer.data.model.ImageRealm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * API to manipulate realm database
 */

public class RealmApiImpl implements RealmApi {
    private static final String TAG = "RealmApi";

    private static RealmApiImpl sInstance = new RealmApiImpl();
    private static AtomicInteger sRefCount = new AtomicInteger(0);
    private Realm mRealm;

    private RealmApiImpl() {
        mRealm = Realm.getDefaultInstance();
    }

    public static RealmApiImpl getInstance() {
        sRefCount.incrementAndGet();
        return sInstance;
    }

    @Override
    public void insertAsync(final RealmObject data) {
        Log.v(TAG,"insertAsync()");
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(data);
            }
        });
    }

    @Override
    public void insertAsync(final List<? extends RealmObject> data) {
        Log.v(TAG,"insertAsync()");
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(data);
            }
        });
    }

    @Override
    public RealmResults<? extends RealmObject> queryAsync(Class<? extends RealmObject> type, HashMap<String, String> params) {
        RealmQuery<? extends RealmObject> query = mRealm.where(type);

        Set<Map.Entry<String, String>> entries = params.entrySet();
        for(Map.Entry<String,String> entry : entries){
            if(type == ImageRealm.class){
                String field = entry.getKey();
                switch (field){
                    case "mTimeStamp":
                        query.equalTo(field, Long.parseLong(entry.getValue()));
                        break;
                    case "mIsUsed":
                    case "mIsFavor":
                    case "mIsWallpaper":
                    case "mIsCached":
                        query.equalTo(field, Boolean.parseBoolean(entry.getValue()));
                        break;
                    default:
                        query.equalTo(field, entry.getValue());
                }
            }
        }

        return query.findAllSortedAsync("mTimeStamp", Sort.DESCENDING);
    }

    @Override
    public RealmResults<? extends RealmObject> querySync(Class<? extends RealmObject> type, HashMap<String, String> params) {
        RealmQuery<? extends RealmObject> query = mRealm.where(type);

        Set<Map.Entry<String, String>> entries = params.entrySet();
        for(Map.Entry<String,String> entry : entries){
            if (type == ImageRealm.class) {
                String field = entry.getKey();
                switch (field){
                    case "mTimeStamp":
                        query.equalTo(field, Long.parseLong(entry.getValue()));
                        break;
                    case "mIsUsed":
                    case "mIsFavor":
                    case "mIsWallpaper":
                    case "mIsCached":
                        query.equalTo(field, Boolean.parseBoolean(entry.getValue()));
                        break;
                    default:
                        query.equalTo(field, entry.getValue());
                }
            }
        }

        return query.findAll().sort("mTimeStamp", Sort.DESCENDING);
    }

    @Override
    public boolean updateAsync(final Class<? extends RealmObject> type, final HashMap<String, String> params, final HashMap<String, String> updatedValues) {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmQuery<? extends RealmObject> query = realm.where(type);
                if (type == ImageRealm.class) {
                    Set<Map.Entry<String, String>> entries = params.entrySet();
                    for(Map.Entry<String,String> entry : entries){
                        String field = entry.getKey();
                        switch (field) {
                            case "mTimeStamp":
                                query.equalTo(field, Long.parseLong(entry.getValue()));
                                break;
                            case "mIsUsed":
                            case "mIsFavor":
                            case "mIsWallpaper":
                            case "mIsCached":
                                query.equalTo(field, Boolean.parseBoolean(entry.getValue()));
                                break;
                            default:
                                query.equalTo(field, entry.getValue());
                        }
                    }

                    RealmResults<? extends RealmObject> rr = query.findAll();
                    for(int i = 0; i < rr.size(); ++i) {
                        ImageRealm ir = (ImageRealm) rr.get(i);
                        if (ir == null) continue;
                        // updateAsync values
                        Set<Map.Entry<String, String>> entrySet = updatedValues.entrySet();
                        for (Map.Entry<String, String> entry : entrySet) {
                            String field = entry.getKey();
                            switch (field) {
                                case "mTimeStamp":
                                    ir.setTimeStamp(Long.parseLong(entry.getValue()));
                                    break;
                                case "mIsUsed":
                                    ir.setUsed(Boolean.parseBoolean(entry.getValue()));
                                    break;
                                case "mIsFavor":
                                    ir.setIsFavor(Boolean.parseBoolean(entry.getValue()));
                                case "mIsWallpaper":
                                    ir.setIsWallpaper(Boolean.parseBoolean(entry.getValue()));
                                    break;
                                case "mIsCached":
                                    ir.setIsCached(Boolean.parseBoolean(entry.getValue()));
                                case "mName":
                                    ir.setName(entry.getValue());
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
        });

        return true;
    }

    @Override
    public boolean deleteSync(final RealmObject data) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                data.deleteFromRealm();
            }
        });

        return false;
    }

    @Override
    public void deleteAsync(Class<? extends RealmObject> type, final HashMap<String, String> params) {
        Log.v(TAG,"deleteAsync()");
        final RealmQuery<? extends RealmObject> query = mRealm.where(type);

        if (ImageRealm.class == type) {
            mRealm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Set<Map.Entry<String, String>> entries = params.entrySet();
                    for(Map.Entry<String,String> entry : entries){
                        String field = entry.getKey();
                        switch (field) {
                            case "mUrl":
                                query.equalTo(field, entry.getValue());
                            case "mTimeStamp":
                                query.equalTo(field, Long.parseLong(entry.getValue()));
                                break;
                            case "mIsUsed":
                            case "mIsFavor":
                            case "mIsWallpaper":
                            case "mIsCached":
                                query.equalTo(field, Boolean.parseBoolean(entry.getValue()));
                                break;
                            default:
                                query.equalTo(field, entry.getValue());
                        }
                    }

                    RealmResults<? extends RealmObject> rr = query.findAll();

                    rr.deleteAllFromRealm();
                }
            });
        }
    }

    @Override
    public void markUnusedRealm(final int num) {
        Log.v(TAG,"markUnusedRealm()");
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<ImageRealm> unused = realm.where(ImageRealm.class)
                        .equalTo("mIsUsed", false)
                        .findAll();

                for (int i = 0; i < num && i < unused.size(); ++i) {
                    ImageRealm ir = unused.get(i);
                    ir.setUsed(true);
                    ir.setTimeStamp(System.currentTimeMillis());
                }
            }
        });
    }

    @Override
    public void trimData(Class<? extends RealmObject> type, HashMap<String, String> params, int max) {
        Log.v(TAG,"trimData()");

        RealmQuery<? extends RealmObject> query = mRealm.where(type);

        if (ImageRealm.class == type) {
            Set<Map.Entry<String, String>> entries = params.entrySet();
            for(Map.Entry<String,String> entry : entries){
                String field = entry.getKey();
                switch (field) {
                    case "mTimeStamp":
                        query.equalTo(field, Long.parseLong(entry.getValue()));
                        break;
                    case "mIsUsed":
                    case "mIsFavor":
                    case "mIsWallpaper":
                    case "mIsCached":
                        query.equalTo(field, Boolean.parseBoolean(entry.getValue()));
                        break;
                    default:
                        query.equalTo(field, entry.getValue());
                }
            }

            RealmResults<? extends RealmObject> rr = query.findAll().sort("mTimeStamp", Sort.DESCENDING);

            for (int i = max; i < rr.size(); ++i) {
                rr.get(i).deleteFromRealm();
            }
        }
    }

    @Override
    public void decRef() {
        Log.v(TAG,"decRef()");
        if (sRefCount.decrementAndGet() == 0) {
            mRealm.close();
        }
    }
}
