package com.github.runningforlife.photosniffer.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * a class to save image source site info
 */

public class ImageWebSite implements Parcelable{
    public String url;
    public String name;

    public ImageWebSite(String name, String url){
        this.url = url;
        this.name = name;
    }

    ImageWebSite(Parcel in) {
        url = in.readString();
        name = in.readString();
    }

    public static final Creator<ImageWebSite> CREATOR = new Creator<ImageWebSite>() {
        @Override
        public ImageWebSite createFromParcel(Parcel in) {
            return new ImageWebSite(in);
        }

        @Override
        public ImageWebSite[] newArray(int size) {
            return new ImageWebSite[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(name);
    }
}