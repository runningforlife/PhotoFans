package jason.github.com.photofans.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * a class to save image source site info
 */

public class ImageSource implements Parcelable{
    public String url;
    public String name;

    public ImageSource(String name, String url){
        this.url = url;
        this.name = name;
    }

    protected ImageSource(Parcel in) {
        url = in.readString();
        name = in.readString();
    }

    public static final Creator<ImageSource> CREATOR = new Creator<ImageSource>() {
        @Override
        public ImageSource createFromParcel(Parcel in) {
            return new ImageSource(in);
        }

        @Override
        public ImageSource[] newArray(int size) {
            return new ImageSource[size];
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