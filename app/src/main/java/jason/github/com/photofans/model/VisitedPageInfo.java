package jason.github.com.photofans.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * a class to record visited pages
 */

public class VisitedPageInfo extends RealmObject {
    @PrimaryKey
    private String mUrl;
    private long mVisitTime;
    private boolean mIsVisited;

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
}
