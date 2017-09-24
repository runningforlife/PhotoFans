package com.github.runningforlife.photosniffer.crawler.processor;

import android.text.TextUtils;
import android.util.Log;

import com.github.runningforlife.photosniffer.model.QuotePageInfo;
import com.github.runningforlife.photosniffer.model.QuoteRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.service.MyThreadFactory;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import us.codecraft.webmagic.Page;

/**
 * retrieve quotes from pages
 */

public class QuotesRetriever implements PageRetriever<QuoteRealm>{
    private static final String TAG = "QuotesRetriever";

    private static final String CLASS_GRID = "grid";
    private static final String CLASS_HEADER = "grid__header";
    private static final String CLASS_CONTENT = "grid__content";
    private static final String CLASS_TITLE = "grid__title";
    private static final String CLASS_PAGE = "paging";
    private static final String CLASS_NEXT_PAGE_LINK = "nextpostslink";
    private static final String CLASS_AUTHOR = "quote-single__author-name-link";
    private static final String CLASS_CONTENT_LINK = "grid__content-link";
    private static final String ATTR_LINK = "href";
    private static final String ATTR_IMG = "img";

    private int expect;
    private RetrieveCompleteListener listener;
    // for test
    private List<QuotePageInfo> pageList;
    private List<QuoteRealm> quoteList;
    private List<String> retrievedPageUrl;

    //FIXME: 区分一个页面是否被使用过
    public QuotesRetriever(RealmResults<QuotePageInfo> pages, int expect){
        this.expect = expect;

        pageList = new ArrayList<>();
        quoteList = new ArrayList<>();
        retrievedPageUrl = new ArrayList<>();

        retrievedPageUrl.add(QuotePageFilter.QUOTE_SOURCE_0);
        if(pages != null && pages.size() > 0) {
            for (QuotePageInfo page : pages) {
                if(!retrievedPageUrl.contains(page.getUrl())) {
                    retrievedPageUrl.add(page.getUrl());
                }
            }
        }
    }

    public interface RetrieveCompleteListener{
        void onRetrieveComplete(int cnt);
    }

    public void setCompleteListener(RetrieveCompleteListener listener){
        this.listener = listener;
    }

    @Override
    public List<QuoteRealm> retrieve(Page page) {
        if(page == null) return null;
        String quotePageUrl = page.getUrl().get();
        Log.v(TAG,"retrieve(): page url " + quotePageUrl);

        List<QuoteRealm> quotes = new ArrayList<>();
        // page list
        List<QuotePageInfo> quotePageList = new ArrayList<>();
        // already visited
        quotePageList.add(new QuotePageInfo(quotePageUrl, true));

        // whether there are new pages add to next time retrieve
        boolean hasNewPages = false;

        Document doc = page.getHtml().getDocument();

        Elements elementsGrid = doc.getElementsByClass(CLASS_GRID);

        for(Element element : elementsGrid){
            // header url
            Elements header = element.getElementsByClass(CLASS_HEADER);
            String headerUrl = header.select(ATTR_IMG).get(0).attr("src");
            // author
            Elements eAuthor;
            if(!quotePageUrl.contains("author")) {
                eAuthor = element.getElementsByClass(CLASS_AUTHOR);
            }else{
                // for "author page", title is the author name
                eAuthor = element.getElementsByClass(CLASS_TITLE);
            }
            // content
            Elements eContent = element.getElementsByClass(CLASS_CONTENT_LINK);

            if(eAuthor.size() != 0 && eContent.size() != 0) {
                String author = eAuthor.get(0).ownText();
                // author page list
                String pageUrl = eAuthor.attr(ATTR_LINK);
                if (!retrievedPageUrl.contains(pageUrl)) {
                    QuotePageInfo pageInfo = new QuotePageInfo(pageUrl, false);
                    quotePageList.add(pageInfo);
                    // add requests
                    page.addTargetRequest(pageUrl);
                    hasNewPages = true;
                    retrievedPageUrl.add(pageUrl);
                }
                //quote content
                String url = eContent.get(0).attr(ATTR_LINK);
                String text = eContent.text();
                if (!TextUtils.isEmpty(text)) {
                    QuoteRealm quote = new QuoteRealm(url, author, text);
                    quote.setHeader(headerUrl);
                    quotes.add(quote);
                }
            }
        }
        // next page link
        if(doc.hasClass(CLASS_PAGE)) {
            Elements elementsNext = doc.getElementsByClass(CLASS_PAGE);
            for (Element element : elementsNext) {
                Elements nextPage = element.getElementsByClass(CLASS_NEXT_PAGE_LINK);
                String pageLink = nextPage.get(0).attr(ATTR_LINK);
                if (!retrievedPageUrl.contains(pageLink)) {
                    retrievedPageUrl.add(pageLink);
                    page.addTargetRequest(pageLink);
                    hasNewPages = true;

                    QuotePageInfo pageInfo = new QuotePageInfo(pageLink, false);
                    quotePageList.add(pageInfo);
                }
            }
        }
        // no new pages, get it from unvisited list
        if(!hasNewPages){
            String target = getPageUrl();
            Log.d(TAG,"add new request=" + target);
            page.addTargetRequest(target);
        }

        pageList.addAll(quotePageList);
        quoteList.addAll(quotes);
        // save quotes
        saveQuoteList(quotes);
        // save page list
        savePageList(quotePageList);

        if(quoteList.size() >= expect && listener != null){
            listener.onRetrieveComplete(quotes.size());
        }

        return quotes;
    }

    public List<QuotePageInfo> getPageList(){
        return pageList;
    }

    public List<QuoteRealm> getQuoteList(){
        return quoteList;
    }

    private void savePageList(final List<QuotePageInfo> pages){
        Log.v(TAG,"savePageList(): page size = " + pages.size());
        if(pages.size() > 0) {
            MyThreadFactory.getInstance().newThread(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "savePageList()");
                    RealmManager realmManager = RealmManager.getInstance();
                    realmManager.saveQuotePage(pages);
                }
            }).start();
        }
    }

    private String getPageUrl(){
        Random rnd = new Random();
        int idx = rnd.nextInt(retrievedPageUrl.size());
        Log.d(TAG,"getPageUrl(): size = " + retrievedPageUrl.size());
        return retrievedPageUrl.get(idx);
    }

    private void saveQuoteList(final List<QuoteRealm> quotes){
        Log.v(TAG,"saveQuoteList(): quotes size = " + quotes.size());
        if(quotes.size() > 0) {
            MyThreadFactory.getInstance().newThread(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "saveQuoteList()");
                    RealmManager realmManager = RealmManager.getInstance();
                    realmManager.saveQuoteRealm(quotes);
                }
            }).start();
        }
    }
}
