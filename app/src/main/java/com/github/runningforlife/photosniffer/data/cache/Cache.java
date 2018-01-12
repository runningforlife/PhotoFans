package com.github.runningforlife.photosniffer.data.cache;

/**
 * Created by jason on 12/8/17.
 */

public interface Cache {

    /** initialize the cache */
    void initialize();

    /** put a image with url to Cache */
    boolean put(String url, Entry entry);

    /** get Cache by url */
    Entry get(String url);

    /** get file path by url */
    String getFilePath(String url);

    /** remove a image Cache */
    void remove(String url);

    void clear();

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
