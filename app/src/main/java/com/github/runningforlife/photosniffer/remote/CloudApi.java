package com.github.runningforlife.photosniffer.remote;

/**
 * API to access lean cloud
 */

public interface CloudApi {

    /*
     * save a file to cloud
     */
    void saveFile(String name, String data);

    /*
     * upload user advice to cloud
     */
    void saveAdvice(String advice);

}
