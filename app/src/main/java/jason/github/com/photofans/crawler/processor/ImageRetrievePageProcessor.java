package jason.github.com.photofans.crawler.processor;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
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

    private final static String WIDTH = "width";
    private final static String HEIGHT = "height";
    // filter image width and height
    private final static int MIN_WIDTH = 200;
    private final static int MIN_HEIGHT = 200;

    private final static String URL_FREE_JPG = "http://en.freejpg.com.ar/free/images";
    private final static String MATCH_FREE_JPG = "http://en.freejpg.com.ar/asset";

    private final static String URL_PIXELS = "https://www.pexels.com";
    private final static String URL_ALBUM = "http://albumarium.com";

    private final static String REG_FREE_JPG = "http://en\\.freejpg\\.com\\.ar/.*(\\.(gif|jpg|png))$";

    private List<ImageRealm> imgList = new ArrayList<>();
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

        int status = page.getStatusCode();
        if(status == 200) {
            retrieveImages(page);
        }else if(status >= 400 && status <= 511){
            // error happens
        }
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
        void onRetrieveComplete(List<ImageRealm> data);
    }

    public void addListener(RetrieveCompleteListener listener){
        mListeners.add(listener);
    }

    public void removeListener(RetrieveCompleteListener listener){
        mListeners.remove(listener);
    }

    private void retrieveImages(Page page){

        if(!isVisited(page)) {
            MyThreadFactory.getInstance()
                    .newThread(new SaveRunnable(getPageList(page)))
                    .start();

            boolean hasNewData = false;
            // here we retrieve all those IMAGE urls
            Document doc = page.getHtml().getDocument();
            Elements images = doc.select("img");
            Log.v(TAG, "retrieved images = " + images.size());
            for (Element img : images) {
                // here, absolute URL and relative URL
                if (img.tagName().equals("img")) {
                    String relUrl = img.attr("src");
                    String url = relUrl;
                    String absUrl = img.attr("abs:src");
                    // compose relative url and base url
                    if(!URLUtil.isNetworkUrl(absUrl)){
                        if(page.getUrl().get().startsWith(URL_ALBUM)){
                            url = URL_ALBUM + relUrl;
                        }
                    }

                    Log.v(TAG,"base uri = " + img.baseUri());

                    int w = MIN_WIDTH,h = MIN_HEIGHT;
                    if(img.hasAttr(WIDTH)) {
                        String width = img.attr("width");

                        w = Integer.parseInt(width);
                    }
                    if(img.hasAttr(HEIGHT)){
                        h = Integer.parseInt(img.attr(HEIGHT));
                    }

                    if(w < MIN_WIDTH || h < MIN_HEIGHT){
                        continue;
                    }
                    Log.v(TAG, "retrieved image url = " + url);
                    if(!URLUtil.isValidUrl(url)){
                        continue;
                    }
                    ImageRealm imageRealm = new ImageRealm();

                    String imgName = img.attr("alt");
                    if (TextUtils.isEmpty(imgName)) {
                        imageRealm.setName("unknown");
                    } else {
                        String str = imgName;
                        if (imgName.startsWith("free images")) {
                            str = imgName.substring("free images".length());
                        }
                        String firstWord = str.contains(" ") ? str.substring(0, str.indexOf(" "))
                                : str;
                        imageRealm.setName(firstWord);
                    }
                    imageRealm.setName(imgName);
                    imageRealm.setUrl(url);
                    imageRealm.setTimeStamp(System.currentTimeMillis());

                    if (!imgList.contains(imageRealm)) {
                        imgList.add(imageRealm);
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
        List<String> urlList = page.getHtml().links().all();
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
