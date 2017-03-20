package jason.github.com.photofans.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * image info
 */

public class ImageRealm extends RealmObject{
    private String mUrl;
    private String mName;
    private long mTimeStamp;

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
                ",name = " + mName +
                ", saved time = " + new Date(mTimeStamp);
    }

    @Override
    public boolean equals(Object o){
        if(! (o instanceof  ImageRealm)){
            return false;
        }

        return this == o || (this.mName.equals(((ImageRealm)o).mName)
                && this.mUrl.equals(((ImageRealm)o).mUrl));
    }
}
