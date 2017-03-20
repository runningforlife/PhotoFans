package jason.github.com.photofans.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jason on 3/19/17.
 */

public class ImageItem implements Parcelable{
    private String mUrl;
    private String mName;
    private long mTimeStamp;

    public ImageItem(){}
    
    protected ImageItem(Parcel in) {
        mUrl = in.readString();
        mName = in.readString();
    }

    public static final Creator<ImageItem> CREATOR = new Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel in) {
            return new ImageItem(in);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };

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

    @Override
    public String toString(){
        return "Image details:" +
                "url = " + mUrl +
                ",name = " + mName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUrl);
        dest.writeString(mName);
    }
}
