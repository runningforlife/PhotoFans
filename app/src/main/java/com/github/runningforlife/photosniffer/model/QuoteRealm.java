package com.github.runningforlife.photosniffer.model;

import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * daily quotes realm data
 */

public class QuoteRealm extends RealmObject {
    @PrimaryKey
    @NonNull
    private String url;
    private String author;
    private String text;
    private String savedTime;
    private long showTime; // when this item is showed

    public QuoteRealm(){

    }

    public QuoteRealm(@NonNull String url, String author, String text){
        this.url = url;
        this.author = author;
        this.text = text;
        this.savedTime = DateFormat.getDateTimeInstance().format(new Date());
    }

    public void setUrl(String url){
        this.url = url;
    }

    public String getUrl(){
        return url;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public String getAuthor(){
        return author;
    }

    public void setText(String text){
        this.text = text;
    }

    public String getText(){
        return text;
    }

    public void setSavedTime(long time){
        this.savedTime = DateFormat.getDateTimeInstance()
                .format(new Date(time));
    }

    public String getSavedTime(){
        return savedTime;
    }

    public void setShowTime(long showTime){
        this.showTime = showTime;
    }

    public long getShowTime(){
        return showTime;
    }

    @Override
    public String toString(){
        return "Quote Detail:" +
                ",url=" + url +
                ",author=" + author +
                ",text=" + text;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof QuoteRealm)) return false;

        return (this == o || (url.equals(((QuoteRealm)o).url) &&
                (author.equals(((QuoteRealm)o).author) &&
                (text.equals(((QuoteRealm)o).text)))));
    }
}
