package com.github.runningforlife.photosniffer.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * a class to record visited pages
 */

public class ImagePageInfo extends RealmObject {
    @PrimaryKey
    private String mUrl;
    private long mTimeStamp;
    private boolean mIsVisited;

    public ImagePageInfo() {
        mTimeStamp = System.currentTimeMillis();
        mIsVisited = false;
    }

    public ImagePageInfo(String url){
        mUrl = url;
        mIsVisited = false;
    }

    public void setUrl(String url){
        mUrl = url;
    }

    public String getUrl(){
        return mUrl;
    }

    public void setVisitTime(long time){
        mTimeStamp = time;
    }

    public long getVisitTime() {
        return mTimeStamp;
    }

    public void setIsVisited(boolean visited){
        mIsVisited = visited;
    }

    public boolean getIsVisited(){
        return mIsVisited;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ImagePageInfo)) {
            return false;
        }

        return this == o || (this.mUrl.equals(((ImagePageInfo)o).mUrl));
    }
}
