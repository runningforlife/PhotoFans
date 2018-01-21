package com.github.runningforlife.photosniffer.data.remote;

import android.service.autofill.SaveCallback;

import java.io.File;

/**
 * API to access lean cloud
 */

public interface CloudApi {

    /*
     * save a file to cloud
     */
    void saveFile(String name, String data);

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
}
