package jason.github.com.photofans.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import jason.github.com.photofans.crawler.OkHttpDownloader;
import jason.github.com.photofans.crawler.processor.ImageRetrievePageProcessor;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.model.RealmHelper;
import us.codecraft.webmagic.Spider;

/**
 *  a service to handle image retrieve request from client
 *  client may want to give a limited number of images to retrieve at a time
 *
 *  @author JasonWang
 */

public class ImageRetrieveService extends IntentService implements
        ImageRetrievePageProcessor.RetrieveCompleteListener{

    private static final String TAG = "ImageRetrieveService";
    //TODO: different websites may have different image url format
    // we want to treat it differently
    private final static String URL_FREE_JPG = "http://en.freejpg.com.ar/free/images/";
    private final static String URL_PIXELS = "https://www.pexels.com/";
    private final static String URL_ALBUM = "http://albumarium.com/";
    private static final String URL_ILLUSION = "http://illusion.scene360.com/";
    private static final String URL_VISUAL_HUNT = "https://visualhunt.com/";
    private static final String URL_1X = "https://1x.com/";
    private static final String URL_PIXBABY = "https://pixabay.com/";
    private static final String URL_PUBLIC_ARCHIVE = "http://publicdomainarchive.com/";
    private static final String URL_VISUAL_CHINA = "http://www.vcg.com/creative";
    // max number of images to be retrieved a time
    public static final String EXTRA_MAX_IMAGES = "maxImages";

    private ResultReceiver mReceiver;
    private ImageRetrievePageProcessor mProcessor;
    private Spider mSpider;

    public ImageRetrieveService(){
        super("ImageRetrieveService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int max = intent.getIntExtra(EXTRA_MAX_IMAGES,0);
        mReceiver = intent.getParcelableExtra("receiver");
        if(mReceiver != null) {
            mReceiver.send(ServiceStatus.RUNNING, null);
        }
        startCrawler(max);
    }

    private void startCrawler(int n){
        Log.i(TAG,"startCrawler(): max images to be retrieved = " + n);
        mProcessor = new ImageRetrievePageProcessor(n);
        mProcessor.addListener(this);
        String lastUrl = mProcessor.getStartUrl();
        if(TextUtils.isEmpty(lastUrl)){
            String[] allUrls = {URL_PIXELS,URL_FREE_JPG,URL_ILLUSION,
            URL_VISUAL_CHINA,URL_VISUAL_HUNT,URL_1X,URL_PUBLIC_ARCHIVE};
            mSpider = Spider.create(mProcessor)
                    .addUrl(allUrls)
                    .setDownloader(new OkHttpDownloader());
        }else{
            mSpider = Spider.create(mProcessor)
                    .addUrl(lastUrl)
                    .setDownloader(new OkHttpDownloader());
        }

        mSpider.run();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // time out
                mSpider.stop();
                sendResult(0);
            }
        }, 60000);

    }

    @Override
    public void onRetrieveComplete(List<ImageRealm> data) {

        mSpider.stop();
        if(data == null || data.isEmpty()){
            Log.v(TAG,"onRetrieveComplete(): data is empty");
            sendResult(0);
        }else {
            Log.v(TAG,"onRetrieveComplete(): retrieved data size = " + data.size());
            saveToRealm(data);
            sendResult(data.size());
        }
    }

    private void saveToRealm(List<ImageRealm> data){
        RealmHelper.getInstance().writeAsync(data);
    }

    private void sendResult(int size){
        Bundle bundle = new Bundle();
        bundle.putLong("result", size);
        if(size != 0) {
            mReceiver.send(ServiceStatus.SUCCESS, bundle);
        }else{
            mReceiver.send(ServiceStatus.ERROR,null);
        }
        // remove listener
        mProcessor.removeListener(this);
        // stop the spider
        mSpider.stop();
    }
}
