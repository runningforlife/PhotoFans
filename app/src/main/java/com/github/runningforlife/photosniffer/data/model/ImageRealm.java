package com.github.runningforlife.photosniffer.data.model;

import android.support.annotation.NonNull;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * image info
 */

public class ImageRealm extends RealmObject implements Comparable<ImageRealm> {
    @PrimaryKey
    private String mUrl;
    private String mName;
    private long mTimeStamp;
    private boolean mIsUsed;
    private boolean mIsFavor;
    private boolean mIsWallpaper;
    private boolean mIsCached;

    public ImageRealm() {
        mName = "unknown";
        mTimeStamp = System.currentTimeMillis();
        mIsUsed = false;
    }

    public void setUrl(String url){
        mUrl = url;
    }

    public String getUrl(){
        return mUrl;
    }

    public void setName(String name){
        mName = name;
    }

    public String getName(){
        return mName;
    }

    public void setTimeStamp(long time){
        mTimeStamp = time;
    }

    public long getTimeStamp(){
        return mTimeStamp;
    }

    public void setUsed(boolean used){
        mIsUsed = used;
    }

    public boolean getUsed(){
        return mIsUsed;
    }

    public boolean getIsFavor(){
        return mIsFavor;
    }

    public void setIsFavor(boolean isFavor){
        mIsFavor = isFavor;
    }

    public void setIsWallpaper(boolean isWallpaper){
        mIsWallpaper = isWallpaper;
    }

    public boolean getIsWallpaper(){
        return mIsWallpaper;
    }

    public boolean getIsCached() {
        return mIsCached;
    }

    public void setIsCached(boolean isCached) {
        mIsCached = isCached;
    }

    @Override
    public String toString() {
        return "Image details:" +
                "url = " + mUrl +
                ",name = " + mName +
                ",saved time = " + new Date(mTimeStamp);
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof  ImageRealm)) {
            return false;
        }

        return this == o || this.mUrl.equals(((ImageRealm)o).mUrl);
    }

    @Override
    public int hashCode() {
        return (int) (mName.hashCode()
                + mUrl.hashCode()
                + mTimeStamp >>> 2);
    }

    // time descending
    @Override
    public int compareTo(@NonNull ImageRealm o) {
        return (mTimeStamp - o.mTimeStamp) < 0 ? 1 :
                (((mTimeStamp - o.mTimeStamp) == 0) ? 0 : -1);
    }
}
