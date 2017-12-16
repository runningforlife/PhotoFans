package com.github.runningforlife.photosniffer.data.cache;

import android.graphics.Bitmap;

/**
 * Created by jason on 12/8/17.
 */

public interface cache {

    /** put a image with url to cache */
    void put(String url, Entry entry);

    /** get cache by url */
    Entry get(String url);

    /** remove a image cache */
    void remove(String url);

    void clear();

    String getCacheKey(String url);

    String getFileName(String key);

    boolean isExist(String url);

    final class Entry {

        public Entry(byte[] data, long lastModified) {
            this.data = data;
            this.lastModified = lastModified;
        }

        /* data to put */
        public byte[] data;

        /* last modified time */
        public long lastModified;
    }
}
