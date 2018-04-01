package com.github.runningforlife.photosniffer.data.remote;

import java.io.File;

/**
 * API to access lean cloud
 */

public interface CloudApi {

    /*
     * save local file to cloud
     */
    void saveFile(File file);

    /*
     * upload user advice to cloud
     */
    void saveAdvice(String email, String advice, com.avos.avoscloud.SaveCallback callback);

    /*
     * record the number of user
     */
    void newUser();

    /**
     * save latest pola collections count
     */
    void savePolaCollections(int number);

    /**
     * get latest pola collections
     */
    void getPolaCollections(String objectId, GetDataCallback callback);

    public interface GetDataCallback {
        void onQueryPolaCollectionsDone(int number);
    }
}
