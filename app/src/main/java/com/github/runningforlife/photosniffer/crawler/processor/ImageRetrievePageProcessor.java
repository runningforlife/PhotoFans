package com.github.runningforlife.photosniffer.crawler.processor;

import android.util.Log;
import android.webkit.URLUtil;

import com.github.runningforlife.photosniffer.data.model.ImagePageInfo;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.data.local.RealmManager;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

import com.github.runningforlife.photosniffer.service.MyThreadFactory;
import com.github.runningforlife.photosniffer.utils.UrlUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class ImageRetrievePageProcessor implements PageProcessor {

    private static final String TAG = "ImagePageProcessor";

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    private List<RetrieveCompleteListener> mListeners;
    private int mExpectedImages;
    private volatile boolean mIsExpectedDone;

    private static HashMap<String,Boolean> sAllPages = new HashMap<>();
    // last url to start this page retrieving
    private static final int MAX_SEED_URL = 3;
    private static List<String> sLastUrl;
    private int mCurrentImages;
    @SuppressWarnings("unchecked")
    private ImagePageFilter mPageFilter;
    private static final int DEFAULT_RETRIEVED_IMAGES = 20;
    // executor server to save data
    private ExecutorService mExecutor;

    public ImageRetrievePageProcessor(int expected){
        mExpectedImages = expected;
        mListeners = new ArrayList<>();
        sLastUrl = new ArrayList<>();
        mPageFilter = new ImagePageFilter();
        mIsExpectedDone = false;
        mCurrentImages = 0;
        mExecutor = Executors.newFixedThreadPool(2,new MyThreadFactory());

        loadPages();
    }

    @Override
    public void process(Page page) {
        Log.v(TAG,"process()");

        if(page != null) {
            int status = page.getStatusCode();
            if (status == 200) {
                retrieveImages(page);
            } else if (status >= 400 && status <= 511) {
                // error happens
                Log.v(TAG,"cannot download page,status = " + status);
            }
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public List<String> getStartUrl(){
        Log.d(TAG,"getStartUrl(): url = " + sLastUrl.size());
        return sLastUrl;
    }

    public interface RetrieveCompleteListener{
        void onExpectedComplete(int imgCount);
        void onRetrieveComplete(int imgCount);
    }

    public void addListener(RetrieveCompleteListener listener){
        mListeners.add(listener);
    }

    public void removeListener(RetrieveCompleteListener listener){
        mListeners.remove(listener);
    }

    public int getRetrievedImageCount(){
        return mCurrentImages;
    }

    public void stopGracefully(){
        mExecutor.shutdown();
    }

    private void retrieveImages(Page page){

        if(!isVisited(page) && isValidPage(page)) {
            List<ImageRealm> result = ImageRetrieverFactory.getInstance().
                    retrieve(page);
            if(result != null && result.size() > 0) {
                for(ImageRealm img : result){
                    if(mCurrentImages <= mExpectedImages && !img.getUsed()){
                        img.setUsed(true);
                    }

                    ++mCurrentImages;
                }

                mExecutor.submit(new SaveRunnable(result,
                        ImageRealm.class.getSimpleName()));

                if (mCurrentImages >= mExpectedImages && !mIsExpectedDone) {
                    mIsExpectedDone = true;
                    notifyExpectedComplete();
                }

                if(mCurrentImages >= DEFAULT_RETRIEVED_IMAGES){
                    notifyRetrieveComplete();
                }
            }

            // save to disk
            mExecutor.submit(new SaveRunnable(getPageList(page),
                    ImagePageInfo.class.getSimpleName()));
        }
    }

    private void notifyExpectedComplete(){
        // expected number of images is got
        for (RetrieveCompleteListener listener : mListeners) {
            listener.onExpectedComplete(mCurrentImages);
        }
    }

    private void notifyRetrieveComplete(){
        // notify jobs are done
        for (RetrieveCompleteListener listener : mListeners) {
            listener.onRetrieveComplete(mCurrentImages);
        }
    }

    private void loadPages(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ImagePageInfo> pages = RealmManager.getAllUnvisitedImagePages(realm);
        Log.d(TAG,"loadPages(): unvisisted page size = " + pages.size());

        if(pages.size() > 0){
            // choose a random page from unvisited url
            for(int i = 0; sLastUrl.size() < MAX_SEED_URL && i < pages.size(); ++i) {
                Random random = new Random();
                int idx = random.nextInt(pages.size());

                final String url;
                try {
                    String pageUrl = pages.get(idx).getUrl();
                    url = UrlUtil.getRootUrl(pageUrl);
                    if(mPageFilter.accept(url) && !sLastUrl.contains(pageUrl)) {
                        sLastUrl.add(pageUrl);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            for (ImagePageInfo info : pages) {
                if (!sAllPages.containsKey(info.getUrl())) {
                    sAllPages.put(info.getUrl(),info.getIsVisited());
                }
            }
        }

        realm.close();
    }

    private boolean isVisited(Page page){
        return (sAllPages.containsKey(page.getUrl().get())
                && sAllPages.get(page.getUrl().get()));
    }

    private boolean isValidPage(Page page){
        try {
            String baseUrl = UrlUtil.getRootUrl(page.getUrl().get());
            return URLUtil.isValidUrl(baseUrl) && mPageFilter.accept(baseUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidPageUrl(String url){
        return URLUtil.isValidUrl(url);
    }

    private  class SaveRunnable implements  Runnable{
        private List<? extends RealmObject> data;
        private String type;

        SaveRunnable(List<? extends RealmObject> data, String type){
            this.data = data;
            this.type = type;
        }

        @Override
        public void run(){
            RealmManager rm = RealmManager.getInstance();
            if(ImageRealm.class.getSimpleName().equals(type)){
                rm.saveImageRealmAsync((List<ImageRealm>) data);
            }
            else if(ImagePageInfo.class.getSimpleName().equals(type)){
                rm.savePageAsync((List<ImagePageInfo>) data);
            }
        }
    }

    private List<ImagePageInfo> getPageList(Page page){

        List<String> urlList = page.getHtml().links().all();
        List<ImagePageInfo> pageList = new ArrayList<>(urlList.size() + 1);

        page.addTargetRequests(urlList);

        urlList.add(page.getUrl().get());
        for(String url : urlList){
            if(!sAllPages.containsKey(url) && isValidPageUrl(url)) {
                ImagePageInfo info = new ImagePageInfo();
                info.setUrl(url);
                if(urlList.indexOf(url) != 0) {
                    info.setIsVisited(false);
                }else{
                    info.setIsVisited(true);
                }
                info.setVisitTime(System.currentTimeMillis());
                pageList.add(info);
                sAllPages.put(url,info.getIsVisited());
                //Log.d(TAG,"getPageList(): page url = " + url);
            }
        }

        return pageList;
    }
}
