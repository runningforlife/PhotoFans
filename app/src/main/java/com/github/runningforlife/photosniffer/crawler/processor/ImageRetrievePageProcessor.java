package com.github.runningforlife.photosniffer.crawler.processor;

import android.util.Log;
import android.webkit.URLUtil;

import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImagePageInfo;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

import com.github.runningforlife.photosniffer.utils.UrlUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class ImageRetrievePageProcessor implements PageProcessor {

    private static final String TAG = "ImagePageProcessor";

    private static final int DEFAULT_RETRIEVED_IMAGES = 20;

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    private static Random sRandom = new Random();

    private List<RetrieveCompleteListener> mListeners;
    private int mExpectedImages;
    private volatile boolean mIsExpectedDone;

    private HashMap<String,Boolean> mPagesState = new HashMap<>();
    private RealmResults<ImagePageInfo> mUnvisitedPages;
    // last url to start this page retrieving
    private static final int MAX_SEED_URL = 3;
    private List<String> mLastUrl;
    private int mCurrentImages;
    @SuppressWarnings("unchecked")
    private ImagePageFilter mPageFilter;
    // executor server to save data
    private ExecutorService mExecutor;

    private ImageRetrieverFactory mRetrieverFactory;
    private PageRetriever mPixelsRetriever;

    private RealmApi mRealApi;

    public ImageRetrievePageProcessor(int expected) {
        mExpectedImages = expected;
        mListeners = new ArrayList<>();
        mLastUrl = new ArrayList<>();
        mPageFilter = new ImagePageFilter();
        mIsExpectedDone = false;
        mCurrentImages = 0;
        mExecutor = Executors.newSingleThreadExecutor();
        //mRealApi = realmApi;
        mRealApi = RealmApiImpl.getInstance();

        loadPages();

        mRetrieverFactory = ImageRetrieverFactory.getInstance();
        mPixelsRetriever = new PixelPageRetriever(mPagesState);
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

    public List<String> getStartUrl() {
        Log.d(TAG,"getStartUrl(): url = " + mLastUrl.size());
        return mLastUrl;
    }

    public interface RetrieveCompleteListener {

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
        mRealApi.closeRealm();
        //mExecutor.shutdown();
    }

    private void retrieveImages(Page page) {

        List<ImageRealm> result = null;

        if (!isVisited(page) && isValidPage(page)) {
            if (page.getUrl().get().contains("https://www.pexels.com")) {
                result = mPixelsRetriever.retrieveImages(page);
                // save pages we get
                List<ImagePageInfo> pages = mPixelsRetriever.retrieveLinks(page);
                //mExecutor.submit(new SaveRunnable(pages));
                mRealApi.insertAsync(pages);
            } else {
                result = mRetrieverFactory.retrieveImages(page);
                // save to disk
                mRealApi.insertAsync(getPageList(page));
                //mExecutor.submit(new SaveRunnable(getPageList(page)));
            }

            if (result != null && result.size() > 0) {
                for (ImageRealm img : result) {
                    if (mCurrentImages <= mExpectedImages && !img.getUsed()) {
                        img.setUsed(true);
                    }
                    ++mCurrentImages;
                }

                //mExecutor.submit(new SaveRunnable(result));
                mRealApi.insertAsync(result);

                if (mCurrentImages >= mExpectedImages && !mIsExpectedDone) {
                    mIsExpectedDone = true;
                    notifyExpectedComplete();
                }

                if (mCurrentImages >= DEFAULT_RETRIEVED_IMAGES) {
                    notifyRetrieveComplete();
                }
            }
        }
    }

    private void notifyExpectedComplete() {
        // expected number of images is got
        for (RetrieveCompleteListener listener : mListeners) {
            listener.onExpectedComplete(mCurrentImages);
        }
    }

    private void notifyRetrieveComplete() {
        // notify jobs are done
        for (RetrieveCompleteListener listener : mListeners) {
            listener.onRetrieveComplete(mCurrentImages);
        }
    }

    private void loadPages() {
        Realm realm = Realm.getDefaultInstance();
        //mUnvisitedPages = RealmManager.getAllUnvisitedImagePages(realm);
        HashMap<String,String> params = new HashMap<>();
        params.put("mIsVisited", Boolean.toString(Boolean.FALSE));
        mUnvisitedPages = (RealmResults<ImagePageInfo>) mRealApi.querySync(ImagePageInfo.class, params);

        mUnvisitedPages.addChangeListener(new RealmChangeListener<RealmResults<ImagePageInfo>>() {
            @Override
            public void onChange(RealmResults<ImagePageInfo> element) {
                for (ImagePageInfo info : element) {
                    if (!mPagesState.containsKey(info.getUrl())) {
                        mPagesState.put(info.getUrl(),info.getIsVisited());
                    }
                }
            }
        });

        Log.d(TAG,"loadPages(): unvisisted page size = " + mUnvisitedPages.size());
        if (mUnvisitedPages.size() > 0) {
            for (int i = 0; mLastUrl.size() < MAX_SEED_URL && i < mUnvisitedPages.size(); ++i) {
                int idx = sRandom.nextInt(mUnvisitedPages.size());

                String url;
                try {
                    String pageUrl = mUnvisitedPages.get(idx).getUrl();
                    url = UrlUtil.getRootUrl(pageUrl);
                    if(mPageFilter.accept(url) && !mLastUrl.contains(pageUrl)) {
                        mLastUrl.add(pageUrl);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        for (ImagePageInfo info : mUnvisitedPages) {
            if (!mPagesState.containsKey(info.getUrl())) {
                mPagesState.put(info.getUrl(),info.getIsVisited());
            }
        }
    }

    private boolean isVisited(Page page) {
        return (mPagesState.containsKey(page.getUrl().get())
                && mPagesState.get(page.getUrl().get()));
    }

    private boolean isValidPage(Page page) {
        try {
            String baseUrl = UrlUtil.getRootUrl(page.getUrl().get());
            return URLUtil.isValidUrl(baseUrl) && mPageFilter.accept(baseUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidPageUrl(String url) {
        return URLUtil.isValidUrl(url);
    }

    private List<ImagePageInfo> getPageList(Page page) {

        List<String> urlList = page.getHtml().links().all();
        List<ImagePageInfo> pageList = new ArrayList<>(urlList.size() + 1);

        page.addTargetRequests(urlList);

        urlList.add(page.getUrl().get());
        for(String url : urlList){
            if(!mPagesState.containsKey(url) && isValidPageUrl(url)) {
                ImagePageInfo info = new ImagePageInfo();
                info.setUrl(url);
                if(urlList.indexOf(url) != 0) {
                    info.setIsVisited(false);
                }else{
                    info.setIsVisited(true);
                }
                info.setVisitTime(System.currentTimeMillis());
                pageList.add(info);
                mPagesState.put(url,info.getIsVisited());
                //Log.d(TAG,"getPageList(): page url = " + url);
            }
        }

        return pageList;
    }

/*    private final class SaveRunnable implements  Runnable {
        private List<? extends RealmObject> data;

        SaveRunnable(List<? extends RealmObject> data) {
            this.data = data;
        }

        @Override
        public void run() {
            RealmApiImpl.getInstance().insertAsync(data);
        }
    }*/
}
