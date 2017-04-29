package jason.github.com.photofans.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import jason.github.com.photofans.crawler.OkHttpDownloader;
import jason.github.com.photofans.crawler.processor.ImageRetrievePageProcessor;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.model.RealmHelper;
import us.codecraft.webmagic.Spider;

import static jason.github.com.photofans.crawler.processor.ImageSource.*;

/**
 *  a service to handle image retrieve request from client
 *  client may want to give a limited number of images to retrieve at a time
 *
 *  @author JasonWang
 */

public class ImageRetrieveService extends IntentService implements
        ImageRetrievePageProcessor.RetrieveCompleteListener{

    private static final String TAG = "ImageRetrieveService";
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
            mSpider = Spider.create(mProcessor)
                    .addUrl(new String[]{URL_MM,URL_YOUWU})
                    .setDownloader(new OkHttpDownloader());
        }else{
            mSpider = Spider.create(mProcessor)
                    .addUrl(lastUrl)
                    .setDownloader(new OkHttpDownloader());
        }

        mSpider.run();

        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // time out
                sendResult(0);
            }
        }, 60000);

    }

    @Override
    public void onRetrieveComplete(List<ImageRealm> data) {
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
        // stop the spider
        mSpider.stop();
        
        Bundle bundle = new Bundle();
        bundle.putLong("result", size);
        if(size != 0) {
            mReceiver.send(ServiceStatus.SUCCESS, bundle);
        }else{
            mReceiver.send(ServiceStatus.ERROR,null);
        }
        // remove listener
        mProcessor.removeListener(this);
    }
}
