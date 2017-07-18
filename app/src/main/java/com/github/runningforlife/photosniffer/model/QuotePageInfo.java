package com.github.runningforlife.photosniffer.model;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * a web page of quotes
 */

public class QuotePageInfo extends RealmObject{
    @PrimaryKey
    private String url;
    private long time;
    private boolean isVisited;

    public QuotePageInfo(){}

    public QuotePageInfo(@NonNull String url, boolean isVisited){
        this.url = url;
        this.isVisited = isVisited;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public String getUrl(){
        return url;
    }

    public void setTime(long time){
        this.time = time;
    }

    public long getTime(){
        return time;
    }

    public void setVisited(boolean isVisited){
        this.isVisited = isVisited;
    }

    public boolean getVisited(){
        return isVisited;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof QuotePageInfo)) return false;

        return ((this == o) || (url.equals(((QuotePageInfo)o).url)));
    }
}

