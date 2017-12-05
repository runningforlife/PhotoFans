package com.github.runningforlife.photosniffer.data.local;

import com.github.runningforlife.photosniffer.data.model.ImageRealm;

import java.util.HashMap;
import java.util.List;

import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * API to manipulate realm database
 */

public interface RealmApi {
    /**
     * insert data
     */
    void insertAsync(RealmObject data);

    /**
     * insert list realm object
     */
    void insertAsync(List<? extends RealmObject> data);

    /**
     * query async
     */
    RealmResults<? extends RealmObject> queryAsync(Class<? extends RealmObject> type, HashMap<String, String> params);

    /**
     * query sync
     */
    RealmResults<? extends RealmObject> querySync(Class<? extends RealmObject> type, HashMap<String,String> params);

    /**
     * updateAsync realm
     */
    boolean updateAsync(Class<? extends RealmObject> type, HashMap<String,String> params, HashMap<String, String> updatedValues);

    /**
     * updateAsync the realm object
     * @param data
     */
    void updateSync(Class<? extends RealmObject> type, List<? extends RealmObject> data, HashMap<String, String> updatedValues);

    /**
     * delete realm object
     */
    boolean delete(RealmObject data);


    /**
     * trim data to the given size
     */
    void trimData(Class<? extends RealmObject> type, HashMap<String, String> params, int max);

}
