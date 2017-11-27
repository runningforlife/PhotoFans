package com.github.runningforlife.photosniffer.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * a class to record visited pages
 */

public class ImagePageInfo extends RealmObject {
    @PrimaryKey
    private String mUrl;
    private long mVisitTime;
    private boolean mIsVisited;

    public ImagePageInfo(){
    }

    public ImagePageInfo(String url){
        mIsVisited = false;
    }

    public void setUrl(String url){
        mUrl = url;
    }

    public String getUrl(){
        return mUrl;
    }

    public void setVisitTime(long time){
        mVisitTime = time;
    }

    public long getVisitTime(){
        return mVisitTime;
    }

    public void setIsVisited(boolean visited){
        mIsVisited = visited;
    }

    public boolean getIsVisited(){
        return mIsVisited;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof ImagePageInfo)){
            return false;
        }

        return this == o || (this.mUrl.equals(((ImagePageInfo)o).mUrl));
    }
}
