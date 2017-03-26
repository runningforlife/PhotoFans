package jason.github.com.photofans.crawler.processor;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import jason.github.com.photofans.model.ImageItem;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.model.VisitedPageInfo;
import jason.github.com.photofans.service.MyThreadFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class ImageRetrievePageProcessor implements PageProcessor {

    private static final String TAG = "PageProcessor";

    // maximum number of letters of the name
    private final static int MAX_NAME_LEN = 10;

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    private final static String URL_FREE_JPG = "http://en.freejpg.com.ar/free/images/";
    private final static String MATCH_FREE_JPG = "http://en.freejpg.com.ar/asset/";

    private final static String URL_PIXELS = "https://www.pexels.com/";
    private final static String URL_ALBUM = "http://albumarium.com/";

    private final static String REG_FREE_JPG = "http://en\\.freejpg\\.com\\.ar/.*(\\.(gif|jpg|png))$";

    private List<ImageItem> imgList = new ArrayList<ImageItem>();
    private List<RetrieveCompleteListener> mListeners;
    private int mMaxRetrievedImages = DEFAULT_RETRIEVED_IMAGES;

    private static HashMap<String,Boolean> sAllPages = new HashMap<>();
    // last url to start this page retrieving
    private static String sLastUrl;

    private static final int DEFAULT_RETRIEVED_IMAGES = 20;

    public ImageRetrievePageProcessor(int n){
        mMaxRetrievedImages = n > 0 ? n : DEFAULT_RETRIEVED_IMAGES;
        mListeners = new ArrayList<>();

        loadPages();
    }

    @Override
    public void process(Page page) {
        Log.v(TAG,"process()");

        retrieveImages(page);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public String getStartUrl(){
        Log.d(TAG,"getStartUrl(): url = " + sLastUrl);
        return sLastUrl;
    }

    public interface RetrieveCompleteListener{
        void onRetrieveComplete(List<ImageItem> data);
        void onRetrieveComplete(VisitedPageInfo pageInfo);
    }

    public void addListener(RetrieveCompleteListener listener){
        mListeners.add(listener);
    }

    public void removeListener(RetrieveCompleteListener listener){
        mListeners.remove(listener);
    }

    private void retrieveImages(Page page){

        ImageRealm imageRealm = new ImageRealm();

        if(!isVisited(page)) {
            MyThreadFactory.getInstance()
                    .newThread(new SaveRunnable(getPageList(page)))
                    .start();

            boolean hasNewData = false;
            // here we retrieve all those IMAGE urls
            Document doc = page.getHtml().getDocument();
            Elements images = doc.select("img[src$=.jpg]");
            Log.v(TAG, "retrieved image size = " + images.size());
            for (Element img : images) {
                if (img.tagName().equals("img")) {
                    String url = img.attr("abs:src");
                    if (!url.startsWith(MATCH_FREE_JPG)) {
                        continue;
                    }
                    Log.v(TAG, "retrieved image url = " + url);
                    ImageItem info = new ImageItem();

                    String imgName = img.attr("alt");
                    if (TextUtils.isEmpty(imgName)) {
                        info.setName("unknown");
                    } else {
                        String str = imgName;
                        if (imgName.startsWith("free images")) {
                            str = imgName.substring("free images".length());
                        }
                        String firstWord = str.contains(" ") ? str.substring(0, str.indexOf(" "))
                                : str;
                        info.setName(firstWord);
                    }
                    info.setUrl(url);
                    info.setTimeStamp(System.currentTimeMillis());

                    imageRealm.setName(imgName);
                    imageRealm.setUrl(url);
                    imageRealm.setTimeStamp(System.currentTimeMillis());

                    if (!imgList.contains(info)) {
                        imgList.add(info);
                        hasNewData = true;
                    }
                }
                // enough already
                if (imgList.size() > mMaxRetrievedImages) {
                    break;
                }
            }

            if (hasNewData) {
                // notify jobs are done
                for (RetrieveCompleteListener listener : mListeners) {
                    listener.onRetrieveComplete(imgList);
                }
            }
        }
    }

    private void loadPages(){
        Realm realm = Realm.getDefaultInstance();

        RealmResults<VisitedPageInfo> pages = realm.where(VisitedPageInfo.class)
                .equalTo("mIsVisited",false)
                .findAll()
                .sort("mVisitTime",Sort.DESCENDING);
        Log.v(TAG,"loadPages(): data size = " + pages.size());
        if(pages.size() > 0){
            try {
                // choose a random page from unvisited url
                Random random = new Random();
                int idx = random.nextInt(pages.size());
                sLastUrl = pages.get(idx).getUrl();
                for (VisitedPageInfo info : pages) {
                    if (!sAllPages.containsKey(info.getUrl())) {
                        sAllPages.put(info.getUrl(),info.getIsVisited());
                    }
                }
            }finally {
                realm.close();
            }
        }
    }

    private boolean isVisited(Page page){
        return (sAllPages.containsKey(page.getUrl().get())
                && sAllPages.get(page.getUrl().get()));
    }

    private  class SaveRunnable implements  Runnable{
        private List<VisitedPageInfo> mPage;

        public SaveRunnable(List<VisitedPageInfo> page){
            mPage = page;
        }

        @Override
        public void run(){
            Looper.prepare();
            saveToRealm(mPage);
            Looper.loop();
        }
    }

    private List<VisitedPageInfo> getPageList(Page page){
        List<String> urlList = page.getHtml().links().regex(URL_FREE_JPG + ".*\\w").all();
        List<VisitedPageInfo> pageList = new ArrayList<>(urlList.size() + 1);
        page.addTargetRequests(urlList);

        urlList.add(page.getUrl().get());
        for(String url : urlList){
            if(!sAllPages.containsKey(url)) {
                VisitedPageInfo info = new VisitedPageInfo();
                info.setUrl(url);
                if(urlList.indexOf(url) != 0) {
                    info.setIsVisited(false);
                }else{
                    info.setIsVisited(true);
                }
                info.setVisitTime(System.currentTimeMillis());
                pageList.add(info);
                sAllPages.put(url,info.getIsVisited());
                Log.d(TAG,"getPageList(): page url = " + url);
            }
        }

        return pageList;
    }

    private void saveToRealm(final List<VisitedPageInfo> page){
        Log.v(TAG,"saveToRealm(): " + page.size() + " pages is retrieved");
        // save data
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(page);
            realm.commitTransaction();
        }finally {
            realm.close();
        }
    }
}
