package com.github.runningforlife.photofans.crawler.processor;

import android.os.Looper;
import android.util.Log;

import com.github.runningforlife.photofans.model.ImageRealm;
import com.github.runningforlife.photofans.model.VisitedPageInfo;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import com.github.runningforlife.photofans.service.MyThreadFactory;
import com.github.runningforlife.photofans.utils.SharedPrefUtil;
import com.github.runningforlife.photofans.utils.UrlUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class ImageRetrievePageProcessor implements PageProcessor {

    private static final String TAG = "PageProcessor";

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    private List<ImageRealm> mImgList = new ArrayList<>();
    private List<RetrieveCompleteListener> mListeners;
    private int mMaxRetrievedImages = DEFAULT_RETRIEVED_IMAGES;

    private static HashMap<String,Boolean> sAllPages = new HashMap<>();
    // last url to start this page retrieving
    private static final int MAX_SEED_URL = 3;
    private static List<String> sLastUrl = new ArrayList<>();
    @SuppressWarnings("unchecked")
    private static HashSet<String> sValidPageUrls = new HashSet<>(new ArrayList(Arrays.asList(ImageSource.ALL_URLS)));

    private static final int DEFAULT_RETRIEVED_IMAGES = 10;

    public ImageRetrievePageProcessor(int n){
        mMaxRetrievedImages = n > 0 ? n : DEFAULT_RETRIEVED_IMAGES;
        mListeners = new ArrayList<>();

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
                if (mImgList.size() >= mMaxRetrievedImages) {
                    // marked part of them as used
                    for (int i = 0; i < mMaxRetrievedImages; ++i) {
                        mImgList.get(i).setUsed(true);
                    }
                    notifyListeners();
                }
            }

            // save to disk
            MyThreadFactory.getInstance()
                    .newThread(new SaveRunnable(getPageList(page)))
                    .start();
        }
    }

    private void notifyListeners(){
        // notify jobs are done
        for (RetrieveCompleteListener listener : mListeners) {
            listener.onRetrieveComplete(mImgList);
        }
    }

    private void loadPages(){
        Realm realm = Realm.getDefaultInstance();

        RealmResults<VisitedPageInfo> pages = realm.where(VisitedPageInfo.class)
                .equalTo("mIsVisited",false)
                .isNotNull("mUrl")
                .findAll();
        Log.v(TAG,"loadPages(): data size = " + pages.size());

        if(pages.size() > 0){
            try {
                // choose a random page from unvisited url
                for(int i = 0; i < MAX_SEED_URL && i < pages.size(); ++i) {
                    Random random = new Random();
                    int idx = random.nextInt(pages.size());
                    sLastUrl.add(pages.get(idx).getUrl());
                }
                for (VisitedPageInfo info : pages) {
                    if (!sAllPages.containsKey(info.getUrl())) {
                        sAllPages.put(info.getUrl(),info.getIsVisited());
                    }
                }
            }finally {
                realm.close();
            }
        }else{
            sLastUrl = SharedPrefUtil.getImageSource();
        }
    }

    private boolean isVisited(Page page){
        return (sAllPages.containsKey(page.getUrl().get())
                && sAllPages.get(page.getUrl().get()));
    }

    private boolean isValidPage(Page page){
        try {
            String baseUrl = UrlUtil.getRootUrl(page.getUrl().get());
            return sValidPageUrls.contains(baseUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidUrl(String url){
        try {
            String baseUrl = UrlUtil.getRootUrl(url);
            return sValidPageUrls.contains(baseUrl);
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
