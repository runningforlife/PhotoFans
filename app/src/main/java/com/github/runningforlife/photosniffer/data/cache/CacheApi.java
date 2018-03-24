package com.github.runningforlife.photosniffer.data.cache;

import android.graphics.Bitmap;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * a Cache API for user
 */

public interface CacheApi {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EVENT_INIT, EVENT_PUT, EVENT_GET, EVENT_REMOVE, EVENT_CLEAR})
    @interface CacheAction {}
    int EVENT_INIT = 0;
    int EVENT_PUT = 1;
    int EVENT_GET = 2;
    int EVENT_REMOVE = 3;
    int EVENT_CLEAR = 4;

    interface CacheCallback {
        void onGetEntryDone(Cache.Entry entry);
    }

    /** put a bitmap into cache */
    void put(String url, Bitmap bitmap);

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
