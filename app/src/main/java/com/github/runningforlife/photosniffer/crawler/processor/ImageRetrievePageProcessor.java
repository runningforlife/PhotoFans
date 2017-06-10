package com.github.runningforlife.photosniffer.crawler.processor;

import android.os.Looper;
import android.util.Log;
import android.webkit.URLUtil;

import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.model.VisitedPageInfo;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;

import com.github.runningforlife.photosniffer.service.MyThreadFactory;
import com.github.runningforlife.photosniffer.utils.UrlUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class ImageRetrievePageProcessor implements PageProcessor {

    private static final String TAG = "PageProcessor";

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    private List<ImageRealm> mImgList = new ArrayList<>();
    private List<RetrieveCompleteListener> mListeners;
    private int mExpectedImages;
    private volatile boolean mIsExpectedDone;

    private static HashMap<String,Boolean> sAllPages = new HashMap<>();
    // last url to start this page retrieving
    private static final int MAX_SEED_URL = 3;
    private static List<String> sLastUrl;
    @SuppressWarnings("unchecked")
    private SourcePageFilter mPageFilter;
    private static final int DEFAULT_RETRIEVED_IMAGES = 20;

    public ImageRetrievePageProcessor(int expected){
        mExpectedImages = expected;
        mListeners = new ArrayList<>();
        sLastUrl = new ArrayList<>();
        mPageFilter = new SourcePageFilter();
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
        void onExpectedComplete(List<ImageRealm> expected);
        void onRetrieveComplete(List<ImageRealm> data);
    }

    public void addListener(RetrieveCompleteListener listener){
        mListeners.add(listener);
    }

    public void removeListener(RetrieveCompleteListener listener){
        mListeners.remove(listener);
    }

    public List<ImageRealm> getImageList(){
        return mImgList;
    }

    private void retrieveImages(Page page){

        if(!isVisited(page) && isValidPage(page)) {
            List<ImageRealm> result = ImageRetrieverFactory.getInstance().
                    retrieveImages(page);
            if(result != null && result.size() > 0) {
                mImgList.addAll(result);
                if (mImgList.size() >= mExpectedImages) {
                    // marked part of them as used
                    for (int i = 0; i < mExpectedImages; ++i) {
                        mImgList.get(i).setUsed(true);
                    }
                    mIsExpectedDone = true;
                    notifyExpectedComplete();
                }

                if(mImgList.size() >= DEFAULT_RETRIEVED_IMAGES){
                    notifyRetrieveComplete();
                }
            }

            // save to disk
            MyThreadFactory.getInstance()
                    .newThread(new SaveRunnable(getPageList(page)))
                    .start();
        }
    }

    private void notifyExpectedComplete(){
        // expected number of images is got
        for (RetrieveCompleteListener listener : mListeners) {
            listener.onExpectedComplete(mImgList);
        }
    }

    private void notifyRetrieveComplete(){
        // notify jobs are done
        for (RetrieveCompleteListener listener : mListeners) {
            listener.onRetrieveComplete(mImgList);
        }
    }

    private void loadPages(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<VisitedPageInfo> pages = RealmManager.getInstance()
                .getAllUnvisitedPages(realm);
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

            for (VisitedPageInfo info : pages) {
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

    private boolean isValidUrl(String url){
        try {
            String baseUrl = UrlUtil.getRootUrl(url);
            return URLUtil.isValidUrl(baseUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private  class SaveRunnable implements  Runnable{
        private List<VisitedPageInfo> mPage;

        SaveRunnable(List<VisitedPageInfo> page){
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
            if(!sAllPages.containsKey(url) && isValidUrl(url)) {
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
                //Log.d(TAG,"getPageList(): page url = " + url);
            }
        }

        return pageList;
    }

    private void saveToRealm(final List<VisitedPageInfo> page){
        Log.v(TAG,"saveToRealm(): " + page.size() + " pages is retrieved");
        // save data
        RealmManager.getInstance()
                .writeAsync(page);
    }
}
