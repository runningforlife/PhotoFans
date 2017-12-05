package com.github.runningforlife.photosniffer.data.local;

import android.util.Log;

import com.github.runningforlife.photosniffer.data.model.ImageRealm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static RealmApiImpl getInstance() {
        return sInstance;
    }

    @Override
    public void insertAsync(final RealmObject data) {
        Log.v(TAG,"insertAsync()");
        Realm r = Realm.getDefaultInstance();
        r.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(data);
            }
        });
    }

    @Override
    public void insertAsync(final List<? extends RealmObject> data) {
        Log.v(TAG,"insertAsync()");
        Realm r = Realm.getDefaultInstance();
        r.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(data);
            }
        });
    }

    @Override
    public RealmResults<? extends RealmObject> queryAsync(Class<? extends RealmObject> type, HashMap<String, String> params) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<? extends RealmObject> query = realm.where(type);

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
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<? extends RealmObject> query = realm.where(type);

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
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmQuery<? extends RealmObject> query = realm.where(type);
                if(type == ImageRealm.class){
                    Set<Map.Entry<String, String>> entries = params.entrySet();
                    for(Map.Entry<String,String> entry : entries){
                        String field = entry.getKey();
                        switch (field){
                            case "mTimeStamp":
                                query.equalTo(field, Long.parseLong(entry.getValue()));
                                break;
                            case "mIsUsed":
                            case "mIsFavor":
                            case "mIsWallpaper":
                                query.equalTo(field, Boolean.parseBoolean(entry.getValue()));
                                break;
                            default:
                                query.equalTo(field, entry.getValue());
                        }
                    }

                    RealmResults<? extends RealmObject> rr = query.findAll();
                    for(int i = 0; i < rr.size(); ++i) {
                        ImageRealm ir = (ImageRealm) rr.get(i);
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
    public void updateSync(final Class<? extends RealmObject> type, final List<? extends RealmObject> data, final HashMap<String, String> updatedValues) {
        Log.v(TAG,"updateSync()");
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (type == (ImageRealm.class)){
                    Set<Map.Entry<String,String>> entries = updatedValues.entrySet();
                    for (RealmObject ro : data) {
                        ImageRealm ir = (ImageRealm)ro;

                        for (Map.Entry<String, String> entry : entries) {
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
    }

    @Override
    public boolean delete(final RealmObject data) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                data.deleteFromRealm();
            }
        });
        return false;
    }

    @Override
    public void trimData(Class<? extends RealmObject> type, HashMap<String, String> params, int max) {

    }
}
