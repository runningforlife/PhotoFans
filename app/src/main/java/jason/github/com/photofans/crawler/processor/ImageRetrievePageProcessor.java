package jason.github.com.photofans.crawler.processor;

import android.text.TextUtils;
import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import jason.github.com.photofans.model.ImageItem;
import jason.github.com.photofans.repository.RealmHelper;
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

    private RealmHelper mHelper;
    private static Set<String> sAllPages = Collections.newSetFromMap(new HashMap<String,Boolean>());
    // last url to start this page retrieving
    private static String sLastUrl;

    private static final int DEFAULT_RETRIEVED_IMAGES = 20;

    public ImageRetrievePageProcessor(int n){
        mMaxRetrievedImages = n > 0 ? n : DEFAULT_RETRIEVED_IMAGES;
        mListeners = new ArrayList<>();

        initRealm();
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
        Realm realm = Realm.getDefaultInstance();
        long time = realm.where(VisitedPageInfo.class)
                .max("mVisitTime").longValue();
        sLastUrl = realm.where(VisitedPageInfo.class)
                .equalTo("mVisitTime",time)
                .findFirst()
                .getUrl();

        return sLastUrl;
    }

    public interface RetrieveCompleteListener{
        void onRetrieveComplete(List<ImageItem> data);
    }

    public void addListener(RetrieveCompleteListener listener){
        mListeners.add(listener);
    }

    public void removeListener(RetrieveCompleteListener listener){
        mListeners.remove(listener);
    }

    private void retrieveImages(Page page){

        // loading url if there is none
        if(sAllPages.isEmpty()){
            loadPages();
        }

        if(!isVisited(page)) {
            // save data to realm
            VisitedPageInfo pageInfo = new VisitedPageInfo();
            pageInfo.setUrl(page.getUrl().get());
            pageInfo.setVisitTime(System.currentTimeMillis());
            //saveToRealm(pageInfo);

            page.addTargetRequests(page.getHtml().links().regex(URL_FREE_JPG + ".*\\w").all());

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

    private void initRealm(){
        mHelper = RealmHelper.getInstance();
        Realm realm = Realm.getDefaultInstance();

        RealmResults<VisitedPageInfo> pages = realm.where(VisitedPageInfo.class)
                .findAllSorted("mVisitTime", Sort.DESCENDING);
        if(pages.size() > 0){
            try {
                Log.v(TAG,"initRealm(): data size = " + pages.size());
                if(pages.size() > 0) {
                    sLastUrl = pages.get(0).getUrl();
                    for (VisitedPageInfo info : pages) {
                        if (sAllPages.contains(info.getUrl())) {
                            sAllPages.add(info.getUrl());
                        }
                    }
                }
            }finally {
                realm.close();
            }
        }
    }

    private void loadPages(){
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<VisitedPageInfo> pages = realm.where(VisitedPageInfo.class)
                    .findAll();
            if (pages.size() > 0) {
                for (VisitedPageInfo page : pages) {
                    if (!sAllPages.contains(page.getUrl())) {
                        sAllPages.add(page.getUrl());
                    }
                }
            }
        }finally {
            realm.close();
        }
    }

    private void saveToRealm(final VisitedPageInfo page){
        Log.v(TAG,"saveToRealm()");
        Realm realm = Realm.getDefaultInstance();

        try {
            realm.beginTransaction();
            realm.copyFromRealm(page);
            realm.commitTransaction();
        }finally {
            realm.close();
        }

    }

    private boolean isVisited(Page page){
        return sAllPages.contains(page.getUrl().get());
    }
}
