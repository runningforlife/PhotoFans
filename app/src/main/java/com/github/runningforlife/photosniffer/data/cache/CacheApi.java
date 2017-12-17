package com.github.runningforlife.photosniffer.data.cache;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * a Cache API for user
 */

public interface CacheApi {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ACTION_INIT, ACTION_PUT, ACTION_GET, ACTION_REMOVE, ACTION_CLEAR})
    @interface CacheAction {}
    String ACTION_INIT = "init";
    String ACTION_PUT = "put";
    String ACTION_GET = "get";
    String ACTION_REMOVE = "remove";
    String ACTION_CLEAR = "clear";

    interface CacheCallback {
        void onGetEntryDone(Cache.Entry entry);
    }

    /** put a image with url to Cache */
    void put(String url, Cache.Entry entry);

    /** get Cache by url, this is async by a callback */
    void get(String url, CacheCallback cacheCallback);

    /** get Cache by url sync */
    Cache.Entry get(String url);

    /** get file path by url */
    String getFilePath(String url);

    /** remove a image Cache, this is async */
    void remove(String url);

    /** clear the Cache */
    void clear();

    /** whether a url exits */
    boolean isExist(String url);
}
