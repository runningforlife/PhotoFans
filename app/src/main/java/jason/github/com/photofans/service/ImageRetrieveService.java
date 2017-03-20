package jason.github.com.photofans.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jason.github.com.photofans.crawler.OkHttpDownloader;
import jason.github.com.photofans.crawler.processor.ImageRetrievePageProcessor;
import jason.github.com.photofans.model.ImageItem;
import jason.github.com.photofans.model.ImageRealm;
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

    private final static String URL_FREE_JPG = "http://en.freejpg.com.ar/free/images/";
    private final static String URL_PIXELS = "https://www.pexels.com/";
    private final static String URL_ALBUM = "http://albumarium.com/";

    private final static String REG_FREE_JPG = "http://en\\.freejpg\\.com\\.ar/.*(\\.(gif|jpg|png))$";

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
        mSpider = Spider.create(mProcessor)
                .addUrl(URL_FREE_JPG)
                .setDownloader(new OkHttpDownloader());
        mSpider.run();
    }

    @Override
    public void onRetrieveComplete(List<ImageItem> data) {
        Log.v(TAG,"onRetrieveComplete()");

        if(data == null || data.isEmpty()){
            mReceiver.send(ServiceStatus.ERROR,null);
        }else {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("result", (ArrayList<? extends Parcelable>) data);
            mReceiver.send(ServiceStatus.SUCCESS, bundle);
        }
        // remove listener
        mProcessor.removeListener(this);
        // stop the spider
        mSpider.stop();
    }
}
