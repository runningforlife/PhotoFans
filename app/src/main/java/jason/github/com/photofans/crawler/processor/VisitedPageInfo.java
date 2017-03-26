package jason.github.com.photofans.crawler.processor;

import io.realm.RealmObject;

/**
 * a class to record visited pages
 */

public class VisitedPageInfo extends RealmObject {
    private String mUrl;
    private long mVisitTime;

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
}
